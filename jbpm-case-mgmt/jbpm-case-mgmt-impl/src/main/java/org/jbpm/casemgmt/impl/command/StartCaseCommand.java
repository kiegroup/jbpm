/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.casemgmt.impl.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.drools.core.definitions.rule.impl.RuleImpl;
import org.jbpm.casemgmt.api.CaseRuntimeDataService;
import org.jbpm.casemgmt.api.model.CaseDefinition;
import org.jbpm.casemgmt.api.model.instance.CaseFileInstance;
import org.jbpm.casemgmt.impl.event.CaseEventSupport;
import org.jbpm.casemgmt.impl.model.instance.CaseFileInstanceImpl;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.services.api.ProcessService;
import org.kie.api.KieServices;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.command.ExecutableCommand;
import org.kie.api.command.KieCommands;
import org.kie.api.event.rule.DefaultAgendaEventListener;
import org.kie.api.event.rule.MatchCreatedEvent;
import org.kie.api.runtime.Context;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.KieInternalServices;
import org.kie.internal.command.RegistryContext;
import org.kie.internal.identity.IdentityProvider;
import org.kie.internal.process.CorrelationKey;
import org.kie.internal.process.CorrelationKeyFactory;
import org.kie.internal.runtime.manager.context.CaseContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartCaseCommand extends CaseCommand<Void> {

    private static final long serialVersionUID = 6811181095390934146L;

    private static final Logger logger = LoggerFactory.getLogger(StartCaseCommand.class);

    private static CorrelationKeyFactory correlationKeyFactory = KieInternalServices.Factory.get().newCorrelationKeyFactory();
    private static KieCommands commandsFactory = KieServices.Factory.get().getCommands();

    private String caseId;
    private String deploymentId;
    private String caseDefinitionId;
    private CaseFileInstance caseFile;

    private transient ProcessService processService;
    private transient CaseRuntimeDataService caseRuntimeDataService;

    public StartCaseCommand(IdentityProvider identityProvider, String caseId, String deploymentId, String caseDefinitionId, CaseFileInstance caseFile, ProcessService processService, CaseRuntimeDataService caseRuntimeDataService) {
        super(identityProvider);
        this.caseId = caseId;
        this.deploymentId = deploymentId;
        this.caseDefinitionId = caseDefinitionId;
        this.caseFile = caseFile;
        this.processService = processService;
        this.caseRuntimeDataService = caseRuntimeDataService;
    }
    
    @Override
    public Void execute(Context context) {

        CaseEventSupport caseEventSupport = getCaseEventSupport(context);
        caseEventSupport.fireBeforeCaseStarted(caseId, deploymentId, caseDefinitionId, caseFile);
        Map<String, List<Object>> capturedEvents = new HashMap<>();
        logger.debug("Inserting case file into working memory");
        List<Command<?>> commands = new ArrayList<>();
        commands.add(new ExecutableCommand<Void>() {

            private static final long serialVersionUID = 8516665043577142587L;

            @Override
            public Void execute(Context context) {
                KieSession ksession = ((RegistryContext) context).lookup(KieSession.class);

                ksession.addEventListener(new DefaultAgendaEventListener() {

                    public void matchCreated(MatchCreatedEvent event) {
                        String ruleFlowGroup = ((RuleImpl) event.getMatch().getRule()).getRuleFlowGroup();
                        if ("DROOLS_SYSTEM".equals(ruleFlowGroup)) {
                            String ruleName = event.getMatch().getRule().getName();
                            if (ruleName.startsWith("RuleFlow-AdHocActivate-")) {

                                List<Object> events = capturedEvents.get(ruleName);
                                if (events == null) {
                                    events = new ArrayList<>();
                                    capturedEvents.put(ruleName, events);
                                }
                                events.add(event);
                            }
                        }
                    }
                });
                return null;
            }
        });
        commands.add(commandsFactory.newInsert(caseFile));
        commands.add(commandsFactory.newFireAllRules());
        BatchExecutionCommand batch = commandsFactory.newBatchExecution(commands);
        processService.execute(deploymentId, CaseContext.get(caseId), batch);
        logger.debug("Starting process instance for case {} and case definition {}", caseId, caseDefinitionId);
        CorrelationKey correlationKey = correlationKeyFactory.newCorrelationKey(caseId);
        Map<String, Object> params = new HashMap<>();
        // add process parent
        if (caseFile instanceof CaseFileInstanceImpl && ((CaseFileInstanceImpl) caseFile).getParentInstanceId() != null && ((CaseFileInstanceImpl) caseFile).getParentInstanceId() >= 0) {
            params.put("ParentInstanceId", ((CaseFileInstanceImpl) caseFile).getParentInstanceId());
        }
        // set case id to allow it to use CaseContext when creating runtime engine
        params.put(EnvironmentName.CASE_ID, caseId);
        final Map<String, Object> caseData = caseFile.getData();

        CaseDefinition definition = caseRuntimeDataService.getCase(deploymentId, caseDefinitionId);
        Set<String> varNames = definition.getProcessVariables();
        for (Map.Entry<String, Object> entry : caseData.entrySet()) {
            
            if(varNames.contains(entry.getKey())) {
                params.put(entry.getKey(), entry.getValue());
            } else {
                params.put(VariableScope.CASE_FILE_PREFIX + entry.getKey(), entry.getValue());
            }
        }

        final long processInstanceId = processService.startProcess(deploymentId, caseDefinitionId, correlationKey, params);
        logger.debug("Case {} successfully started (process instance id {})", caseId, processInstanceId);        

        processService.execute(deploymentId, CaseContext.get(caseId), new ExecutableCommand<Void>() {

            private static final long serialVersionUID = -7093369406457484236L;

            @Override
            public Void execute(Context context) {
                KieSession ksession = ((RegistryContext) context).lookup(KieSession.class);
                ProcessInstance pi = (ProcessInstance) ksession.getProcessInstance(processInstanceId);
                if (pi != null) {

                    for (Entry<String, List<Object>> entry : capturedEvents.entrySet()) {
                        for (Object event : entry.getValue()) {
                            pi.signalEvent(entry.getKey(), event);
                        }
                    }
                }
                return null;
            }
        });
        caseEventSupport.fireAfterCaseStarted(caseId, deploymentId, caseDefinitionId, caseFile, processInstanceId);
        return null;
    }

    public void setProcessService(ProcessService processService) {
        this.processService = processService;
    }

}
