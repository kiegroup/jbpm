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

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.drools.core.common.InternalKnowledgeRuntime;
import org.drools.core.event.ProcessEventSupport;
import org.jbpm.casemgmt.api.CaseRuntimeDataService;
import org.jbpm.casemgmt.api.model.CaseDefinition;
import org.jbpm.casemgmt.api.model.instance.CaseFileInstance;
import org.jbpm.casemgmt.impl.event.CaseEventSupport;
import org.jbpm.casemgmt.impl.model.instance.CaseFileInstanceImpl;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.instance.InternalProcessRuntime;
import org.jbpm.process.instance.ProcessInstance;
import org.jbpm.services.api.ProcessService;
import org.kie.api.command.ExecutableCommand;
import org.kie.api.runtime.Context;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.KieRuntime;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.internal.KieInternalServices;
import org.kie.internal.command.RegistryContext;
import org.kie.internal.identity.IdentityProvider;
import org.kie.internal.process.CorrelationKey;
import org.kie.internal.process.CorrelationKeyFactory;
import org.kie.internal.runtime.manager.context.CaseContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReopenCaseCommand extends CaseCommand<Void> {
    
    private static final long serialVersionUID = 6811181095390934146L;

    private static final Logger logger = LoggerFactory.getLogger(ReopenCaseCommand.class);
    
    private static CorrelationKeyFactory correlationKeyFactory = KieInternalServices.Factory.get().newCorrelationKeyFactory();
    
    private String caseId;
    private String deploymentId;
    private String caseDefinitionId;
    private Map<String, Object> data;
    
    private transient ProcessService processService;
    private transient CaseRuntimeDataService caseRuntimeDataService;

    public ReopenCaseCommand(IdentityProvider identityProvider, String caseId, String deploymentId, String caseDefinitionId, Map<String, Object> data, ProcessService processService, CaseRuntimeDataService caseRuntimeDataService) {
        super(identityProvider);
        this.caseId = caseId;
        this.deploymentId = deploymentId;
        this.caseDefinitionId = caseDefinitionId;
        this.data = data;
        this.processService = processService;
        this.caseRuntimeDataService = caseRuntimeDataService;
    }

    @Override
    public Void execute(Context context) {
        
        CaseEventSupport caseEventSupport = getCaseEventSupport(context);
        
        KieSession ksession = ((RegistryContext) context).lookup( KieSession.class );
                               
        CaseFileInstance caseFile = getCaseFile(ksession, caseId);     
        
        caseEventSupport.fireBeforeCaseReopened(caseId, caseFile, deploymentId, caseDefinitionId, data);
        
        logger.debug("Updating case file in working memory");
        FactHandle factHandle = ksession.getFactHandle(caseFile);
        ksession.update(factHandle, caseFile);
        
        logger.debug("Starting process instance for case {} and case definition {}", caseId, caseDefinitionId);
        CorrelationKey correlationKey = correlationKeyFactory.newCorrelationKey(caseId);
        Map<String, Object> params = new HashMap<>();
        // set case id to allow it to use CaseContext when creating runtime engine
        params.put(EnvironmentName.CASE_ID, caseId);
        final Map<String, Object> caseData = caseFile.getData();
        caseEventSupport.fireAfterCaseDataAdded(caseFile.getCaseId(), caseFile, caseFile.getDefinitionId(), caseData);
        
        CaseDefinition definition = caseRuntimeDataService.getCase(deploymentId, caseDefinitionId);
        Set<String> varNames = definition.getProcessVariables();
        Map<String, Object> parameters = new HashMap<>(caseData);
        if(data != null) {
            parameters.putAll(data);
        }
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            if(varNames.contains(entry.getKey())) {
                params.put(entry.getKey(), entry.getValue());
            } else {
                params.put(VariableScope.CASE_FILE_PREFIX + entry.getKey(), entry.getValue());
            }
        }


        long processInstanceId = processService.startProcess(deploymentId, caseDefinitionId, correlationKey, params);
        
        ((CaseFileInstanceImpl)caseFile).setCaseReopenDate(new Date());
        if (data != null) {
            caseFile.addAll(data);
        }
        
        logger.debug("Removing case file from working memory to allow refiring of rules...");
        ksession.delete(factHandle);
        ksession.insert(caseFile);
        if (!caseData.isEmpty()) {
            processService.execute(deploymentId, CaseContext.get(caseId), new ExecutableCommand<Void>() {
    
                private static final long serialVersionUID = -7093369406457484236L;
    
                @Override
                public Void execute(Context context) {
                    KieSession ksession = ((RegistryContext) context).lookup( KieSession.class );
                    ProcessInstance pi = (ProcessInstance) ksession.getProcessInstance(processInstanceId);
                    if (pi != null) {
                        ProcessEventSupport processEventSupport = ((InternalProcessRuntime) ((InternalKnowledgeRuntime) ksession).getProcessRuntime()).getProcessEventSupport();
                        VariableScope variableScope = (VariableScope) pi.getContextContainer().getDefaultContext(VariableScope.VARIABLE_SCOPE);
                        for (Entry<String, Object> entry : caseData.entrySet()) {  
                            String name = varNames.contains(entry.getKey()) ? entry.getKey() : VariableScope.CASE_FILE_PREFIX + entry.getKey();
                            List<String> tags = variableScope == null ? Collections.emptyList() : variableScope.tags(name);
                            processEventSupport.fireBeforeVariableChanged(
                                name,
                                name,
                                null, entry.getValue(), 
                                tags,
                                pi,
                                (KieRuntime) ksession );
                            processEventSupport.fireAfterVariableChanged(
                                name,
                                name,
                                null, entry.getValue(), 
                                tags,
                                pi,
                                (KieRuntime) ksession );
                        }
                    }
                    return null;
                }
            });
        }
        logger.debug("Case {} successfully reopened (process instance id {})", caseId, processInstanceId);
        caseEventSupport.fireAfterCaseReopened(caseId, caseFile, deploymentId, caseDefinitionId, data, processInstanceId);
        return null;
    }
    
    public void setProcessService(ProcessService processService) {
        this.processService = processService;
    }

}
