/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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
import static org.assertj.core.api.Assertions.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.jbpm.casemgmt.api.model.instance.CaseInstance;
import org.jbpm.casemgmt.api.model.instance.CaseStageInstance;
import org.jbpm.casemgmt.api.model.instance.StageStatus;
import org.jbpm.casemgmt.impl.model.instance.CaseInstanceImpl;
import org.jbpm.casemgmt.impl.util.AbstractCaseServicesBaseTest;
import org.jbpm.kie.services.impl.admin.commands.CancelNodeInstanceCommand;
import org.jbpm.services.api.model.NodeInstanceDesc;
import org.junit.Test;
import org.kie.api.runtime.query.QueryContext;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.query.QueryFilter;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;

public class StageCancelTest extends AbstractCaseServicesBaseTest {

    @Override
    protected List<String> getProcessDefinitionFiles() {
        List<String> processes = new ArrayList<>();
        processes.add("cases/CaseWithBoundaryTimerStage.bpmn2");
        processes.add("cases/NoStartNodeCaseWithBoundaryTimerStage.bpmn2");
        processes.add("cases/CaseMultiInstanceStage.bpmn2");
        return processes;
    }
    
    @Test
    public void testCancelStageForMultiInstanceStages() {
        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), MULTI_STAGE_CASE_P_ID);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        try {
            CaseInstance caseInstance = caseService.getCaseInstance(caseId);
            Long processInstanceId = ((CaseInstanceImpl)caseInstance).getProcessInstanceId();
            
            Map<String, Object> data = new HashMap<>();
            data.put("type", "one");
            caseService.triggerAdHocFragment(caseId, "Stage #{type}", data);
            data.put("type", "two");
            caseService.triggerAdHocFragment(caseId, "Stage #{type}", data);
            data.put("type", "three");
            caseService.triggerAdHocFragment(caseId, "Stage #{type}", data);
            
            Collection<NodeInstanceDesc> activeNodes = caseRuntimeDataService.getActiveNodesForCase(FIRST_CASE_ID, new QueryContext());
            assertNotNull(activeNodes);
            Assertions.assertThat(activeNodes).hasSize(3);
            
            Iterator<NodeInstanceDesc> activeStageNode = activeNodes.iterator();
            
            //Get all stages
            Collection<CaseStageInstance> allStages = caseRuntimeDataService.getCaseInstanceStages(caseId, false, new QueryContext());
            
            assertThat(allStages)
            .hasSize(4)
            .anyMatch(item -> item.getStatus().equals(StageStatus.Available))
            .noneMatch(item -> item.getStatus().equals(StageStatus.Canceled))
            .noneMatch(item -> item.getStatus().equals(StageStatus.Completed))
            .filteredOn(item -> item.getStatus().equals(StageStatus.Active)).hasSize(3);
            
            assertActiveStages(caseId, 3);
            
            //1. Cancel Stage one
            processService.execute(caseInstance.getDeploymentId(), ProcessInstanceIdContext.get(processInstanceId), 
                                   new CancelNodeInstanceCommand(processInstanceId, activeStageNode.next().getId()));
            
            //Get all stages
            allStages = caseRuntimeDataService.getCaseInstanceStages(caseId, false, new QueryContext());
            
            assertThat(allStages)
            .hasSize(4)
            .anyMatch(item -> item.getStatus().equals(StageStatus.Available))
            .anyMatch(item -> item.getStatus().equals(StageStatus.Canceled))
            .noneMatch(item -> item.getStatus().equals(StageStatus.Completed))
            .filteredOn(item -> item.getStatus().equals(StageStatus.Active)).hasSize(2);
            
            assertActiveStages(caseId, 2);
            
            //2. Complete Stage two
            caseService.triggerAdHocFragment(caseId, "Stage two", "Simple task", null);
            List<TaskSummary> tasks = runtimeDataService.getTasksAssignedAsPotentialOwner("john", new QueryFilter());
            assertThat(tasks).isNotNull().hasSize(1);
            assertTask(tasks.get(0), "john", "Simple task", Status.Reserved);

            userTaskService.completeAutoProgress(tasks.get(0).getId(), "john", new HashMap<>());

            //Get all stages
            allStages = caseRuntimeDataService.getCaseInstanceStages(caseId, false, new QueryContext());
            
            assertThat(allStages)
            .hasSize(4)
            .anyMatch(item -> item.getStatus().equals(StageStatus.Available))
            .anyMatch(item -> item.getStatus().equals(StageStatus.Active))
            .anyMatch(item -> item.getStatus().equals(StageStatus.Canceled))
            .anyMatch(item -> item.getStatus().equals(StageStatus.Completed));
            
            assertActiveStages(caseId, 1);
            
            //3. Cancel Stage three
            activeStageNode.next();
            processService.execute(caseInstance.getDeploymentId(), ProcessInstanceIdContext.get(processInstanceId), 
                                   new CancelNodeInstanceCommand(processInstanceId, activeStageNode.next().getId()));
            
            //Get all stages
            allStages = caseRuntimeDataService.getCaseInstanceStages(caseId, false, new QueryContext());
            
            assertThat(allStages)
            .hasSize(4)
            .anyMatch(item -> item.getStatus().equals(StageStatus.Available))
            .anyMatch(item -> item.getStatus().equals(StageStatus.Completed))
            .noneMatch(item -> item.getStatus().equals(StageStatus.Active))
            .filteredOn(item -> item.getStatus().equals(StageStatus.Canceled)).hasSize(2);
            
            assertActiveStages(caseId, 0);

        } catch (Exception e) {
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }

    
    @Test
    public void testCancelStageAutoComplete() {
        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), "CaseWithBoundaryTimerStage");
        try {
          testCancelStage(caseId);
        } catch (Exception e) {
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }
    
    @Test
    public void testCancelStageNoAutoComplete() {
        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), STAGE_WITH_BOUNDARY_EVENT_CONDITION);
        try {
          caseService.addDataToCaseFile(caseId, "readyToActivate", true);
          testCancelStage(caseId);
        } catch (Exception e) {
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }

    private void testCancelStage(String caseId) {
        Collection<CaseStageInstance> activeStages = caseRuntimeDataService.getCaseInstanceStages(caseId, true, new QueryContext());
        assertThat(activeStages).hasSize(1);
        
        CaseInstance caseInstance = caseService.getCaseInstance(caseId);
        Long processInstanceId = ((CaseInstanceImpl)caseInstance).getProcessInstanceId();
        
        Collection<NodeInstanceDesc> activeNodes = caseRuntimeDataService.getActiveNodesForCase(FIRST_CASE_ID, new QueryContext());
        assertNotNull(activeNodes);
        
        NodeInstanceDesc activeStage = activeNodes.iterator().next();
        
        //Cancel Stage node
        processService.execute(caseInstance.getDeploymentId(), ProcessInstanceIdContext.get(processInstanceId), 
                               new CancelNodeInstanceCommand(processInstanceId, activeStage.getId()));
        
        //Get all stages
        Collection<CaseStageInstance> allStages = caseRuntimeDataService.getCaseInstanceStages(caseId, false, new QueryContext());
        assertThat(allStages).hasSize(2);
        Iterator<CaseStageInstance> iterator = allStages.iterator();
        assertThat(iterator.next().getStatus()).isEqualTo(StageStatus.Canceled);
        assertThat(iterator.next().getStatus()).isEqualTo(StageStatus.Available);
        
        assertActiveStages(caseId, 0);
    }
    
    private void assertActiveStages(String caseId, int expectedActiveStages) {
        Collection<CaseStageInstance> activeStages = caseRuntimeDataService.getCaseInstanceStages(caseId, true, new QueryContext());
        assertThat(activeStages).hasSize(expectedActiveStages);
    }

}
