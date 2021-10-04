/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.casemgmt.api.model.instance.CaseFileInstance;
import org.jbpm.casemgmt.impl.util.AbstractCaseServicesBaseTest;
import org.jbpm.services.api.model.ProcessInstanceWithVarsDesc;
import org.jbpm.services.api.model.UserTaskInstanceWithPotOwnerDesc;
import org.jbpm.services.api.query.model.QueryParam;
import org.jbpm.services.task.impl.model.UserImpl;
import org.junit.Test;
import org.kie.api.runtime.query.QueryContext;
import org.kie.api.task.model.OrganizationalEntity;

import static java.util.Collections.emptyList;
import static org.jbpm.services.api.query.model.QueryParam.equalsTo;
import static org.jbpm.services.api.query.model.QueryParam.list;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AdvanceCaseRuntimeDataServiceImplTest extends AbstractCaseServicesBaseTest {


    @Override
    protected List<String> getProcessDefinitionFiles() {
        List<String> processes = new ArrayList<String>();
        processes.add("cases/EmptyCase.bpmn2");
        processes.add("cases/UserTaskCase.bpmn2");
        return processes;
    }

    /*
     * Case instance queries
     */
    @Test
    public void testSearchByVariable() {
        Map<String, Object> data = new HashMap<>();
        data.put("s", "my first case");

        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), EMPTY_CASE_P_ID, data);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), EMPTY_CASE_P_ID, caseFile);

        List<QueryParam> vars = list(equalsTo("s", "my first case"));

        List<ProcessInstanceWithVarsDesc> process = advanceCaseRuntimeDataService.queryCaseByVariables(emptyList(), vars, new QueryContext());
        assertEquals(1, process.size());
        assertEquals("my first case", process.get(0).getExtraData().get("s"));

        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        caseService.cancelCase(caseId);

    }

    @Test
    public void testSearchUserByVariable() {
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl(USER));

        Map<String, Object> data = new HashMap<>();
        data.put("name", "my first case");
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), USER_TASK_CASE_P_ID, data, roleAssignments);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), USER_TASK_CASE_P_ID, caseFile);

        List<UserTaskInstanceWithPotOwnerDesc> process = advanceCaseRuntimeDataService.queryUserTasksByVariables(emptyList(), emptyList(), emptyList(), emptyList(), new QueryContext());
        assertEquals(1, process.size());
        assertEquals("my first case", process.get(0).getExtraData().get("name"));

        assertNotNull(caseId);
        assertEquals(HR_CASE_ID, caseId);
        caseService.cancelCase(caseId);

    }

    @Test
    public void queryCaseByVariablesAndTask() {
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl(USER));

        Map<String, Object> data = new HashMap<>();
        data.put("name", "my first case");
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), USER_TASK_CASE_P_ID, data, roleAssignments);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), USER_TASK_CASE_P_ID, caseFile);

        List<ProcessInstanceWithVarsDesc> process = advanceCaseRuntimeDataService.queryCaseByVariablesAndTask(emptyList(), emptyList(), emptyList(), emptyList(), new QueryContext());
        assertEquals(1, process.size());
        assertEquals("my first case", process.get(0).getExtraData().get("name"));

        assertNotNull(caseId);
        assertEquals(HR_CASE_ID, caseId);
        caseService.cancelCase(caseId);

    }

    @Test
    public void testQueryCaseByVariablesAndTaskByPotOwnersDefaultALL() {
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl(USER));

        Map<String, Object> data = new HashMap<>();
        data.put("name", "my first case");
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), USER_TASK_CASE_P_ID, data, roleAssignments);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), USER_TASK_CASE_P_ID, caseFile);

        List<ProcessInstanceWithVarsDesc> process = advanceCaseRuntimeDataService.queryCaseByVariablesAndTask(emptyList(), emptyList(), emptyList(), getPotOwnersQuery("ALL"), new QueryContext());
        assertTrue(process.isEmpty());

        caseService.cancelCase(caseId);
    }

    @Test
    public void testQueryCaseByVariablesAndTaskByPotOwnersDefaultANY() {
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl(USER));

        Map<String, Object> data = new HashMap<>();
        data.put("name", "my first case");
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), USER_TASK_CASE_P_ID, data, roleAssignments);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), USER_TASK_CASE_P_ID, caseFile);
        List<ProcessInstanceWithVarsDesc> process = advanceCaseRuntimeDataService.queryCaseByVariablesAndTask(emptyList(), emptyList(), emptyList(), getPotOwnersQuery("ANY"), new QueryContext());
        assertEquals(1, process.size());
        assertEquals("my first case", process.get(0).getExtraData().get("name"));

        assertNotNull(caseId);
        assertEquals(HR_CASE_ID, caseId);
        caseService.cancelCase(caseId);
    }

    @Test
    public void testQueryUserTasksByVariablesByPotOwnersDefaultALL() {
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl(USER));

        Map<String, Object> data = new HashMap<>();
        data.put("name", "my first case");
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), USER_TASK_CASE_P_ID, data, roleAssignments);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), USER_TASK_CASE_P_ID, caseFile);

        List<UserTaskInstanceWithPotOwnerDesc> process = advanceCaseRuntimeDataService.queryUserTasksByVariables(emptyList(), emptyList(), emptyList(), getPotOwnersQuery("ALL"), new QueryContext());
        assertTrue(process.isEmpty());

        caseService.cancelCase(caseId);
    }

    @Test
    public void testQueryUserTasksByVariablesByPotOwnersANY() {
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl(USER));

        Map<String, Object> data = new HashMap<>();
        data.put("name", "my first case");
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), USER_TASK_CASE_P_ID, data, roleAssignments);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), USER_TASK_CASE_P_ID, caseFile);
        List<UserTaskInstanceWithPotOwnerDesc> process = advanceCaseRuntimeDataService.queryUserTasksByVariables(emptyList(), emptyList(), emptyList(), getPotOwnersQuery("ANY"), new QueryContext());
        assertEquals(1, process.size());
        assertEquals("my first case", process.get(0).getExtraData().get("name"));

        assertNotNull(caseId);
        assertEquals(HR_CASE_ID, caseId);
        caseService.cancelCase(caseId);

    }

    private QueryParam getPotOwnersQuery(String operator) {
        return new QueryParam(null, operator, Arrays.asList("HR", "IT", USER));
    }

}