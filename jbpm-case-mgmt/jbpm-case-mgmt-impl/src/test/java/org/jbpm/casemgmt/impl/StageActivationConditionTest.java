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
package org.jbpm.casemgmt.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.jbpm.casemgmt.api.model.instance.CaseInstance;
import org.jbpm.casemgmt.api.model.instance.CaseStageInstance;
import org.jbpm.casemgmt.impl.model.instance.CaseInstanceImpl;
import org.jbpm.casemgmt.impl.util.AbstractCaseServicesBaseTest;
import org.jbpm.services.api.model.NodeInstanceDesc;
import org.junit.Test;
import org.kie.api.command.ExecutableCommand;
import org.kie.api.runtime.Context;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.kie.api.runtime.query.QueryContext;
import org.kie.internal.command.RegistryContext;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;

public class StageActivationConditionTest extends AbstractCaseServicesBaseTest {

    private static final String STAGE_WITH_ACTIVATION_COND = "NoStartNodeAdhocCaseWithActivationCondition";
    private static final String STAGE_WITH_ACTIVATION_COND_NO_TERMINATE = "NoStartNodeAdhocCaseWithActivationConditionNoTerminate";


    @Override
    protected List<String> getProcessDefinitionFiles() {
        List<String> processes = new ArrayList<>();
        processes.add("cases/stage/activation/NoStartNodeAdhocCaseWithActivationCondition.bpmn2");
        processes.add("cases/stage/activation/NoStartNodeAdhocCaseWithActivationConditionNoTerminate.bpmn2");
        return processes;
    }
    
    @Test
    public void testReopenWithCaseFileReevaluation() {
        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), STAGE_WITH_ACTIVATION_COND_NO_TERMINATE);
        assertThat(caseId).isNotNull().isEqualTo(FIRST_CASE_ID);
        assertCaseInstanceActive(caseId);
        
        caseService.triggerAdHocFragment(caseId, "Adhoc 1", null);
        
        Collection<CaseStageInstance> activeStages = caseRuntimeDataService.getCaseInstanceStages(caseId, true, new QueryContext());
        assertThat(activeStages).hasSize(0);

        caseService.addDataToCaseFile(caseId, "readyToActivate", true);
        
        activeStages = caseRuntimeDataService.getCaseInstanceStages(caseId, true, new QueryContext());
        assertThat(activeStages).hasSize(1);
        
        caseService.addDataToCaseFile(caseId, "readyToComplete", true);
        
        activeStages = caseRuntimeDataService.getCaseInstanceStages(caseId, true, new QueryContext());
        assertThat(activeStages).hasSize(0);
        
        caseService.closeCase(caseId, "closing for test");
             
        caseService.reopenCase(caseId, deploymentUnit.getIdentifier(), STAGE_WITH_ACTIVATION_COND_NO_TERMINATE);
        
        activeStages = caseRuntimeDataService.getCaseInstanceStages(caseId, true, new QueryContext());
        assertThat(activeStages).hasSize(0);
        
        CaseInstance ci = caseService.getCaseInstance(caseId);
        
        Collection<NodeInstanceDesc> history = runtimeDataService.getProcessInstanceHistoryCompleted(((CaseInstanceImpl) ci).getProcessInstanceId(), new QueryContext());
        assertThat(history).hasSize(2);
        
        List<String> completedNodeNames = history.stream().map(ni -> ni.getName()).collect(Collectors.toList());
        assertThat(completedNodeNames).contains("stage", "end");
        
        caseService.closeCase(caseId, "closing for real");

        assertCaseInstanceNotActive(caseId);
    }

    @Test
    public void testAutoComplete() {
        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), STAGE_WITH_ACTIVATION_COND);
        assertThat(caseId).isNotNull().isEqualTo(FIRST_CASE_ID);
        assertCaseInstanceActive(caseId);
        
        Collection<CaseStageInstance> activeStages = caseRuntimeDataService.getCaseInstanceStages(caseId, true, new QueryContext());
        assertThat(activeStages).hasSize(0);

        caseService.addDataToCaseFile(caseId, "readyToActivate", true);
        
        activeStages = caseRuntimeDataService.getCaseInstanceStages(caseId, true, new QueryContext());
        assertThat(activeStages).hasSize(1);
        
        caseService.addDataToCaseFile(caseId, "readyToComplete", true);
             
        assertCaseInstanceNotActive(caseId);
    }
    
    @Test
    public void testAutoCompleteCheckActivationData() {
        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), STAGE_WITH_ACTIVATION_COND);
        assertThat(caseId).isNotNull().isEqualTo(FIRST_CASE_ID);
        assertCaseInstanceActive(caseId);
        
        Collection<CaseStageInstance> activeStages = caseRuntimeDataService.getCaseInstanceStages(caseId, true, new QueryContext());
        assertThat(activeStages).hasSize(0);

        caseService.addDataToCaseFile(caseId, "readyToActivate", true);
        
        activeStages = caseRuntimeDataService.getCaseInstanceStages(caseId, true, new QueryContext());
        assertThat(activeStages).hasSize(1);
        
        CaseInstance caseInstance = caseService.getCaseInstance(caseId);
        Long processInstanceId = ((CaseInstanceImpl)caseInstance).getProcessInstanceId();
        Boolean readyVar = processService.execute(deploymentUnit.getIdentifier(), 
                               ProcessInstanceIdContext.get(processInstanceId), new ExecutableCommand<Boolean>() {

                                private static final long serialVersionUID = 6953820688229804654L;

                                @Override
                                public Boolean execute(Context context) {
                                    KieSession ksession = ((RegistryContext) context).lookup( KieSession.class );
                                    
                                    WorkflowProcessInstance pi = (WorkflowProcessInstance) ksession.getProcessInstance(processInstanceId, true);
                                    NodeInstance ni = pi.getNodeInstances().iterator().next();
                                    
                                    return (Boolean) ni.getVariable("ready");
                                }
                            });
        assertNotNull("Ready variable should not be null", readyVar);
        assertTrue("Ready variable should be set to true", readyVar);
        
        caseService.addDataToCaseFile(caseId, "readyToComplete", true);
             
        assertCaseInstanceNotActive(caseId);
    }

}
