/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.process.workitem.bpmn2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.jbpm.process.instance.impl.util.TypeTransformer;
import org.jbpm.process.workitem.core.AbstractLogOrThrowWorkItemHandler;
import org.jbpm.workflow.core.node.DataAssociation;
import org.jbpm.workflow.core.node.WorkItemNode;
import org.jbpm.workflow.instance.WorkflowProcessInstance;
import org.jbpm.workflow.instance.node.WorkItemNodeInstance;
import org.kie.api.KieServices;
import org.kie.api.builder.KieScanner;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.command.KieCommands;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNMessage.Severity;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.internal.runtime.Cacheable;
import org.kie.internal.runtime.manager.RuntimeManagerRegistry;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.JavaParser;
import com.github.javaparser.JavaParserBuild;

/**
 * Additional BusinessRuleTask support that allows to decouple rules from processes - as default BusinessRuleTask
 * uses exact same working memory (kie session) as process which essentially means same kbase.
 * To allow better separation and maintainability BusinessRuleTaskHandler is provided that supports:
 * <ul>
 * <li>DRL stateful</li>
 * <li>DRL stateless</li>
 * <li>DMN</li>
 * </ul>
 * Type of runtime is selected by Language data input and if not given defaults to DRL stateless.
 * <p>
 * Session type can be given by KieSessionType data input and session name can be given as KieSessionName property -these apply to DRL only.
 * <p>
 * DMN support following data inputs:
 * <ul>
 * <li>Namespace - DMN namespace to be used - mandatory</li>
 * <li>Model - DMN model to be used - mandatory</li>
 * <li>Decision - DMN decision name to be used - optional</li>
 * </ul>
 * <p>
 * Results returned will be then put back into the data outputs. <br/>
 * <br/>
 * DRL handling is based on same names for data input and output as that is then used as correlation.<br/>
 * DMN handling receives all data from DMNResult.<br/>
 */
public abstract class AbstractRuleTaskHandler extends AbstractLogOrThrowWorkItemHandler implements Cacheable {

    private static final Logger logger = LoggerFactory.getLogger(AbstractRuleTaskHandler.class);

    protected static final String STATELESS_TYPE = "stateless";
    protected static final String STATEFULL_TYPE = "statefull";
    protected static final String STATEFUL_TYPE = "stateful";

    protected static final String DRL_LANG = "DRL";
    protected static final String DMN_LANG = "DMN";

    private KieServices kieServices = KieServices.get();
    private KieCommands commandsFactory = kieServices.getCommands();
    private KieContainer kieContainer;
    private KieScanner kieScanner;

    private ClassLoader classLoader;
    private RuntimeManager runtimeManager;
    private TypeTransformer typeTransformer;
    
    public AbstractRuleTaskHandler(String groupId,
                                   String artifactId,
                                   String version) {
        this(groupId,
             artifactId,
             version,
             -1);
    }

    public AbstractRuleTaskHandler(String groupId,
            String artifactId,
            String version,
            long scannerInterval) {
        this(groupId, artifactId, version, scannerInterval, null, null);
    }
    public AbstractRuleTaskHandler(String groupId,
                                   String artifactId,
                                   String version,
                                   long scannerInterval,
                                   ClassLoader classLoader,
                                   RuntimeManager runtimeManager) {
        logger.debug("About to create KieContainer for {}, {}, {} with scanner interval {}",
                     groupId,
                     artifactId,
                     version,
                     scannerInterval);
        kieContainer = kieServices.newKieContainer(kieServices.newReleaseId(groupId,
                                                                            artifactId,
                                                                            version));
        this.classLoader = classLoader;
        this.runtimeManager = runtimeManager;
        this.typeTransformer = new TypeTransformer(classLoader);

        if (scannerInterval > 0) {
            kieScanner = kieServices.newKieScanner(kieContainer);
            kieScanner.start(scannerInterval);
            logger.debug("Scanner started for {} with poll interval set to {}",
                         kieContainer,
                         scannerInterval);
        }
    }
    
    public abstract String getRuleLanguage();

    public void executeWorkItem(WorkItem workItem,
                                final WorkItemManager manager) {

        Map<String, Object> parameters = new HashMap<>(workItem.getParameters());
        String language = (String) parameters.remove("Language");
        if (language == null) {
            language = getRuleLanguage();
        }
        String kieSessionName = (String) parameters.remove("KieSessionName");
        String kieSessionType = (String) parameters.remove("KieSessionType");
        if (kieSessionType == null) {
            kieSessionType = STATELESS_TYPE;
        }

        Map<String, Object> results = new HashMap<>();
        try {
            logger.debug("Facts to be inserted into working memory {}",
                         parameters);
            if (DRL_LANG.equalsIgnoreCase(language)) {
                if (STATEFULL_TYPE.equalsIgnoreCase(kieSessionType) || 
                    STATEFUL_TYPE.equalsIgnoreCase(kieSessionType)) {
                    handleStatefull(workItem,
                                    kieSessionName,
                                    parameters,
                                    results);
                } else {
                    handleStateless(workItem,
                                    kieSessionName,
                                    parameters,
                                    results);
                }
            } else if (DMN_LANG.equalsIgnoreCase(language)) {
                handleDMN(workItem,
                          parameters,
                          results);
            } else {
                throw new IllegalArgumentException("Not supported language type " + language);
            }
            logger.debug("Facts retrieved from working memory {}",
                         results);
            manager.completeWorkItem(workItem.getId(),
                                     results);
        } catch (Exception e) {
            handleException(e);
        }
    }

    @Override
    public void abortWorkItem(WorkItem workItem,
                              WorkItemManager manager) {
        // no-op
    }

    public void setRuntimeManager(RuntimeManager runtimeManager) {
        this.runtimeManager = runtimeManager;
    }

    @Override
    public void close() {
        if (kieScanner != null) {
            kieScanner.shutdown();
            logger.debug("Scanner shutdown for kie container {}",
                         kieContainer);
        }
        kieContainer.dispose();
    }

    protected void handleStatefull(WorkItem workItem,
                                   String kieSessionName,
                                   Map<String, Object> parameters,
                                   Map<String, Object> results) {
        logger.debug("Evaluating rules in statefull session with name {}",
                     kieSessionName);
        Map<String, FactHandle> factHandles = new HashMap<String, FactHandle>();
        KieSession kieSession = kieContainer.newKieSession(kieSessionName);
        for (Entry<String, Object> entry : parameters.entrySet()) {
            String inputKey = workItem.getId() + "_" + entry.getKey();

            factHandles.put(inputKey,
                            kieSession.insert(entry.getValue()));
        }
        int fired = kieSession.fireAllRules();
        logger.debug("{} rules fired",
                     fired);
        for (Entry<String, FactHandle> entry : factHandles.entrySet()) {

            Object object = kieSession.getObject(entry.getValue());
            String key = entry.getKey().replaceAll(workItem.getId() + "_",
                                                   "");
            results.put(key,
                        object);

            kieSession.delete(entry.getValue());
        }
        factHandles.clear();
    }

    protected void handleStateless(WorkItem workItem,
                                   String kieSessionName,
                                   Map<String, Object> parameters,
                                   Map<String, Object> results) {
        logger.debug("Evaluating rules in stateless session with name {}", kieSessionName);
        StatelessKieSession kieSession = kieContainer.newStatelessKieSession(kieSessionName);
        List<Command<?>> commands = new ArrayList<Command<?>>();

        for (Entry<String, Object> entry : parameters.entrySet()) {
            String inputKey = workItem.getId() + "_" + entry.getKey();

            commands.add(commandsFactory.newInsert(entry.getValue(),
                                                   inputKey,
                                                   true,
                                                   null));
        }
        commands.add(commandsFactory.newFireAllRules("Fired"));
        BatchExecutionCommand executionCommand = commandsFactory.newBatchExecution(commands);
        ExecutionResults executionResults = kieSession.execute(executionCommand);
        logger.debug("{} rules fired",
                     executionResults.getValue("Fired"));

        for (Entry<String, Object> entry : parameters.entrySet()) {
            String inputKey = workItem.getId() + "_" + entry.getKey();
            String key = entry.getKey().replaceAll(workItem.getId() + "_",
                                                   "");
            results.put(key,
                        executionResults.getValue(inputKey));
        }
    }

    protected void handleDMN(WorkItem workItem,
                             Map<String, Object> parameters,
                             Map<String, Object> results) {
        String namespace = (String) parameters.remove("Namespace");
        String model = (String) parameters.remove("Model");
        String decision = (String) parameters.remove("Decision");

        DMNRuntime runtime = kieContainer.newKieSession().getKieRuntime(DMNRuntime.class);
        DMNModel dmnModel = runtime.getModel(namespace,
                                             model);
        if (dmnModel == null) {
            throw new IllegalArgumentException("DMN model '" + model + "' not found with namespace '" + namespace + "'");
        }
        DMNResult dmnResult = null;
        DMNContext context = runtime.newContext();

        for (Entry<String, Object> entry : parameters.entrySet()) {
            context.set(entry.getKey(),
                        entry.getValue());
        }

        if (decision != null && !decision.isEmpty()) {
            dmnResult = runtime.evaluateDecisionByName(dmnModel,
                                                       decision,
                                                       context);
        } else {
            dmnResult = runtime.evaluateAll(dmnModel,
                                            context);
        }

        if (dmnResult.hasErrors()) {
            String errors = dmnResult.getMessages(Severity.ERROR).stream()
                    .map(message -> message.toString())
                    .collect(Collectors.joining(", "));

            throw new RuntimeException("DMN result errors:: " + errors);
        }

        // no class loader defined we don't even try to convert.
        if(classLoader == null || runtimeManager == null) {
            results.putAll(dmnResult.getContext().getAll());
            return;
        }

        RuntimeEngine engine = runtimeManager.getRuntimeEngine(ProcessInstanceIdContext.get(workItem.getProcessInstanceId()));
        try {
            KieSession localksession = engine.getKieSession();
            WorkflowProcessInstance processInstance = (WorkflowProcessInstance) localksession.getProcessInstance(workItem.getProcessInstanceId());
            WorkItemNodeInstance nodeInstance = findNodeInstance(workItem.getId(), processInstance);
            WorkItemNode workItemNode = (WorkItemNode) nodeInstance.getNode();

            // data outputs contains the structure refs for data association
            Map<String, String> dataTypeOutputs = (Map<String, String>) workItemNode.getMetaData("DataOutputs");
            Map<String, Object> decisionOutputData = dmnResult.getContext().getAll();
            Map<String, Object> outcome = new HashMap<>();

            if(!workItemNode.getOutAssociations().isEmpty()) {
                // if there is one out association but the data is multiple we collapse to an object
                for(DataAssociation dataAssociation : workItemNode.getOutAssociations()) {
                   String targetOutputTask = dataAssociation.getSources().get(0);
                   String targetTypeOutputTask = dataTypeOutputs.get(targetOutputTask);
                   outcome.put(targetOutputTask, typeTransformer.transform(decisionOutputData.get(targetOutputTask), targetTypeOutputTask));
                }
                results.putAll(outcome);
            } else {
                results.putAll(dmnResult.getContext().getAll());
            }
        } catch (IOException | ClassNotFoundException e) {
            results.putAll(dmnResult.getContext().getAll());
        } finally {
            runtimeManager.disposeRuntimeEngine(engine);
        }

    }


    public KieContainer getKieContainer() {
        return this.kieContainer;
    }
}
