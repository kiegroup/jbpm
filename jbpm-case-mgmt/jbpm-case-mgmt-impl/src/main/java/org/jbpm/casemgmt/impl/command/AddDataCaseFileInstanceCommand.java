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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.drools.core.ClassObjectFilter;
import org.drools.core.common.InternalKnowledgeRuntime;
import org.drools.core.event.ProcessEventSupport;
import org.jbpm.casemgmt.api.CaseRuntimeDataService;
import org.jbpm.casemgmt.api.auth.AuthorizationManager;
import org.jbpm.casemgmt.api.model.instance.CaseFileInstance;
import org.jbpm.casemgmt.impl.event.CaseEventSupport;
import org.jbpm.casemgmt.impl.model.instance.CaseFileInstanceImpl;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.core.context.variable.VariableViolationException;
import org.jbpm.process.instance.InternalProcessRuntime;
import org.jbpm.services.api.ProcessService;
import org.jbpm.workflow.instance.WorkflowProcessInstance;
import org.kie.api.command.ExecutableCommand;
import org.kie.api.runtime.Context;
import org.kie.api.runtime.KieRuntime;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.internal.command.RegistryContext;
import org.kie.internal.identity.IdentityProvider;
import org.kie.internal.runtime.manager.context.CaseContext;

/**
 * Updates case file with new data
 */
public class AddDataCaseFileInstanceCommand extends CaseCommand<Void> {

    private static final long serialVersionUID = 6345222909719335953L;

    private String deploymentId;
    private Long processInstanceId;
    private Map<String, Object> parameters;
    private AuthorizationManager authorizationManager;
    
    private List<String> accessRestriction;
    
    private transient ProcessService processService;
    private transient CaseRuntimeDataService caseRuntimeDataService;
    public AddDataCaseFileInstanceCommand(String deploymentId, Long processInstanceId, IdentityProvider identityProvider, Map<String, Object> parameters, List<String> accessRestriction, AuthorizationManager authorizationManager, ProcessService processService, CaseRuntimeDataService caseRuntimeDataService) {
        super(identityProvider);
        this.deploymentId = deploymentId;
        this.processInstanceId = processInstanceId;
        this.parameters = parameters;   
        this.authorizationManager = authorizationManager;
        this.accessRestriction = accessRestriction;
        this.processService = processService;
        this.caseRuntimeDataService = caseRuntimeDataService;
    }

    @Override
    public Void execute(Context context) {        
        
        KieSession ksession = ((RegistryContext) context).lookup( KieSession.class );
        
        Collection<? extends Object> caseFiles = ksession.getObjects(new ClassObjectFilter(CaseFileInstance.class));
        if (caseFiles.size() != 1) {
            throw new IllegalStateException("Not able to find distinct case file - found case files " + caseFiles.size());
        }
        CaseFileInstance caseFile = (CaseFileInstance) caseFiles.iterator().next();
        // apply authorization
        authorizationManager.checkDataAuthorization(caseFile.getCaseId(), caseFile, parameters.keySet());
        
        // check read only variables, due to dependencies between firexxxCaseData methods, we need this logic here, we cannot reuse VariableScopeInstance.setVariable
        org.jbpm.process.instance.ProcessInstance pi = (org.jbpm.process.instance.ProcessInstance) ksession.getProcessInstance(processInstanceId);
        Set<String> processVariables = caseRuntimeDataService.getCase(pi.getDeploymentId(), pi.getProcessId()).getProcessVariables();

        VariableScope variableScope = (VariableScope) pi.getContextContainer().getDefaultContext(VariableScope.VARIABLE_SCOPE);
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            Object oldValue = caseFile.getData(entry.getKey());
            if (oldValue != null && !oldValue.equals(entry.getValue()) && variableScope.isReadOnly(processVariables.contains(entry.getKey()) ? entry.getKey() : VariableScope.CASE_FILE_PREFIX + entry.getKey())) {
                throw new VariableViolationException(pi.getId(), entry.getKey(), "variable is read only and cannot be replaced");
            }
        }

        FactHandle factHandle = ksession.getFactHandle(caseFile);
        
        CaseEventSupport caseEventSupport = getCaseEventSupport(context);
        caseEventSupport.fireBeforeCaseDataAdded(caseFile.getCaseId(), caseFile, caseFile.getDefinitionId(), parameters);
        caseFile.addAll(parameters);
        
        // setup data restriction if any are given
        for (String name : parameters.keySet()) {
            if (accessRestriction != null) {
                ((CaseFileInstanceImpl) caseFile).addDataAccessRestriction(name, accessRestriction);
            } else {
                ((CaseFileInstanceImpl) caseFile).removeDataAccessRestriction(name);
            }
        }
                
        if (parameters != null && !parameters.isEmpty()) {
            processService.execute(deploymentId, CaseContext.get(caseFile.getCaseId()), new ExecutableCommand<Void>() {
    
                private static final long serialVersionUID = -7093369406457484236L;
    
                @Override
                public Void execute(Context context) {
                    KieSession ksession = ((RegistryContext) context).lookup( KieSession.class );
                    WorkflowProcessInstance pi = (WorkflowProcessInstance) ksession.getProcessInstance(processInstanceId);

                    if (pi != null) {
                        ProcessEventSupport processEventSupport = ((InternalProcessRuntime) ((InternalKnowledgeRuntime) ksession).getProcessRuntime()).getProcessEventSupport();
                        VariableScope variableScope = (VariableScope) pi.getContextContainer().getDefaultContext(VariableScope.VARIABLE_SCOPE);
                        for (Entry<String, Object> entry : parameters.entrySet()) {
                            if(processVariables.contains(entry.getKey())) {
                                pi.setVariable(entry.getKey(), entry.getValue());
                            } else {
                                String name = VariableScope.CASE_FILE_PREFIX + entry.getKey();
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
                    }
                    return null;
                }
            });
        }
         
        ksession.update(factHandle, caseFile);
        triggerRules(ksession);
        caseEventSupport.fireAfterCaseDataAdded(caseFile.getCaseId(), caseFile, caseFile.getDefinitionId(), parameters);
        return null;
    }
}
