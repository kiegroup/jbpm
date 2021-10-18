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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.assertj.core.api.Assertions;
import org.drools.core.definitions.rule.impl.RuleImpl;
import org.drools.core.rule.GroupElement;
import org.drools.core.spi.Activation;
import org.drools.persistence.info.SessionInfo;
import org.jbpm.bpmn2.objects.Person;
import org.jbpm.casemgmt.api.AdHocFragmentNotFoundException;
import org.jbpm.casemgmt.api.CaseActiveException;
import org.jbpm.casemgmt.api.CaseCommentNotFoundException;
import org.jbpm.casemgmt.api.CaseDefinitionNotFoundException;
import org.jbpm.casemgmt.api.CaseNotFoundException;
import org.jbpm.casemgmt.api.auth.AuthorizationManager;
import org.jbpm.casemgmt.api.dynamic.TaskSpecification;
import org.jbpm.casemgmt.api.model.AdHocFragment;
import org.jbpm.casemgmt.api.model.CaseDefinition;
import org.jbpm.casemgmt.api.model.CaseFileItem;
import org.jbpm.casemgmt.api.model.CaseStage;
import org.jbpm.casemgmt.api.model.CaseStatus;
import org.jbpm.casemgmt.api.model.instance.CaseFileInstance;
import org.jbpm.casemgmt.api.model.instance.CaseInstance;
import org.jbpm.casemgmt.api.model.instance.CaseMilestoneInstance;
import org.jbpm.casemgmt.api.model.instance.CaseStageInstance;
import org.jbpm.casemgmt.api.model.instance.CommentInstance;
import org.jbpm.casemgmt.api.model.instance.CommentSortBy;
import org.jbpm.casemgmt.api.model.instance.MilestoneStatus;
import org.jbpm.casemgmt.api.model.instance.StageStatus;
import org.jbpm.casemgmt.impl.model.instance.CaseInstanceImpl;
import org.jbpm.casemgmt.impl.objects.EchoService;
import org.jbpm.casemgmt.impl.util.AbstractCaseServicesBaseTest;
import org.jbpm.casemgmt.impl.util.CountDownListenerFactory;
import org.jbpm.document.Document;
import org.jbpm.document.service.impl.DocumentImpl;
import org.jbpm.process.core.context.variable.VariableViolationException;
import org.jbpm.runtime.manager.impl.jpa.ContextMappingInfo;
import org.jbpm.services.api.ProcessInstanceNotFoundException;
import org.jbpm.services.api.TaskNotFoundException;
import org.jbpm.services.api.model.NodeInstanceDesc;
import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.jbpm.services.api.model.VariableDesc;
import org.jbpm.services.task.impl.model.GroupImpl;
import org.jbpm.services.task.impl.model.UserImpl;
import org.jbpm.test.listener.process.NodeLeftCountDownProcessEventListener;
import org.jbpm.workflow.instance.node.MilestoneNodeInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.kie.api.command.ExecutableCommand;
import org.kie.api.event.rule.MatchCreatedEvent;
import org.kie.api.runtime.Context;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.CaseAssignment;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.kie.api.runtime.query.QueryContext;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.KieInternalServices;
import org.kie.internal.command.RegistryContext;
import org.kie.internal.process.CorrelationKey;
import org.kie.internal.query.QueryFilter;
import org.kie.internal.runtime.conf.ObjectModel;
import org.kie.internal.runtime.manager.RuntimeManagerRegistry;
import org.kie.internal.runtime.manager.SessionNotFoundException;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CaseServiceImplTest extends AbstractCaseServicesBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(CaseServiceImplTest.class);

    private static final String NEW_RESTRICTED_VALUE = "new restricted value";
    
    private static final List<String> RESTRICTED_TESTS = Arrays.asList(
            "testCaseWithRestrictedCaseFileItem", 
            "testCaseWithRequiredRestrictedCaseFileItem");
    
    private static final List<String> VIOLATING_RESTRICTED_TESTS = Arrays.asList(
            "testCaseWithViolatingRestrictedCaseFileItem", 
            "testCaseReopenWithViolatingRestrictedCaseFileItem",
            "testAddDataCaseWithViolatingRestrictedCaseFileItem");

    @Rule
    public TestName name = new TestName();

    @Override
    protected List<String> getProcessDefinitionFiles() {
        List<String> processes = new ArrayList<String>();
        processes.add("cases/EmptyCase.bpmn2");
        processes.add("cases/UserTaskCase.bpmn2");
        processes.add("cases/UserTaskWithStageCase.bpmn2");
        processes.add("cases/UserTaskWithStageCaseAutoStart.bpmn2");
        processes.add("cases/UserStageAdhocCase.bpmn2");
        processes.add("cases/ScriptRoleAssignmentCase.bpmn2");
        processes.add("cases/NoStartNodeAdhocCase.bpmn2");
        processes.add("cases/CaseFileConditionalEvent.bpmn2");
        processes.add("cases/CaseWithRolesDefinition.bpmn2");
        processes.add("cases/CaseWithTwoStagesConditions.bpmn2");
        processes.add("cases/CaseWithExpressionOnCaseFileItem.bpmn2");
        processes.add("cases/UserTaskCaseDataRestrictions.bpmn2");
        processes.add("cases/InclusiveGatewayInDynamicCase.bpmn2");
        processes.add("cases/CaseMultiInstanceStage.bpmn2");
        processes.add("cases/UserTaskCaseData.bpmn2");
        processes.add("cases/CaseWithStageAndBoundaryTimer.bpmn2");
        processes.add("cases/CaseWithBoundaryTimerStage.bpmn2");
        processes.add("cases/NoStartNodeCaseWithBoundaryTimerStage.bpmn2");
        processes.add("cases/UserTaskCaseRequiredCaseFileItem.bpmn2");
        processes.add("cases/UserTaskCaseRestrictedCaseFileItem.bpmn2");
        processes.add("cases/UserTaskCaseRequiredRestrictedCaseFileItem.bpmn2");
        processes.add("cases/UserTaskCaseReadOnlyCaseFileItem.bpmn2");
        // add processes that can be used by cases but are not cases themselves
        processes.add("processes/DataVerificationProcess.bpmn2");
        processes.add("processes/DynamicSubProcess.bpmn2");
        return processes;
    }

    @Test
    public void testStartEmptyCase() {
        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), EMPTY_CASE_P_ID);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());

            caseService.cancelCase(caseId);
            CaseInstance instance = caseService.getCaseInstance(caseId);
            Assertions.assertThat(instance.getStatus()).isEqualTo(CaseStatus.CANCELLED.getId());
            caseId = null;
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }

    @Test
    public void testStartEmptyCaseAndDestroy() {
        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), EMPTY_CASE_P_ID);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());

            caseService.destroyCase(caseId);
            CaseInstance instance = caseService.getCaseInstance(caseId);
            Assertions.assertThat(instance.getStatus()).isEqualTo(CaseStatus.CANCELLED.getId());
            caseId = null;
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }

    @Test
    public void testStartEmptyCaseWithCaseFile() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "my first case");
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), EMPTY_CASE_P_ID, data);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), EMPTY_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId, true, false, false, false);
            assertNotNull(cInstance);
            assertEquals(FIRST_CASE_ID, cInstance.getCaseId());
            assertNotNull(cInstance.getCaseFile());
            assertEquals("my first case", cInstance.getCaseFile().getData("name"));
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());

            Collection<VariableDesc> vars = runtimeDataService.getVariablesCurrentState(((CaseInstanceImpl) cInstance).getProcessInstanceId());
            assertNotNull(vars);
            assertEquals(3, vars.size());
            Map<String, Object> mappedVars = vars.stream().collect(toMap(v -> v.getVariableId(), v -> v.getNewValue()));
            assertEquals("my first case", mappedVars.get("name"));
            assertEquals(FIRST_CASE_ID, mappedVars.get("CaseId"));
            assertEquals("john", mappedVars.get("initiator"));

            caseService.cancelCase(caseId);
            CaseInstance instance = caseService.getCaseInstance(caseId);
            Assertions.assertThat(instance.getStatus()).isEqualTo(CaseStatus.CANCELLED.getId());
            caseId = null;
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }

    @Test
    public void testStartEmptyCaseWithCaseFileAndDocument() {
        byte[] docContent = "first case document".getBytes();
        DocumentImpl document = new DocumentImpl(UUID.randomUUID().toString(), "test case doc", docContent.length, new Date());
        document.setContent(docContent);

        Map<String, Object> data = new HashMap<>();
        data.put("name", "my first case");
        data.put("document", document);
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), EMPTY_CASE_P_ID, data);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), EMPTY_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId, true, false, false, false);
            assertNotNull(cInstance);
            assertEquals(FIRST_CASE_ID, cInstance.getCaseId());
            assertNotNull(cInstance.getCaseFile());
            assertEquals("my first case", cInstance.getCaseFile().getData("name"));
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());

            Object doc = cInstance.getCaseFile().getData("document");
            assertNotNull(doc);
            assertTrue(doc instanceof Document);

            Document caseDoc = (Document) doc;
            assertEquals("test case doc", caseDoc.getName());
            assertEquals(docContent.length, caseDoc.getSize());
            assertEquals(new String(docContent), new String(caseDoc.getContent()));

            caseService.cancelCase(caseId);
            CaseInstance instance = caseService.getCaseInstance(caseId);
            Assertions.assertThat(instance.getStatus()).isEqualTo(CaseStatus.CANCELLED.getId());
            caseId = null;
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }

    @Test
    public void testAddUserTaskToEmptyCase() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "my first case");
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), EMPTY_CASE_P_ID, data);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), EMPTY_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals(FIRST_CASE_ID, cInstance.getCaseId());
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());

            // add dynamic user task to empty case instance - first by case id
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("variable", "#{name}");
            caseService.addDynamicTask(FIRST_CASE_ID, caseService.newHumanTaskSpec("First task", "test", "john", null, parameters));

            List<TaskSummary> tasks = runtimeDataService.getTasksAssignedAsPotentialOwner("john", new QueryFilter());
            assertNotNull(tasks);
            assertEquals(1, tasks.size());

            TaskSummary task = tasks.get(0);
            assertTask(task, "john", "First task", Status.Reserved);
            assertEquals("test", task.getDescription());

            Map<String, Object> inputs = userTaskService.getTaskInputContentByTaskId(task.getId());
            assertNotNull(inputs);
            assertEquals("my first case", inputs.get("variable"));

            String nameVar = (String) processService.getProcessInstanceVariable(task.getProcessInstanceId(), "name");
            assertNotNull(nameVar);
            assertEquals("my first case", nameVar);

            userTaskService.start(task.getId(), "john");
            Map<String, Object> outcome = new HashMap<>();
            outcome.put("name", "updated by dynamic task");
            userTaskService.complete(task.getId(), "john", outcome);

            nameVar = (String) processService.getProcessInstanceVariable(task.getProcessInstanceId(), "name");
            assertNotNull(nameVar);
            assertEquals("updated by dynamic task", nameVar);

            // second task add by process instance id
            Collection<ProcessInstanceDesc> caseProcessInstances = caseRuntimeDataService.getProcessInstancesForCase(caseId, new QueryContext());
            assertNotNull(caseProcessInstances);
            assertEquals(1, caseProcessInstances.size());

            ProcessInstanceDesc casePI = caseProcessInstances.iterator().next();
            assertNotNull(casePI);
            assertEquals(FIRST_CASE_ID, casePI.getCorrelationKey());

            caseService.addDynamicTask(casePI.getId(), caseService.newHumanTaskSpec("Second task", "another test", "mary", null, parameters));
            tasks = runtimeDataService.getTasksAssignedAsPotentialOwner("mary", new QueryFilter());
            assertNotNull(tasks);
            assertEquals(1, tasks.size());

            task = tasks.get(0);
            assertTask(task, "mary", "Second task", Status.Reserved);
            assertEquals("another test", task.getDescription());

            // User john cannot work with task assigned to mary
            try {
                userTaskService.start(task.getId(), "john");
            } catch (TaskNotFoundException e) {
                // expected
            }

            userTaskService.start(task.getId(), "mary");
            userTaskService.complete(task.getId(), "mary", null);

            Collection<NodeInstanceDesc> nodes = runtimeDataService.getProcessInstanceHistoryCompleted(casePI.getId(), new QueryContext());
            assertNotNull(nodes);

            assertEquals(4, nodes.size());

            Map<String, String> nodesByName = nodes.stream().collect(toMap(NodeInstanceDesc::getName, NodeInstanceDesc::getNodeType));
            assertTrue(nodesByName.containsKey("StartProcess"));
            assertTrue(nodesByName.containsKey("EndProcess"));
            assertTrue(nodesByName.containsKey("[Dynamic] First task"));
            assertTrue(nodesByName.containsKey("[Dynamic] Second task"));

            assertEquals("StartNode", nodesByName.get("StartProcess"));
            assertEquals("EndNode", nodesByName.get("EndProcess"));
            assertEquals("Human Task", nodesByName.get("[Dynamic] First task"));
            assertEquals("Human Task", nodesByName.get("[Dynamic] Second task"));
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }

    @Test
    public void testAddUserTaskToCaseWithStage() {
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl("john"));

        Map<String, Object> data = new HashMap<>();
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), USER_TASK_CASE_P_ID, data, roleAssignments);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), USER_TASK_STAGE_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals(FIRST_CASE_ID, cInstance.getCaseId());
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());

            CaseDefinition caseDef = caseRuntimeDataService.getCase(deploymentUnit.getIdentifier(), USER_TASK_STAGE_CASE_P_ID);
            assertNotNull(caseDef);
            assertEquals(1, caseDef.getCaseStages().size());
            assertEquals(deploymentUnit.getIdentifier(), caseDef.getDeploymentId());

            CaseStage stage = caseDef.getCaseStages().iterator().next();

            // add dynamic user task to empty case instance - first by case id
            Map<String, Object> parameters = new HashMap<>();
            caseService.addDynamicTaskToStage(FIRST_CASE_ID, stage.getId(), caseService.newHumanTaskSpec("First task", "test", "john", null, parameters));

            List<TaskSummary> tasks = runtimeDataService.getTasksAssignedAsPotentialOwner("john", new QueryFilter());
            assertNotNull(tasks);
            assertEquals(1, tasks.size());

            TaskSummary task = tasks.get(0);
            assertTask(task, "john", "First task", Status.Reserved);
            assertEquals("test", task.getDescription());

            // second task add by process instance id
            Collection<ProcessInstanceDesc> caseProcessInstances = caseRuntimeDataService.getProcessInstancesForCase(caseId, new QueryContext());
            assertNotNull(caseProcessInstances);
            assertEquals(1, caseProcessInstances.size());

            ProcessInstanceDesc casePI = caseProcessInstances.iterator().next();
            assertNotNull(casePI);
            assertEquals(FIRST_CASE_ID, casePI.getCorrelationKey());

            caseService.addDynamicTaskToStage(casePI.getId(), stage.getId(), caseService.newHumanTaskSpec("Second task", "another test", "mary", null, parameters));
            tasks = runtimeDataService.getTasksAssignedAsPotentialOwner("mary", new QueryFilter());
            assertNotNull(tasks);
            assertEquals(1, tasks.size());
            task = tasks.get(0);
            assertTask(task, "mary", "Second task", Status.Reserved);
            assertEquals("another test", task.getDescription());
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }

    @Test
    public void testAddServiceTaskToEmptyCase() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "my first case");
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), EMPTY_CASE_P_ID, data);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), EMPTY_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals(FIRST_CASE_ID, cInstance.getCaseId());
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());

            Collection<ProcessInstanceDesc> caseProcessInstances = caseRuntimeDataService.getProcessInstancesForCase(caseId, new QueryContext());
            assertNotNull(caseProcessInstances);
            assertEquals(1, caseProcessInstances.size());

            ProcessInstanceDesc casePI = caseProcessInstances.iterator().next();
            assertNotNull(casePI);
            assertEquals(FIRST_CASE_ID, casePI.getCorrelationKey());

            String nameVar = (String) processService.getProcessInstanceVariable(casePI.getId(), "name");
            assertNotNull(nameVar);
            assertEquals("my first case", nameVar);

            // add dynamic service task to empty case instance - first by case id
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("Interface", EchoService.class.getName());
            parameters.put("Operation", "echo");
            parameters.put("ParameterType", String.class.getName());
            parameters.put("Parameter", "testing dynamic service task");
            caseService.addDynamicTask(FIRST_CASE_ID, caseService.newTaskSpec("Service Task", "task 1", parameters));

            nameVar = (String) processService.getProcessInstanceVariable(casePI.getId(), "name");
            assertNotNull(nameVar);
            assertEquals("testing dynamic service task echoed by service", nameVar);

            // second dynamic service task add by process instance id    
            parameters.put("Parameter", "testing dynamic service task 2");
            caseService.addDynamicTask(casePI.getId(), caseService.newTaskSpec("Service Task", "task 2", parameters));

            nameVar = (String) processService.getProcessInstanceVariable(casePI.getId(), "name");
            assertNotNull(nameVar);
            assertEquals("testing dynamic service task 2 echoed by service", nameVar);

            Collection<NodeInstanceDesc> nodes = runtimeDataService.getProcessInstanceHistoryCompleted(casePI.getId(), new QueryContext());
            assertNotNull(nodes);

            assertEquals(4, nodes.size());

            Map<String, String> nodesByName = nodes.stream().collect(toMap(NodeInstanceDesc::getName, NodeInstanceDesc::getNodeType));
            assertTrue(nodesByName.containsKey("StartProcess"));
            assertTrue(nodesByName.containsKey("EndProcess"));
            assertTrue(nodesByName.containsKey("[Dynamic] task 1"));
            assertTrue(nodesByName.containsKey("[Dynamic] task 2"));

            assertEquals("StartNode", nodesByName.get("StartProcess"));
            assertEquals("EndNode", nodesByName.get("EndProcess"));
            assertEquals("Service Task", nodesByName.get("[Dynamic] task 1"));
            assertEquals("Service Task", nodesByName.get("[Dynamic] task 2"));
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }

    @Test
    public void testAddSubprocessToEmptyCase() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "my first case");
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), EMPTY_CASE_P_ID, data);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), EMPTY_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals(FIRST_CASE_ID, cInstance.getCaseId());
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());

            // add dynamic user task to empty case instance - first by case id
            Map<String, Object> parameters = new HashMap<>();
            caseService.addDynamicSubprocess(FIRST_CASE_ID, SUBPROCESS_P_ID, parameters);

            // second task add by process instance id
            Collection<ProcessInstanceDesc> caseProcessInstances = caseRuntimeDataService.getProcessInstancesForCase(caseId, new QueryContext());
            assertNotNull(caseProcessInstances);
            assertEquals(2, caseProcessInstances.size());

            ProcessInstanceDesc casePI = caseProcessInstances.iterator().next();
            assertNotNull(casePI);
            assertEquals(FIRST_CASE_ID, casePI.getCorrelationKey());

            Collection<NodeInstanceDesc> nodes = runtimeDataService.getProcessInstanceHistoryCompleted(casePI.getId(), new QueryContext());
            assertNotNull(nodes);

            assertEquals(3, nodes.size());

            Map<String, String> nodesByName = nodes.stream().collect(toMap(NodeInstanceDesc::getName, NodeInstanceDesc::getNodeType));
            assertTrue(nodesByName.containsKey("StartProcess"));
            assertTrue(nodesByName.containsKey("EndProcess"));
            assertTrue(nodesByName.containsKey("[Dynamic] Sub Process"));

            assertEquals("StartNode", nodesByName.get("StartProcess"));
            assertEquals("EndNode", nodesByName.get("EndProcess"));
            assertEquals("SubProcessNode", nodesByName.get("[Dynamic] Sub Process"));

            caseService.addDynamicSubprocess(casePI.getId(), SUBPROCESS_P_ID, parameters);

            // let's verify that there are three process instances related to this case
            caseProcessInstances = caseRuntimeDataService.getProcessInstancesForCase(caseId, new QueryContext());
            assertNotNull(caseProcessInstances);
            assertEquals(3, caseProcessInstances.size());
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }

    @Test
    public void testAddSubprocessToCaseWithStage() {
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl("john"));

        Map<String, Object> data = new HashMap<>();
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), USER_TASK_CASE_P_ID, data, roleAssignments);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), USER_TASK_STAGE_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals(FIRST_CASE_ID, cInstance.getCaseId());
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());

            CaseDefinition caseDef = caseRuntimeDataService.getCase(deploymentUnit.getIdentifier(), USER_TASK_STAGE_CASE_P_ID);
            assertNotNull(caseDef);
            assertEquals(1, caseDef.getCaseStages().size());
            assertEquals(deploymentUnit.getIdentifier(), caseDef.getDeploymentId());

            CaseStage stage = caseDef.getCaseStages().iterator().next();

            // add dynamic user task to empty case instance - first by case id
            Map<String, Object> parameters = new HashMap<>();
            caseService.addDynamicSubprocessToStage(FIRST_CASE_ID, stage.getId(), SUBPROCESS_P_ID, parameters);

            // second task add by process instance id
            Collection<ProcessInstanceDesc> caseProcessInstances = caseRuntimeDataService.getProcessInstancesForCase(caseId, new QueryContext());
            assertNotNull(caseProcessInstances);
            assertEquals(2, caseProcessInstances.size());

            ProcessInstanceDesc casePI = caseProcessInstances.iterator().next();
            assertNotNull(casePI);
            assertEquals(FIRST_CASE_ID, casePI.getCorrelationKey());

            caseService.addDynamicSubprocessToStage(casePI.getId(), stage.getId(), SUBPROCESS_P_ID, parameters);

            // let's verify that there are three process instances related to this case
            caseProcessInstances = caseRuntimeDataService.getProcessInstancesForCase(caseId, new QueryContext());
            assertNotNull(caseProcessInstances);
            assertEquals(3, caseProcessInstances.size());
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }

    @Test
    public void testPerCaseCleanup() throws InterruptedException {
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl("john"));

        Map<String, Object> data = new HashMap<>();
        data.put("s", "description");
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), USER_TASK_CASE_P_ID, data, roleAssignments);
        CaseDefinition caseDef = caseRuntimeDataService.getCase(deploymentUnit.getIdentifier(), USER_TASK_CASE_P_ID);
        assertNotNull(caseDef);
        assertEquals(deploymentUnit.getIdentifier(), caseDef.getDeploymentId());
        assertEquals(3, caseDef.getAdHocFragments().size());
        Map<String, AdHocFragment> mappedFragments = mapAdHocFragments(caseDef.getAdHocFragments());
        assertTrue(mappedFragments.containsKey("Hello2"));
        assertTrue(mappedFragments.containsKey("Milestone1"));
        assertTrue(mappedFragments.containsKey("Milestone2"));
        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), USER_TASK_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(HR_CASE_ID, caseId);
        Collection<ProcessInstanceDesc> desc = runtimeDataService.getProcessInstancesByDeploymentId(deploymentUnit.getIdentifier(), Arrays.asList(0,1,2,3,4), new QueryContext());
        List<Long> ids = desc.stream().map(e -> e.getId()).collect(Collectors.toList());
        Long pid = ids.get(0);
        CountDownLatch latch = new CountDownLatch(1);

        Runnable run = new Runnable() {
            public void run() {
                for(int i = 0; i <= 5; i++) {

                    RuntimeManager manager = RuntimeManagerRegistry.get().getManager(deploymentUnit.getIdentifier());
                    RuntimeEngine engine = manager.getRuntimeEngine(ProcessInstanceIdContext.get(pid));
                    try {
                        KieSession ksession = engine.getKieSession();
                        ((org.drools.core.process.instance.WorkItemManager) ksession.getWorkItemManager()).getWorkItem(1000000);
                    } catch(SessionNotFoundException e) {
                        throw new ProcessInstanceNotFoundException("Process instance with id " + pid + " was not found", e);
                    } finally {
                        manager.disposeRuntimeEngine(engine);
                    }
                    if (i == 3) {
                        try {
                            latch.await();
                        } catch (InterruptedException e) {
                            //ignore
                        }
                    }

                }
            };
        };

        Thread executor = new Thread(run);
        executor.start();

        processService.abortProcessInstance(pid);
        logger.info("cancelled");

        latch.countDown();

        executor.join();
        EntityManager em = this.emf.createEntityManager();
        List<ContextMappingInfo> contextMappingInfo = em.createQuery("SELECT o FROM ContextMappingInfo o", ContextMappingInfo.class).getResultList();
        logger.info("ContextMappingInfo found {}", contextMappingInfo.stream().map(ContextMappingInfo::toString).collect(Collectors.toList()));
        assertEquals(1, contextMappingInfo.size());

        List<SessionInfo> sessionsInfo = em.createQuery("SELECT o FROM SessionInfo o", SessionInfo.class).getResultList();
        logger.info("Sessions found {}", sessionsInfo.stream().map(SessionInfo::getId).collect(Collectors.toList()));
        assertEquals(1, sessionsInfo.size());
        em.close();
    }

    @Test
    public void testPerCaseEnsureCleanupInDisposeEngine() throws InterruptedException {
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl("john"));

        Map<String, Object> data = new HashMap<>();
        data.put("s", "description");
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), USER_TASK_CASE_P_ID, data, roleAssignments);
        CaseDefinition caseDef = caseRuntimeDataService.getCase(deploymentUnit.getIdentifier(), USER_TASK_CASE_P_ID);
        assertNotNull(caseDef);
        assertEquals(deploymentUnit.getIdentifier(), caseDef.getDeploymentId());
        assertEquals(3, caseDef.getAdHocFragments().size());
        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), USER_TASK_CASE_P_ID, caseFile);

        Collection<ProcessInstanceDesc> desc = runtimeDataService.getProcessInstancesByDeploymentId(deploymentUnit.getIdentifier(), Arrays.asList(0,1,2,3,4), new QueryContext());
        List<Long> ids = desc.stream().map(ProcessInstanceDesc::getId).collect(Collectors.toList());
        Long pid = ids.get(0);

        RuntimeManager manager = RuntimeManagerRegistry.get().getManager(deploymentUnit.getIdentifier());
        RuntimeEngine engine = manager.getRuntimeEngine(ProcessInstanceIdContext.get(pid));
        // no op so the runtime is not being init when lazy loading is used
        CountDownLatch latch = new CountDownLatch(1);
        manager.disposeRuntimeEngine(engine);
        try {
            Thread executor = new Thread(() -> {
                RuntimeEngine localEngine = manager.getRuntimeEngine(ProcessInstanceIdContext.get(pid));
                manager.disposeRuntimeEngine(localEngine);
                latch.countDown();
            });
            executor.start();
            latch.await(1000, TimeUnit.MILLISECONDS);
            assertEquals(0, latch.getCount());
        } finally {
            caseService.cancelCase(caseId);
        }

        logger.info("cancelled");
        EntityManager em = this.emf.createEntityManager();
        List<ContextMappingInfo> contextMappingInfo = em.createQuery("SELECT o FROM ContextMappingInfo o", ContextMappingInfo.class).getResultList();
        logger.info("ContextMappingInfo found {}", contextMappingInfo.stream().map(ContextMappingInfo::toString).collect(Collectors.toList()));
        assertEquals(1, contextMappingInfo.size());

        List<SessionInfo> sessionsInfo = em.createQuery("SELECT o FROM SessionInfo o", SessionInfo.class).getResultList();
        logger.info("Sessions found {}", sessionsInfo.stream().map(SessionInfo::getId).collect(Collectors.toList()));
        assertEquals(1, sessionsInfo.size());
        em.close();
    }

    @Test
    public void testTriggerTaskAndMilestoneInCase() {
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl("john"));

        Map<String, Object> data = new HashMap<>();
        data.put("s", "description");
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), USER_TASK_CASE_P_ID, data, roleAssignments);

        CaseDefinition caseDef = caseRuntimeDataService.getCase(deploymentUnit.getIdentifier(), USER_TASK_CASE_P_ID);
        assertNotNull(caseDef);
        assertEquals(deploymentUnit.getIdentifier(), caseDef.getDeploymentId());
        assertEquals(3, caseDef.getAdHocFragments().size());
        Map<String, AdHocFragment> mappedFragments = mapAdHocFragments(caseDef.getAdHocFragments());
        assertTrue(mappedFragments.containsKey("Hello2"));
        assertTrue(mappedFragments.containsKey("Milestone1"));
        assertTrue(mappedFragments.containsKey("Milestone2"));

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), USER_TASK_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(HR_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals(HR_CASE_ID, cInstance.getCaseId());
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());

            List<TaskSummary> tasks = runtimeDataService.getTasksAssignedAsPotentialOwner("john", new QueryFilter());
            assertNotNull(tasks);
            assertEquals(1, tasks.size());

            TaskSummary task = tasks.get(0);
            assertNotNull(task);
            assertEquals("Hello1", task.getName());
            assertEquals("john", task.getActualOwnerId());
            assertEquals(Status.Reserved, task.getStatus());

            // now let's trigger one (human task) fragment
            Map<String, Object> taskData = new HashMap<>();
            taskData.put("test", "value");
            taskData.put("fromVar", "#{s}");
            caseService.triggerAdHocFragment(HR_CASE_ID, "Hello2", taskData);

            tasks = runtimeDataService.getTasksAssignedAsPotentialOwner("john", new QueryFilter());
            assertNotNull(tasks);
            assertEquals(2, tasks.size());

            task = tasks.get(0);
            assertNotNull(task);
            assertEquals("Hello2", task.getName());
            assertEquals("john", task.getActualOwnerId());
            assertEquals(Status.Reserved, task.getStatus());

            Map<String, Object> taskInputs = userTaskService.getTaskInputContentByTaskId(task.getId());
            assertNotNull(taskInputs);
            assertTrue(taskInputs.containsKey("test"));
            assertTrue(taskInputs.containsKey("fromVar"));
            assertEquals("value", taskInputs.get("test"));
            assertEquals("description", taskInputs.get("fromVar"));

            task = tasks.get(1);
            assertNotNull(task);
            assertEquals("Hello1", task.getName());
            assertEquals("john", task.getActualOwnerId());
            assertEquals(Status.Reserved, task.getStatus());

            Collection<CaseMilestoneInstance> milestones = caseRuntimeDataService.getCaseInstanceMilestones(HR_CASE_ID, true, new QueryContext());
            assertNotNull(milestones);
            assertEquals(0, milestones.size());

            milestones = caseRuntimeDataService.getCaseInstanceMilestones(HR_CASE_ID, false, new QueryContext());
            assertNotNull(milestones);
            assertEquals(2, milestones.size());

            List<String> expectedMilestones = Arrays.asList("Milestone1", "Milestone2");
            for (CaseMilestoneInstance mi : milestones) {
                assertTrue("Expected milestone not found", expectedMilestones.contains(mi.getName()));
                assertEquals("Wrong milestone status", MilestoneStatus.Available, mi.getStatus());
                assertFalse("Should not be achieved", mi.isAchieved());
                assertNull("Achieved date should be null", mi.getAchievedAt());
            }

            // trigger milestone node
            caseService.triggerAdHocFragment(HR_CASE_ID, "Milestone1", null);

            milestones = caseRuntimeDataService.getCaseInstanceMilestones(HR_CASE_ID, true, new QueryContext());
            assertNotNull(milestones);
            assertEquals(1, milestones.size());
            CaseMilestoneInstance msInstance = milestones.iterator().next();
            assertNotNull(msInstance);
            assertEquals("Milestone1", msInstance.getName());
            assertEquals(true, msInstance.isAchieved());
            assertNotNull(msInstance.getAchievedAt());

            // trigger another milestone node that has condition so it should not be achieved
            caseService.triggerAdHocFragment(HR_CASE_ID, "Milestone2", null);

            milestones = caseRuntimeDataService.getCaseInstanceMilestones(HR_CASE_ID, true, new QueryContext());
            assertNotNull(milestones);
            assertEquals(1, milestones.size());

            milestones = caseRuntimeDataService.getCaseInstanceMilestones(HR_CASE_ID, false, new QueryContext());
            assertNotNull(milestones);
            assertEquals(2, milestones.size());

            // add dataComplete to case file to achieve milestone
            caseService.addDataToCaseFile(HR_CASE_ID, "dataComplete", true);

            milestones = caseRuntimeDataService.getCaseInstanceMilestones(HR_CASE_ID, true, new QueryContext());
            assertNotNull(milestones);
            assertEquals(2, milestones.size());
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }
    
    @Test
    public void testTriggerTaskAndMilestoneInCaseFakeMatchCreated() {
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl("john"));

        Map<String, Object> data = new HashMap<>();
        data.put("s", "description");
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), USER_TASK_CASE_P_ID, data, roleAssignments);

        CaseDefinition caseDef = caseRuntimeDataService.getCase(deploymentUnit.getIdentifier(), USER_TASK_CASE_P_ID);
        assertNotNull(caseDef);
        assertEquals(deploymentUnit.getIdentifier(), caseDef.getDeploymentId());
        assertEquals(3, caseDef.getAdHocFragments().size());
        Map<String, AdHocFragment> mappedFragments = mapAdHocFragments(caseDef.getAdHocFragments());
        assertTrue(mappedFragments.containsKey("Hello2"));
        assertTrue(mappedFragments.containsKey("Milestone1"));
        assertTrue(mappedFragments.containsKey("Milestone2"));

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), USER_TASK_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(HR_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals(HR_CASE_ID, cInstance.getCaseId());
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());

            List<TaskSummary> tasks = runtimeDataService.getTasksAssignedAsPotentialOwner("john", new QueryFilter());
            assertNotNull(tasks);
            assertEquals(1, tasks.size());

            TaskSummary task = tasks.get(0);
            assertNotNull(task);
            assertEquals("Hello1", task.getName());
            assertEquals("john", task.getActualOwnerId());
            assertEquals(Status.Reserved, task.getStatus());

            // now let's trigger one (human task) fragment
            Map<String, Object> taskData = new HashMap<>();
            taskData.put("test", "value");
            taskData.put("fromVar", "#{s}");
            caseService.triggerAdHocFragment(HR_CASE_ID, "Hello2", taskData);

            tasks = runtimeDataService.getTasksAssignedAsPotentialOwner("john", new QueryFilter());
            assertNotNull(tasks);
            assertEquals(2, tasks.size());

            task = tasks.get(0);
            assertNotNull(task);
            assertEquals("Hello2", task.getName());
            assertEquals("john", task.getActualOwnerId());
            assertEquals(Status.Reserved, task.getStatus());

            Map<String, Object> taskInputs = userTaskService.getTaskInputContentByTaskId(task.getId());
            assertNotNull(taskInputs);
            assertTrue(taskInputs.containsKey("test"));
            assertTrue(taskInputs.containsKey("fromVar"));
            assertEquals("value", taskInputs.get("test"));
            assertEquals("description", taskInputs.get("fromVar"));

            task = tasks.get(1);
            assertNotNull(task);
            assertEquals("Hello1", task.getName());
            assertEquals("john", task.getActualOwnerId());
            assertEquals(Status.Reserved, task.getStatus());

            Collection<CaseMilestoneInstance> milestones = caseRuntimeDataService.getCaseInstanceMilestones(HR_CASE_ID, true, new QueryContext());
            assertNotNull(milestones);
            assertEquals(0, milestones.size());

            milestones = caseRuntimeDataService.getCaseInstanceMilestones(HR_CASE_ID, false, new QueryContext());
            assertNotNull(milestones);
            assertEquals(2, milestones.size());

            List<String> expectedMilestones = Arrays.asList("Milestone1", "Milestone2");
            for (CaseMilestoneInstance mi : milestones) {
                assertTrue("Expected milestone not found", expectedMilestones.contains(mi.getName()));
                assertEquals("Wrong milestone status", MilestoneStatus.Available, mi.getStatus());
                assertFalse("Should not be achieved", mi.isAchieved());
                assertNull("Achieved date should be null", mi.getAchievedAt());
            }

            // trigger milestone node
            caseService.triggerAdHocFragment(HR_CASE_ID, "Milestone1", null);

            milestones = caseRuntimeDataService.getCaseInstanceMilestones(HR_CASE_ID, true, new QueryContext());
            assertNotNull(milestones);
            assertEquals(1, milestones.size());
            CaseMilestoneInstance msInstance = milestones.iterator().next();
            assertNotNull(msInstance);
            assertEquals("Milestone1", msInstance.getName());
            assertTrue(msInstance.isAchieved());
            assertNotNull(msInstance.getAchievedAt());

            // trigger another milestone node that has condition so it should not be achieved
            caseService.triggerAdHocFragment(HR_CASE_ID, "Milestone2", null);

            milestones = caseRuntimeDataService.getCaseInstanceMilestones(HR_CASE_ID, true, new QueryContext());
            assertNotNull(milestones);
            assertEquals(1, milestones.size());

            milestones = caseRuntimeDataService.getCaseInstanceMilestones(HR_CASE_ID, false, new QueryContext());
            assertNotNull(milestones);
            assertEquals(2, milestones.size());

            // now let's fact match created to see if active vs inactive activations achieve milestone
            String eventName = "RuleFlow-Milestone-" + caseDef.getId() + caseDef.getCaseMilestones().stream().filter(cm -> cm.getName().equals("Milestone2")).findFirst().get().getId().replaceFirst("_", "-");
            
            MatchCreatedEvent event = Mockito.mock(MatchCreatedEvent.class);
            Activation<?> activation = Mockito.mock(Activation.class);
            RuleImpl rule = Mockito.mock(RuleImpl.class);
            GroupElement groupElement = Mockito.mock(GroupElement.class);
                        
            Mockito.when(rule.getName()).thenReturn(eventName);
            Mockito.when(rule.getRuleFlowGroup()).thenReturn("DROOLS_SYSTEM");
            
            Mockito.when(groupElement.getOuterDeclarations()).thenReturn(Collections.emptyMap());
            
            Mockito.when(activation.getRule()).thenReturn(rule);
            Mockito.when(activation.isActive()).thenReturn(false);
            Mockito.when(activation.getSubRule()).thenReturn(groupElement);
            
            Mockito.when(event.getMatch()).thenReturn(activation);
            
            final long processInstanceId = ((CaseInstanceImpl)cInstance).getProcessInstanceId();
            
            processService.execute(caseDef.getDeploymentId(), ProcessInstanceIdContext.get(processInstanceId), new ExecutableCommand<Void>() {

                private static final long serialVersionUID = -8605689943374937006L;

                @Override
                public Void execute(Context context) {
                    KieSession ksession = ((RegistryContext) context).lookup( KieSession.class );
                    
                    MilestoneNodeInstance milestone = (MilestoneNodeInstance) ((WorkflowProcessInstance) ksession.getProcessInstance(processInstanceId)).getNodeInstances()
                                                                                                                                                        .stream()
                                                                                                                                                        .filter(ni -> ni.getNodeName().equals("Milestone2"))
                                                                                                                                                        .findFirst().get();
                    milestone.signalEvent(milestone.getActivationEventType(), event);
                    return null;
                }

            });
            
            milestones = caseRuntimeDataService.getCaseInstanceMilestones(HR_CASE_ID, true, new QueryContext());
            assertNotNull(milestones);
            assertEquals(1, milestones.size());
            
            // now let's make it an active activation which in turn should trigger milestone to be completed
            Mockito.when(activation.isActive()).thenReturn(true);
            processService.execute(caseDef.getDeploymentId(), ProcessInstanceIdContext.get(processInstanceId), new ExecutableCommand<Void>() {

                private static final long serialVersionUID = -8605689943374937006L;

                @Override
                public Void execute(Context context) {
                    KieSession ksession = ((RegistryContext) context).lookup( KieSession.class );
                    
                    MilestoneNodeInstance milestone = (MilestoneNodeInstance) ((WorkflowProcessInstance) ksession.getProcessInstance(processInstanceId)).getNodeInstances()
                                                                                                                                                        .stream()
                                                                                                                                                        .filter(ni -> ni.getNodeName().equals("Milestone2"))
                                                                                                                                                        .findFirst().get();
                    milestone.signalEvent(milestone.getActivationEventType(), event);
                    return null;
                }          
            });

            milestones = caseRuntimeDataService.getCaseInstanceMilestones(HR_CASE_ID, true, new QueryContext());
            assertNotNull(milestones);
            assertEquals(2, milestones.size());
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }

    @Test
    public void testCaseRolesWithDynamicTask() {
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl("john"));

        Map<String, Object> data = new HashMap<>();
        data.put("s", "description");
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), USER_TASK_CASE_P_ID, data, roleAssignments);

        CaseDefinition caseDef = caseRuntimeDataService.getCase(deploymentUnit.getIdentifier(), USER_TASK_CASE_P_ID);
        assertNotNull(caseDef);
        assertEquals(deploymentUnit.getIdentifier(), caseDef.getDeploymentId());
        assertEquals(3, caseDef.getAdHocFragments().size());
        Map<String, AdHocFragment> mappedFragments = mapAdHocFragments(caseDef.getAdHocFragments());
        assertTrue(mappedFragments.containsKey("Hello2"));
        assertEquals("HumanTaskNode", mappedFragments.get("Hello2").getType());
        assertTrue(mappedFragments.containsKey("Milestone1"));
        assertEquals("MilestoneNode", mappedFragments.get("Milestone1").getType());
        assertTrue(mappedFragments.containsKey("Milestone2"));
        assertEquals("MilestoneNode", mappedFragments.get("Milestone2").getType());

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), USER_TASK_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(HR_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals(HR_CASE_ID, cInstance.getCaseId());
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());

            caseService.assignToCaseRole(HR_CASE_ID, "contact", new UserImpl("mary"));

            List<TaskSummary> tasks = runtimeDataService.getTasksAssignedAsPotentialOwner("john", new QueryFilter());
            assertNotNull(tasks);
            assertEquals(1, tasks.size());
            assertTask(tasks.get(0), "john", "Hello1", Status.Reserved);

            caseService.addDynamicTask(HR_CASE_ID, caseService.newHumanTaskSpec("Second task", "another test", "contact", null, new HashMap<>()));
            tasks = runtimeDataService.getTasksAssignedAsPotentialOwner("mary", new QueryFilter());
            assertNotNull(tasks);
            assertEquals(1, tasks.size());
            assertTask(tasks.get(0), "mary", "Second task", Status.Reserved);

            // now let's another user to contact role
            caseService.assignToCaseRole(HR_CASE_ID, "contact", new UserImpl("john"));

            caseService.addDynamicTask(HR_CASE_ID, caseService.newHumanTaskSpec("Third task", "another test", "contact", null, new HashMap<>()));
            tasks = runtimeDataService.getTasksAssignedAsPotentialOwner("mary", new QueryFilter());
            assertNotNull(tasks);
            assertEquals(2, tasks.size());
            assertTask(tasks.get(0), null, "Third task", Status.Ready);
            assertTask(tasks.get(1), "mary", "Second task", Status.Reserved);
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }

    @Test
    public void testCaseWithStageAutoStartNodes() {
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl("john"));

        Map<String, Object> data = new HashMap<>();
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), USER_TASK_STAGE_AUTO_START_CASE_P_ID, data, roleAssignments);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), USER_TASK_STAGE_AUTO_START_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals(FIRST_CASE_ID, cInstance.getCaseId());
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());

            List<TaskSummary> tasks = runtimeDataService.getTasksAssignedAsPotentialOwner("john", new QueryFilter());
            assertNotNull(tasks);
            assertEquals(2, tasks.size());
            assertTask(tasks.get(0), "john", "Ask for input", Status.Reserved);
            assertTask(tasks.get(1), "john", "Missing data", Status.Reserved);
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }

    @Test
    public void testCaseWithComments() {
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl("john"));

        Map<String, Object> data = new HashMap<>();
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), USER_TASK_STAGE_AUTO_START_CASE_P_ID, data, roleAssignments);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), USER_TASK_STAGE_AUTO_START_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals(FIRST_CASE_ID, cInstance.getCaseId());
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());

            Collection<CommentInstance> caseComments = caseService.getCaseComments(FIRST_CASE_ID, new QueryContext());
            assertNotNull(caseComments);
            assertEquals(0, caseComments.size());

            String commentId = caseService.addCaseComment(FIRST_CASE_ID, "poul", "just a tiny comment");
            assertNotNull(commentId);

            caseComments = caseService.getCaseComments(FIRST_CASE_ID, new QueryContext());
            assertNotNull(caseComments);
            assertEquals(1, caseComments.size());

            CommentInstance comment = caseComments.iterator().next();
            assertComment(comment, "poul", "just a tiny comment");
            assertEquals(commentId, comment.getId());

            caseService.updateCaseComment(FIRST_CASE_ID, comment.getId(), comment.getAuthor(), "Updated " + comment.getComment());
            caseComments = caseService.getCaseComments(FIRST_CASE_ID, new QueryContext());
            assertNotNull(caseComments);
            assertEquals(1, caseComments.size());

            comment = caseComments.iterator().next();
            assertComment(comment, "poul", "Updated just a tiny comment");

            caseService.addCaseComment(FIRST_CASE_ID, "mary", "another comment");

            caseService.addCaseComment(FIRST_CASE_ID, "john", "third comment");

            caseComments = caseService.getCaseComments(FIRST_CASE_ID, new QueryContext());
            assertNotNull(caseComments);
            assertEquals(3, caseComments.size());

            Iterator<CommentInstance> it = caseComments.iterator();
            assertComment(it.next(), "poul", "Updated just a tiny comment");
            assertComment(it.next(), "mary", "another comment");
            assertComment(it.next(), "john", "third comment");

            caseComments = caseService.getCaseComments(FIRST_CASE_ID, CommentSortBy.Author, new QueryContext());
            assertNotNull(caseComments);
            assertEquals(3, caseComments.size());
            it = caseComments.iterator();
            assertComment(it.next(), "john", "third comment");
            assertComment(it.next(), "mary", "another comment");
            assertComment(it.next(), "poul", "Updated just a tiny comment");

            caseService.removeCaseComment(FIRST_CASE_ID, comment.getId());

            caseComments = caseService.getCaseComments(FIRST_CASE_ID, CommentSortBy.Author, new QueryContext());
            assertEquals(2, caseComments.size());
            it = caseComments.iterator();
            assertComment(it.next(), "john", "third comment");
            assertComment(it.next(), "mary", "another comment");
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }
    
    @Test
    public void testCaseWithCommentsPagination() {
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl("john"));

        Map<String, Object> data = new HashMap<>();
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), USER_TASK_STAGE_AUTO_START_CASE_P_ID, data, roleAssignments);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), USER_TASK_STAGE_AUTO_START_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals(FIRST_CASE_ID, cInstance.getCaseId());
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());
            
            for (int i = 0 ; i < 55 ; i++) {              
                caseService.addCaseComment(FIRST_CASE_ID, "anna", "comment" + i);                
            }
            
            int pageSize = 20;

            int firstPageOffset = 0 * pageSize;
            Collection<CommentInstance> firstPage = caseService.getCaseComments(FIRST_CASE_ID, new QueryContext(firstPageOffset, pageSize));
            assertNotNull(firstPage);
            assertEquals(20, firstPage.size());
            Iterator<CommentInstance> firstPageIter = firstPage.iterator();
            for (int i = 0 ; firstPageIter.hasNext() ; i++) {
                assertComment(firstPageIter.next(), "anna", "comment" + i);
            }
            
            int secondPageOffset = 1 * pageSize;
            Collection<CommentInstance> secondPage = caseService.getCaseComments(FIRST_CASE_ID, new QueryContext(secondPageOffset, pageSize));
            assertNotNull(secondPage);
            assertEquals(20, secondPage.size());
            Iterator<CommentInstance> secondPageIter = secondPage.iterator();
            for (int i = 20 ; secondPageIter.hasNext() ; i++) {
                assertComment(secondPageIter.next(), "anna", "comment" + i);
            }
            
            int thirdPageOffset = 2 * pageSize;
            Collection<CommentInstance> thirdPage = caseService.getCaseComments(FIRST_CASE_ID, new QueryContext(thirdPageOffset, pageSize));
            assertNotNull(thirdPage);
            assertEquals(15, thirdPage.size());            
            Iterator<CommentInstance> thirdPageIter = thirdPage.iterator();
            for (int i = 40 ; thirdPageIter.hasNext() ; i++) {
                assertComment(thirdPageIter.next(), "anna", "comment" + i);
            }
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }

    @Test
    public void testUpdateNotExistingCaseComment() {
        Map<String, Object> data = new HashMap<>();
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), USER_TASK_STAGE_AUTO_START_CASE_P_ID, data);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), USER_TASK_STAGE_AUTO_START_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        try {
            caseService.updateCaseComment(FIRST_CASE_ID, "not-existing-comment", "poul", "just a tiny comment");
            fail("Updating non-existent case comment should throw CaseCommentNotFoundException.");
        } catch (CaseCommentNotFoundException e) {
            // expected
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }

    @Test
    public void testRemoveNotExistingCaseComment() {
        Map<String, Object> data = new HashMap<>();
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), USER_TASK_STAGE_AUTO_START_CASE_P_ID, data);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), USER_TASK_STAGE_AUTO_START_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        try {
            caseService.removeCaseComment(FIRST_CASE_ID, "not-existing-comment");
            fail("Removing non-existent case comment should throw CaseCommentNotFoundException.");
        } catch (CaseCommentNotFoundException e) {
            // expected
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }

    @Test
    public void testStartCaseWithStageAndAdHocFragments() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "my first case");
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), USER_TASK_STAGE_ADHOC_CASE_P_ID, data);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), USER_TASK_STAGE_ADHOC_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        try {
            assertCaseInstance(caseId, "my first case");

            Collection<AdHocFragment> availableFragments = caseRuntimeDataService.getAdHocFragmentsForCase(caseId);
            assertEquals(2, availableFragments.size());

            Map<String, AdHocFragment> mapped = mapAdHocFragments(availableFragments);
            assertEquals("HumanTaskNode", mapped.get("Adhoc 1").getType());
            assertEquals("HumanTaskNode", mapped.get("Adhoc 2").getType());

            List<TaskSummary> tasks = runtimeDataService.getTasksAssignedAsPotentialOwner("john", new QueryFilter());
            assertNotNull(tasks);
            assertEquals(1, tasks.size());
            assertTask(tasks.get(0), "john", "Initial step", Status.Reserved);

            userTaskService.completeAutoProgress(tasks.get(0).getId(), "john", null);

            availableFragments = caseRuntimeDataService.getAdHocFragmentsForCase(caseId);
            assertEquals(4, availableFragments.size());
            mapped = mapAdHocFragments(availableFragments);
            assertEquals("HumanTaskNode", mapped.get("Adhoc 1").getType());
            assertEquals("HumanTaskNode", mapped.get("Adhoc 2").getType());
            assertEquals("HumanTaskNode", mapped.get("First").getType());
            assertEquals("HumanTaskNode", mapped.get("Second").getType());
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }

    @Test
    public void testStartThenReopenEmptyCase() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "my first case");
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), EMPTY_CASE_P_ID, data);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), EMPTY_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());
            assertEquals("my first case", cInstance.getCaseDescription());

            long firstCaseProcessInstanceId = ((CaseInstanceImpl) cInstance).getProcessInstanceId();

            caseService.cancelCase(caseId);
            CaseInstance instance = caseService.getCaseInstance(caseId);
            Assertions.assertThat(instance.getStatus()).isEqualTo(CaseStatus.CANCELLED.getId());

            caseService.reopenCase(caseId, deploymentUnit.getIdentifier(), EMPTY_CASE_P_ID);
            cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());
            assertEquals("my first case", cInstance.getCaseDescription());

            long secondCaseProcessInstanceId = ((CaseInstanceImpl) cInstance).getProcessInstanceId();

            assertTrue(secondCaseProcessInstanceId > firstCaseProcessInstanceId);

            caseService.destroyCase(caseId);
            caseId = null;
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }

    @Test
    public void testStartThenReopenEmptyCaseUpdateDescription() {
        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), EMPTY_CASE_P_ID);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());
            assertEquals("#{name}", cInstance.getCaseDescription());

            long firstCaseProcessInstanceId = ((CaseInstanceImpl) cInstance).getProcessInstanceId();

            caseService.cancelCase(caseId);
            CaseInstance instance = caseService.getCaseInstance(caseId);
            Assertions.assertThat(instance.getStatus()).isEqualTo(CaseStatus.CANCELLED.getId());
            Map<String, Object> data = new HashMap<>();
            data.put("name", "my first case");

            caseService.reopenCase(caseId, deploymentUnit.getIdentifier(), EMPTY_CASE_P_ID, data);
            cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());
            assertEquals("my first case", cInstance.getCaseDescription());

            long secondCaseProcessInstanceId = ((CaseInstanceImpl) cInstance).getProcessInstanceId();

            assertTrue(secondCaseProcessInstanceId > firstCaseProcessInstanceId);

            caseService.destroyCase(caseId);
            caseId = null;
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }

    @Test
    public void testStartThenReopenActiveCase() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "my first case");
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), EMPTY_CASE_P_ID, data);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), EMPTY_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());
            assertEquals("my first case", cInstance.getCaseDescription());

            try {
                caseService.reopenCase(caseId, deploymentUnit.getIdentifier(), EMPTY_CASE_P_ID);
                fail("Not allowed to reopen active case");
            } catch (CaseActiveException e) {
                // expected
            }
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }

    @Test
    public void testStartThenReopenDestroyedCase() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "my first case");
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), EMPTY_CASE_P_ID, data);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), EMPTY_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());
            assertEquals("my first case", cInstance.getCaseDescription());

            caseService.destroyCase(caseId);

            try {
                caseService.reopenCase(caseId, deploymentUnit.getIdentifier(), EMPTY_CASE_P_ID);
                fail("Not allowed to reopen destroyed case");
            } catch (CaseNotFoundException e) {
                // expected
            }
            caseId = null;
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }
    
    @Test
    public void testStartThenReopenEmptyCaseUpdateData() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "my first case");
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), EMPTY_CASE_P_ID, data);

        
        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), EMPTY_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());
            assertEquals("my first case", cInstance.getCaseDescription());

            long firstCaseProcessInstanceId = ((CaseInstanceImpl) cInstance).getProcessInstanceId();
            
            Collection<CaseFileItem> caseFileItems = caseRuntimeDataService.getCaseInstanceDataItems(caseId, new QueryContext());
            assertNotNull(caseFileItems);
            assertEquals(1, caseFileItems.size());
            
            CaseFileItem fileItem = caseFileItems.iterator().next();
            assertNotNull(fileItem);
            assertEquals("name", fileItem.getName());
            assertEquals("my first case", fileItem.getValue());
            
            Collection<VariableDesc> vars = runtimeDataService.getVariablesCurrentState(((CaseInstanceImpl) cInstance).getProcessInstanceId());
            assertNotNull(vars);
            assertEquals(3, vars.size());
            Map<String, Object> mappedVars = vars.stream().collect(toMap(v -> v.getVariableId(), v -> v.getNewValue()));
            assertEquals("my first case", mappedVars.get("name"));
            assertEquals(FIRST_CASE_ID, mappedVars.get("CaseId"));
            assertEquals("john", mappedVars.get("initiator"));

            caseService.cancelCase(caseId);
            CaseInstance instance = caseService.getCaseInstance(caseId);
            Assertions.assertThat(instance.getStatus()).isEqualTo(CaseStatus.CANCELLED.getId());
            data = new HashMap<>();
            data.put("name", "my first case reopened");            

            caseService.reopenCase(caseId, deploymentUnit.getIdentifier(), EMPTY_CASE_P_ID, data);
            cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());
            assertEquals("my first case reopened", cInstance.getCaseDescription());

            long secondCaseProcessInstanceId = ((CaseInstanceImpl) cInstance).getProcessInstanceId();
            assertTrue(secondCaseProcessInstanceId > firstCaseProcessInstanceId);
            
            caseFileItems = caseRuntimeDataService.getCaseInstanceDataItems(caseId, new QueryContext());
            assertNotNull(caseFileItems);
            assertEquals(1, caseFileItems.size());
            
            fileItem = caseFileItems.iterator().next();
            assertNotNull(fileItem);
            assertEquals("name", fileItem.getName());
            assertEquals("my first case reopened", fileItem.getValue());
            
            vars = runtimeDataService.getVariablesCurrentState(secondCaseProcessInstanceId);
            assertNotNull(vars);
            assertEquals(3, vars.size());
            mappedVars = vars.stream().collect(toMap(v -> v.getVariableId(), v -> v.getNewValue()));
            assertEquals("my first case reopened", mappedVars.get("name"));
            assertEquals(FIRST_CASE_ID, mappedVars.get("CaseId"));
            assertEquals("john", mappedVars.get("initiator"));
            
            caseService.addDataToCaseFile(caseId, "name", "my first case updated");
            
            caseFileItems = caseRuntimeDataService.getCaseInstanceDataItems(caseId, new QueryContext());
            assertNotNull(caseFileItems);
            assertEquals(1, caseFileItems.size());
            
            fileItem = caseFileItems.iterator().next();
            assertNotNull(fileItem);
            assertEquals("name", fileItem.getName());
            assertEquals("my first case updated", fileItem.getValue());
            
            vars = runtimeDataService.getVariablesCurrentState(secondCaseProcessInstanceId);
            assertNotNull(vars);
            assertEquals(3, vars.size());
            mappedVars = vars.stream().collect(toMap(v -> v.getVariableId(), v -> v.getNewValue()));
            assertEquals("my first case updated", mappedVars.get("name"));
            assertEquals(FIRST_CASE_ID, mappedVars.get("CaseId"));
            assertEquals("john", mappedVars.get("initiator"));

            caseService.destroyCase(caseId);
            caseId = null;
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }

    @Test
    public void testStartScriptRoleAssignmentCase() {
        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), "ScriptRoleAssignmentCase");
        assertNotNull(caseId);
        assertEquals(HR_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());

            List<TaskSummary> tasks = runtimeDataService.getTasksAssignedAsPotentialOwner("john", new QueryFilter());
            assertNotNull(tasks);
            assertEquals(1, tasks.size());

            TaskSummary task = tasks.get(0);
            assertEquals("User Task 1", task.getName());
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }

    @Test
    public void testStartCaseWithoutStartNode() {
        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), NO_START_NODE_CASE_P_ID);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());

            Collection<NodeInstanceDesc> activeNodes = runtimeDataService.getProcessInstanceHistoryActive(((CaseInstanceImpl) cInstance).getProcessInstanceId(), new org.kie.api.runtime.query.QueryContext());
            assertNotNull(activeNodes);
            assertEquals(1, activeNodes.size());

            NodeInstanceDesc active = activeNodes.iterator().next();
            assertEquals("Initial step", active.getName());

            List<TaskSummary> tasks = runtimeDataService.getTasksAssignedAsPotentialOwner("john", new QueryFilter());
            assertNotNull(tasks);
            assertEquals(1, tasks.size());

            TaskSummary task = tasks.get(0);

            userTaskService.completeAutoProgress(task.getId(), "john", new HashMap<>());
            activeNodes = runtimeDataService.getProcessInstanceHistoryActive(((CaseInstanceImpl) cInstance).getProcessInstanceId(), new org.kie.api.runtime.query.QueryContext());
            assertNotNull(activeNodes);
            assertEquals(1, activeNodes.size());

            active = activeNodes.iterator().next();
            assertEquals("stage", active.getName());

            caseService.cancelCase(caseId);
            CaseInstance instance = caseService.getCaseInstance(caseId);
            Assertions.assertThat(instance.getStatus()).isEqualTo(CaseStatus.CANCELLED.getId());
            caseId = null;
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }

    @Test
    public void testStartEmptyCaseViaProcessService() {
        String caseId = FIRST_CASE_ID;

        CorrelationKey correlationKey = KieInternalServices.Factory.get().newCorrelationKeyFactory().newCorrelationKey(caseId);
        Map<String, Object> params = new HashMap<>();
        params.put("name", "my case via process service");
        Long processInstanceId = processService.startProcess(deploymentUnit.getIdentifier(), EMPTY_CASE_P_ID, correlationKey, params);

        assertNotNull(processInstanceId);

        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());
            assertEquals(caseId, cInstance.getCaseId());

            caseService.cancelCase(caseId);
            CaseInstance instance = caseService.getCaseInstance(caseId);
            Assertions.assertThat(instance.getStatus()).isEqualTo(CaseStatus.CANCELLED.getId());
            caseId = null;
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }

    @Test
    public void testStartEmptyCaseViaProcessServiceWithoutCorrelationKey() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "my case via process service");
        Long processInstanceId = processService.startProcess(deploymentUnit.getIdentifier(), EMPTY_CASE_P_ID, params);
        assertNotNull(processInstanceId);

        String caseId = processInstanceId.toString();

        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());
            assertEquals(caseId, cInstance.getCaseId());

            caseService.cancelCase(caseId);
            CaseInstance instance = caseService.getCaseInstance(caseId);
            Assertions.assertThat(instance.getStatus()).isEqualTo(CaseStatus.CANCELLED.getId());
            caseId = null;
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }

    @Test
    public void testStartEmptyCaseViaProcessServiceWithoutCorrelationKeyWithMilestones() {
        Map<String, Object> params = new HashMap<>();
        params.put("s", "my case via process service");
        Long processInstanceId = processService.startProcess(deploymentUnit.getIdentifier(), USER_TASK_CASE_P_ID, params);
        assertNotNull(processInstanceId);

        String caseId = processInstanceId.toString();

        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());
            assertEquals(caseId, cInstance.getCaseId());

            Collection<CaseMilestoneInstance> milestones = caseRuntimeDataService.getCaseInstanceMilestones(caseId, true, new QueryContext());
            assertNotNull(milestones);
            assertEquals(0, milestones.size());

            // trigger milestone node
            caseService.triggerAdHocFragment(caseId, "Milestone1", null);

            milestones = caseRuntimeDataService.getCaseInstanceMilestones(caseId, true, new QueryContext());
            assertNotNull(milestones);
            assertEquals(1, milestones.size());

            caseService.cancelCase(caseId);
            CaseInstance instance = caseService.getCaseInstance(caseId);
            Assertions.assertThat(instance.getStatus()).isEqualTo(CaseStatus.CANCELLED.getId());
            caseId = null;
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }

    @Test
    public void testStartThenCancelRetrieveCaseFile() {
        try {
            caseService.getCaseFileInstance(FIRST_CASE_ID);
            fail("There is no case yet started");
        } catch (CaseNotFoundException e) {
            // expected
        }

        Map<String, Object> data = new HashMap<>();
        data.put("name", "my first case");
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), EMPTY_CASE_P_ID, data);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), EMPTY_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());
            assertEquals("my first case", cInstance.getCaseDescription());

            CaseFileInstance caseFileFromCase = caseService.getCaseFileInstance(FIRST_CASE_ID);
            assertNotNull(caseFileFromCase);

            caseService.cancelCase(caseId);
            CaseInstance instance = caseService.getCaseInstance(caseId);
            Assertions.assertThat(instance.getStatus()).isEqualTo(CaseStatus.CANCELLED.getId());

            caseFileFromCase = caseService.getCaseFileInstance(FIRST_CASE_ID);
            assertNotNull(caseFileFromCase);

            caseService.destroyCase(caseId);
            CaseInstance caseInstance = caseService.getCaseInstance(caseId);
            assertThat(caseInstance.getStatus()).isIn(CaseStatus.CLOSED.getId(), CaseStatus.CANCELLED.getId());
            caseId = null;

            try {
                caseService.getCaseFileInstance(FIRST_CASE_ID);
                fail("There is no case yet started");
            } catch (CaseNotFoundException e) {
                // expected
            }
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }

    @Test
    public void testUserTaskToCaseWithStageComplete() {
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl("john"));

        Map<String, Object> data = new HashMap<>();
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), USER_TASK_CASE_P_ID, data, roleAssignments);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), USER_TASK_STAGE_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals(FIRST_CASE_ID, cInstance.getCaseId());
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());

            CaseDefinition caseDef = caseRuntimeDataService.getCase(deploymentUnit.getIdentifier(), USER_TASK_STAGE_CASE_P_ID);
            assertNotNull(caseDef);
            assertEquals(1, caseDef.getCaseStages().size());
            assertEquals(deploymentUnit.getIdentifier(), caseDef.getDeploymentId());

            Collection<CaseStageInstance> activeStages = caseRuntimeDataService.getCaseInstanceStages(caseId, true, new QueryContext());
            assertNotNull(activeStages);
            assertEquals(1, activeStages.size());

            caseService.addDataToCaseFile(caseId, "dataComplete", true);

            activeStages = caseRuntimeDataService.getCaseInstanceStages(caseId, true, new QueryContext());
            assertNotNull(activeStages);
            assertEquals(0, activeStages.size());
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }

    @Test
    public void testStartCaseWithConditionalEvent() {
        Map<String, Object> data = new HashMap<>();
        data.put("Documents", new ArrayList<>());
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), COND_CASE_P_ID, data);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), COND_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());

            caseService.cancelCase(caseId);
            CaseInstance instance = caseService.getCaseInstance(caseId);
            Assertions.assertThat(instance.getStatus()).isEqualTo(CaseStatus.CANCELLED.getId());
            caseId = null;
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }

    @Test
    public void testStartCaseWithConditionalEventCompleteCase() {
        List<String> docs = new ArrayList<>();
        docs.add("First doc");

        Map<String, Object> data = new HashMap<>();
        data.put("Documents", docs);
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), COND_CASE_P_ID, data);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), COND_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        try {

            CaseInstance instance = caseService.getCaseInstance(caseId);
            Assertions.assertThat(instance.getStatus()).isEqualTo(CaseStatus.CLOSED.getId());
            caseId = null;
            
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }

    @Test
    public void testCaseOwnedBy() {
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl("john"));
        roleAssignments.put("contact", new GroupImpl("HR"));

        Map<String, Object> data = new HashMap<>();
        data.put("s", "description");
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), USER_TASK_CASE_P_ID, data, roleAssignments);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), USER_TASK_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(HR_CASE_ID, caseId);

        try {
            Collection<CaseInstance> instances = caseRuntimeDataService.getCaseInstancesOwnedBy("john", Collections.singletonList(CaseStatus.OPEN), true, new QueryContext());
            assertNotNull(instances);
            assertEquals(1, instances.size());
            assertNotNull(instances.iterator().next().getCaseFile());

            instances = caseRuntimeDataService.getCaseInstancesOwnedBy("john", Collections.singletonList(CaseStatus.OPEN), false, new QueryContext());
            assertNotNull(instances);
            assertEquals(1, instances.size());
            assertNull(instances.iterator().next().getCaseFile());
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            identityProvider.setName("john");
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }

    @Test
    public void testCaseRolesWithQueries() {
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl("john"));
        roleAssignments.put("contact", new GroupImpl("HR"));

        Map<String, Object> data = new HashMap<>();
        data.put("s", "description");
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), USER_TASK_CASE_P_ID, data, roleAssignments);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), USER_TASK_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(HR_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals(HR_CASE_ID, cInstance.getCaseId());
            assertEquals(CaseStatus.OPEN.getId(), cInstance.getStatus().intValue());
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());

            // only john is now included in case roles
            Collection<CaseInstance> instances = caseRuntimeDataService.getCaseInstancesAnyRole(null, new QueryContext());
            assertNotNull(instances);
            assertEquals(0, instances.size());

            List<CaseStatus> status = Collections.singletonList(CaseStatus.CANCELLED);

            instances = caseRuntimeDataService.getCaseInstancesAnyRole(status, new QueryContext());
            assertNotNull(instances);
            assertFalse("Opened case was returned when searching for cancelled case instances.", instances.stream().anyMatch(n -> n.getCaseId().equals(caseId)));

            status = Collections.singletonList(CaseStatus.OPEN);

            instances = caseRuntimeDataService.getCaseInstancesAnyRole(status, new QueryContext());
            assertNotNull(instances);
            assertEquals(1, instances.size());
            assertNull(instances.iterator().next().getCaseFile());

            instances = caseRuntimeDataService.getCaseInstancesByRole(null, status, new QueryContext());
            assertNotNull(instances);
            assertEquals(0, instances.size());

            instances = caseRuntimeDataService.getCaseInstancesByRole("owner", status, new QueryContext());
            assertNotNull(instances);
            assertEquals(1, instances.size());
            assertNull(instances.iterator().next().getCaseFile());

            instances = caseRuntimeDataService.getCaseInstancesByRole("owner", status, true, new QueryContext());
            assertNotNull(instances);
            assertEquals(1, instances.size());
            assertNotNull(instances.iterator().next().getCaseFile());

            identityProvider.setName("mary");

            instances = caseRuntimeDataService.getCaseInstancesByRole("owner", status, new QueryContext());
            assertNotNull(instances);
            assertEquals("Mary shouldn't be owner of any opened case instance.", 0, instances.size());

            identityProvider.setRoles(Arrays.asList("HR"));

            instances = caseRuntimeDataService.getCaseInstancesByRole("contact", status, new QueryContext());
            assertNotNull(instances);
            assertEquals(1, instances.size());
            assertNull(instances.iterator().next().getCaseFile());
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            identityProvider.setName("john");
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }

    @Test
    public void testCaseAuthorization() {
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl("john"));
        roleAssignments.put("contact", new GroupImpl("HR"));

        Map<String, Object> data = new HashMap<>();
        data.put("s", "description");
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), USER_TASK_CASE_P_ID, data, roleAssignments);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), USER_TASK_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(HR_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals(HR_CASE_ID, cInstance.getCaseId());
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());

            identityProvider.setName("mary");
            try {
                caseService.cancelCase(caseId);
                fail("Mary is not owner of the case so should not be allowed to cancel the case");
            } catch (SecurityException e) {
                // expected
            }
            try {
                caseService.destroyCase(caseId);
                fail("Mary is not owner of the case so should not be allowed to destroy the case");
            } catch (SecurityException e) {
                // expected
            }

            identityProvider.setName("john");
            caseService.cancelCase(caseId);

            identityProvider.setName("mary");
            try {
                caseService.reopenCase(caseId, deploymentUnit.getIdentifier(), USER_TASK_CASE_P_ID);
                fail("Mary is not owner of the case so should not be allowed to reopen the case");
            } catch (SecurityException e) {
                // expected
            }

            identityProvider.setName("john");
            caseService.reopenCase(caseId, deploymentUnit.getIdentifier(), USER_TASK_CASE_P_ID);
            cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals(HR_CASE_ID, cInstance.getCaseId());
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }

    @Test
    public void testCaseAuthorizationImplicitOwner() {
        String expectedCaseId = "UniqueID-0000000001";
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("patient", new UserImpl("john"));

        Map<String, Object> data = new HashMap<>();
        data.put("s", "description");
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), "CaseWithRolesDefinition", data, roleAssignments);

        identityProvider.setName("mary");
        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), "CaseWithRolesDefinition", caseFile);
        assertNotNull(caseId);
        assertEquals(expectedCaseId, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals(expectedCaseId, cInstance.getCaseId());
            assertEquals("mary", cInstance.getOwner());
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());

            identityProvider.setName("john");

            List<TaskSummary> tasks = caseRuntimeDataService.getCaseTasksAssignedAsPotentialOwner(expectedCaseId, "john", Arrays.asList(Status.Reserved), new QueryContext());
            assertEquals(1, tasks.size());

            userTaskService.completeAutoProgress(tasks.get(0).getId(), "john", null);

            identityProvider.setName("mary");
            caseService.triggerAdHocFragment(expectedCaseId, "task2", null);

            tasks = caseRuntimeDataService.getCaseTasksAssignedAsPotentialOwner(expectedCaseId, "mary", Arrays.asList(Status.Reserved), new QueryContext());
            assertEquals(1, tasks.size());

            userTaskService.completeAutoProgress(tasks.get(0).getId(), "mary", null);

            caseService.triggerAdHocFragment(expectedCaseId, "task3", null);

            tasks = caseRuntimeDataService.getCaseTasksAssignedAsPotentialOwner(expectedCaseId, "mary", Arrays.asList(Status.Ready), new QueryContext());
            assertEquals(1, tasks.size());

            userTaskService.completeAutoProgress(tasks.get(0).getId(), "mary", null);
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                identityProvider.setName("mary");
                caseService.cancelCase(caseId);
            }
        }
    }

    @Test
    public void testTriggerNotExistingAdHocFragment() {
        String expectedCaseId = "UniqueID-0000000001";
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("patient", new UserImpl("john"));

        Map<String, Object> data = new HashMap<>();
        data.put("s", "description");
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), "CaseWithRolesDefinition", data, roleAssignments);

        identityProvider.setName("mary");
        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), "CaseWithRolesDefinition", caseFile);
        assertNotNull(caseId);
        assertEquals(expectedCaseId, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals(expectedCaseId, cInstance.getCaseId());
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());

            identityProvider.setName("john");

            List<TaskSummary> tasks = caseRuntimeDataService.getCaseTasksAssignedAsPotentialOwner(expectedCaseId, "john", Arrays.asList(Status.Reserved), new QueryContext());
            assertEquals(1, tasks.size());

            userTaskService.completeAutoProgress(tasks.get(0).getId(), "john", null);

            identityProvider.setName("mary");
            try {
                caseService.triggerAdHocFragment(expectedCaseId, "not existing", null);
                fail("There is no ad hoc fragment with name 'not existing'");
            } catch (AdHocFragmentNotFoundException e) {
                // expected
            }
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                identityProvider.setName("mary");
                caseService.cancelCase(caseId);
            }
        }
    }

    @Test
    public void testTriggerAdHocTasksFromCurrentAndNextStage() {
        identityProvider.setName("john");
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl("john"));

        Map<String, Object> data = new HashMap<>();
        data.put("customData", "none");

        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), TWO_STAGES_CONDITIONS_CASE_P_ID, data, roleAssignments);
        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), TWO_STAGES_CONDITIONS_CASE_P_ID, caseFile);
        assertNotNull(caseId);

        try {
            Collection<CaseStageInstance> stages = caseRuntimeDataService.getCaseInstanceStages(caseId, false, null);
            assertThat(stages).isNotNull().hasSize(2);
            Iterator<CaseStageInstance> iterator = stages.iterator();

            CaseStageInstance stage1 = iterator.next();
            assertThat(stage1.getName()).isEqualTo("Stage One");
            assertThat(stage1.getStatus()).isEqualTo(StageStatus.Active);

            CaseStageInstance stage2 = iterator.next();
            assertThat(stage2.getName()).isEqualTo("Stage Two");
            assertThat(stage2.getStatus()).isEqualTo(StageStatus.Available);

            List<TaskSummary> tasks = runtimeDataService.getTasksAssignedAsPotentialOwner("john", new QueryFilter());
            assertThat(tasks).isNotNull().isEmpty();

            caseService.triggerAdHocFragment(caseId, "Task 1", null);
            tasks = runtimeDataService.getTasksAssignedAsPotentialOwner("john", new QueryFilter());
            assertThat(tasks).isNotNull().hasSize(1);
            assertTask(tasks.get(0), "john", "Task 1", Status.Reserved);

            caseService.triggerAdHocFragment(caseId, "Task 2", null);
            // the task from the next stage should not be triggered
            tasks = runtimeDataService.getTasksAssignedAsPotentialOwner("john", new QueryFilter());
            assertThat(tasks).isNotNull().hasSize(1);
            assertTask(tasks.get(0), "john", "Task 1", Status.Reserved);

            Map<String, Object> params = Collections.singletonMap("myData", "nextStage");
            userTaskService.completeAutoProgress(tasks.get(0).getId(), "john", params);

            stages = caseRuntimeDataService.getCaseInstanceStages(caseId, false, null);
            assertThat(stages).isNotNull().hasSize(2);
            iterator = stages.iterator();

            stage1 = iterator.next();
            assertThat(stage1.getName()).isEqualTo("Stage One");
            assertThat(stage1.getStatus()).isEqualTo(StageStatus.Completed);

            stage2 = iterator.next();
            assertThat(stage2.getName()).isEqualTo("Stage Two");
            assertThat(stage2.getStatus()).isEqualTo(StageStatus.Active);

            caseService.triggerAdHocFragment(caseId, "Task 2", null);
            tasks = runtimeDataService.getTasksAssignedAsPotentialOwner("john", new QueryFilter());
            assertThat(tasks).isNotNull().hasSize(1);
            assertTask(tasks.get(0), "john", "Task 2", Status.Reserved);

            caseService.triggerAdHocFragment(caseId, "Task 1", null);
            // the task from the previous stage should not be triggered
            tasks = runtimeDataService.getTasksAssignedAsPotentialOwner("john", new QueryFilter());
            assertThat(tasks).isNotNull().hasSize(1);
            assertTask(tasks.get(0), "john", "Task 2", Status.Reserved);

            params = Collections.singletonMap("myData", "none");
            userTaskService.completeAutoProgress(tasks.get(0).getId(), "john", params);

            CaseInstance caseInstance = caseService.getCaseInstance(caseId);
            assertThat(caseInstance.getStatus()).isIn(CaseStatus.CLOSED.getId(), CaseStatus.CANCELLED.getId());
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            caseService.cancelCase(caseId);
            fail("Unexpected exception " + e.getMessage());
        }
    }

    @Test
    public void testTriggerMultipleAdHocTasks() {
        identityProvider.setName("john");
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl("john"));

        Map<String, Object> data = new HashMap<>();
        data.put("customData", "none");

        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), TWO_STAGES_CONDITIONS_CASE_P_ID, data, roleAssignments);
        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), TWO_STAGES_CONDITIONS_CASE_P_ID, caseFile);
        assertThat(caseId).isNotNull();

        try {
            List<TaskSummary> tasks = caseRuntimeDataService.getCaseTasksAssignedAsPotentialOwner(caseId, "john", null, new QueryContext());
            assertThat(tasks).isNotNull().isEmpty();

            caseService.triggerAdHocFragment(caseId, "Task 1", null);

            tasks = caseRuntimeDataService.getCaseTasksAssignedAsPotentialOwner(caseId, "john", null, new QueryContext());
            assertThat(tasks).isNotNull().hasSize(1);
            assertThat(tasks).extracting(TaskSummary::getName).containsOnly("Task 1");

            caseService.triggerAdHocFragment(caseId, "Task 1", null);

            tasks = caseRuntimeDataService.getCaseTasksAssignedAsPotentialOwner(caseId, "john", null, new QueryContext());
            assertThat(tasks).isNotNull().hasSize(2);
            assertThat(tasks).extracting(TaskSummary::getName).containsOnly("Task 1");
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            caseService.cancelCase(caseId);
        }
    }

    @Test
    public void testStartEmptyCaseUsingLoggedInOwner() {
        identityProvider.setName("mary");
        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), EMPTY_CASE_P_ID);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals("mary", cInstance.getOwner());
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());
            String initiator = (String) processService.getProcessInstanceVariable(((CaseInstanceImpl) cInstance).getProcessInstanceId(), "initiator");
            assertEquals("mary", initiator);

            caseService.cancelCase(caseId);
            CaseInstance instance = caseService.getCaseInstance(caseId);
            Assertions.assertThat(instance.getStatus()).isEqualTo(CaseStatus.CANCELLED.getId());
            caseId = null;
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }

    @Test
    public void testStartEmptyCaseChangeOwner() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "my first case");
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), EMPTY_CASE_P_ID, data, new HashMap<>());

        identityProvider.setName("mary");
        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), EMPTY_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);

        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals("mary", cInstance.getOwner());
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());
            String initiator = (String) processService.getProcessInstanceVariable(((CaseInstanceImpl) cInstance).getProcessInstanceId(), "initiator");
            assertEquals("mary", initiator);

            caseService.removeFromCaseRole(caseId, "owner", new UserImpl("mary"));
            caseService.assignToCaseRole(caseId, "owner", new UserImpl("john"));

            identityProvider.setName("john");

            cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals("john", cInstance.getOwner());
            initiator = (String) processService.getProcessInstanceVariable(((CaseInstanceImpl) cInstance).getProcessInstanceId(), "initiator");
            assertEquals("mary", initiator);

            caseService.cancelCase(caseId);
            CaseInstance instance = caseService.getCaseInstance(caseId);
            Assertions.assertThat(instance.getStatus()).isEqualTo(CaseStatus.CANCELLED.getId());
            caseId = null;
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }

    @Test
    public void testStartEmptyCaseUsingCaseFileOwner() {
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl("john"));

        Map<String, Object> data = new HashMap<>();
        data.put("name", "my first case");
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), EMPTY_CASE_P_ID, data, roleAssignments);

        identityProvider.setName("mary");
        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), EMPTY_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        identityProvider.setName("john");
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals("john", cInstance.getOwner());
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());
            String initiator = (String) processService.getProcessInstanceVariable(((CaseInstanceImpl) cInstance).getProcessInstanceId(), "initiator");
            assertEquals("mary", initiator);

            caseService.cancelCase(caseId);
            CaseInstance instance = caseService.getCaseInstance(caseId);
            Assertions.assertThat(instance.getStatus()).isEqualTo(CaseStatus.CANCELLED.getId());
            caseId = null;
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }

    @Test
    public void testStartEmptyCaseUsingCaseFileOwnerAsLoggedInUser() {
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl("mary"));

        Map<String, Object> data = new HashMap<>();
        data.put("name", "my first case");
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), EMPTY_CASE_P_ID, data, roleAssignments);

        identityProvider.setName("mary");
        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), EMPTY_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals("mary", cInstance.getOwner());
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());
            String initiator = (String) processService.getProcessInstanceVariable(((CaseInstanceImpl) cInstance).getProcessInstanceId(), "initiator");
            assertEquals("mary", initiator);

            caseService.cancelCase(caseId);
            CaseInstance instance = caseService.getCaseInstance(caseId);
            Assertions.assertThat(instance.getStatus()).isEqualTo(CaseStatus.CANCELLED.getId());
            caseId = null;
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }

    @Test
    public void testUserTaskToCaseWithStageCompleteCaseDataItem() {
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl("john"));

        Map<String, Object> data = new HashMap<>();
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), USER_TASK_CASE_P_ID, data, roleAssignments);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), USER_TASK_STAGE_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals(FIRST_CASE_ID, cInstance.getCaseId());
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());

            Collection<CaseFileItem> caseFileItems = caseRuntimeDataService.getCaseInstanceDataItems(caseId, new QueryContext());
            assertNotNull(caseFileItems);
            assertEquals(0, caseFileItems.size());

            Collection<CaseStageInstance> activeStages = caseRuntimeDataService.getCaseInstanceStages(caseId, true, new QueryContext());
            assertNotNull(activeStages);
            assertEquals(1, activeStages.size());

            caseService.addDataToCaseFile(caseId, "dataComplete", true);

            activeStages = caseRuntimeDataService.getCaseInstanceStages(caseId, true, new QueryContext());
            assertNotNull(activeStages);
            assertEquals(0, activeStages.size());

            caseFileItems = caseRuntimeDataService.getCaseInstanceDataItems(caseId, new QueryContext());
            assertNotNull(caseFileItems);
            assertEquals(1, caseFileItems.size());

            CaseFileItem dataItem = caseFileItems.iterator().next();
            assertNotNull(dataItem);
            assertEquals(caseId, dataItem.getCaseId());
            assertEquals("dataComplete", dataItem.getName());
            assertEquals("true", dataItem.getValue());
            assertEquals(Boolean.class.getName(), dataItem.getType());
            assertEquals(identityProvider.getName(), dataItem.getLastModifiedBy());
            assertNotNull(dataItem.getLastModified());

            caseService.addDataToCaseFile(caseId, "anotherDataItem", "first version");

            caseFileItems = caseRuntimeDataService.getCaseInstanceDataItemsByType(caseId, Collections.singletonList("boolean"), new QueryContext());
            assertNotNull(caseFileItems);
            assertEquals(0, caseFileItems.size());

            caseFileItems = caseRuntimeDataService.getCaseInstanceDataItemsByType(caseId, Collections.singletonList(Boolean.class.getName()), new QueryContext());
            assertNotNull(caseFileItems);
            assertEquals(1, caseFileItems.size());

            dataItem = caseFileItems.iterator().next();
            assertNotNull(dataItem);
            assertEquals(caseId, dataItem.getCaseId());
            assertEquals("dataComplete", dataItem.getName());

            caseFileItems = caseRuntimeDataService.getCaseInstanceDataItemsByType(caseId, Collections.singletonList(String.class.getName()), new QueryContext());
            assertNotNull(caseFileItems);
            assertEquals(1, caseFileItems.size());

            dataItem = caseFileItems.iterator().next();
            assertNotNull(dataItem);
            assertEquals(caseId, dataItem.getCaseId());
            assertEquals("anotherDataItem", dataItem.getName());

            caseFileItems = caseRuntimeDataService.getCaseInstanceDataItems(caseId, new QueryContext());
            assertNotNull(caseFileItems);
            assertEquals(2, caseFileItems.size());

            dataItem = caseFileItems.iterator().next();
            assertNotNull(dataItem);
            assertEquals(caseId, dataItem.getCaseId());
            assertEquals("anotherDataItem", dataItem.getName());
            assertEquals("first version", dataItem.getValue());
            assertEquals(String.class.getName(), dataItem.getType());
            assertEquals(identityProvider.getName(), dataItem.getLastModifiedBy());
            assertNotNull(dataItem.getLastModified());

            caseService.addDataToCaseFile(caseId, "anotherDataItem", "second version");

            caseFileItems = caseRuntimeDataService.getCaseInstanceDataItemsByType(caseId, Arrays.asList(Boolean.class.getName(), String.class.getName()), new QueryContext());
            assertNotNull(caseFileItems);
            assertEquals(2, caseFileItems.size());

            caseFileItems = caseRuntimeDataService.getCaseInstanceDataItemsByName(caseId, Arrays.asList("anotherDataItem", "dataComplete"), new QueryContext());
            assertNotNull(caseFileItems);
            assertEquals(2, caseFileItems.size());

            Iterator<CaseFileItem> it = caseFileItems.iterator();
            dataItem = it.next();
            assertNotNull(dataItem);
            assertEquals(caseId, dataItem.getCaseId());
            assertEquals("anotherDataItem", dataItem.getName());

            dataItem = it.next();
            assertNotNull(dataItem);
            assertEquals(caseId, dataItem.getCaseId());
            assertEquals("dataComplete", dataItem.getName());

            caseFileItems = caseRuntimeDataService.getCaseInstanceDataItems(caseId, new QueryContext());
            assertNotNull(caseFileItems);
            assertEquals(2, caseFileItems.size());

            dataItem = caseFileItems.iterator().next();
            assertNotNull(dataItem);
            assertEquals(caseId, dataItem.getCaseId());
            assertEquals("anotherDataItem", dataItem.getName());
            assertEquals("second version", dataItem.getValue());
            assertEquals(String.class.getName(), dataItem.getType());
            assertEquals(identityProvider.getName(), dataItem.getLastModifiedBy());
            assertNotNull(dataItem.getLastModified());

            caseService.removeDataFromCaseFile(caseId, "anotherDataItem");
            caseFileItems = caseRuntimeDataService.getCaseInstanceDataItems(caseId, new QueryContext());
            assertNotNull(caseFileItems);
            assertEquals(1, caseFileItems.size());

            caseFileItems = caseRuntimeDataService.getCaseInstanceDataItemsByName(caseId, Collections.singletonList("anotherDataItem"), new QueryContext());
            assertNotNull(caseFileItems);
            assertTrue(caseFileItems.isEmpty());

            caseFileItems = caseRuntimeDataService.getCaseInstanceDataItemsByType(caseId, Collections.singletonList(String.class.getName()), new QueryContext());
            assertNotNull(caseFileItems);
            assertTrue(caseFileItems.isEmpty());

            identityProvider.setName("mary");
            caseFileItems = caseRuntimeDataService.getCaseInstanceDataItems(caseId, new QueryContext());
            assertNotNull(caseFileItems);
            // mary is not involved in case instance
            assertEquals(0, caseFileItems.size());
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            identityProvider.setName("john");
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }

    @Test
    public void testUserTaskToCaseSearchByCaseFileData() {
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl("john"));

        Map<String, Object> data = new HashMap<>();
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), USER_TASK_CASE_P_ID, data, roleAssignments);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), USER_TASK_STAGE_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals(FIRST_CASE_ID, cInstance.getCaseId());
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());

            caseService.addDataToCaseFile(caseId, "dataComplete", true);

            Collection<CaseInstance> byCaseData = caseRuntimeDataService.getCaseInstancesByDataItem("dataComplete", Arrays.asList(CaseStatus.OPEN), new QueryContext());
            assertNotNull(byCaseData);
            assertEquals(1, byCaseData.size());

            byCaseData = caseRuntimeDataService.getCaseInstancesByDataItemAndValue("dataComplete", "false", Arrays.asList(CaseStatus.OPEN), new QueryContext());
            assertNotNull(byCaseData);
            assertEquals(0, byCaseData.size());

            byCaseData = caseRuntimeDataService.getCaseInstancesByDataItemAndValue("dataComplete", "true", Arrays.asList(CaseStatus.OPEN), new QueryContext());
            assertNotNull(byCaseData);
            assertEquals(1, byCaseData.size());

            identityProvider.setName("mary");
            byCaseData = caseRuntimeDataService.getCaseInstancesByDataItem("dataComplete", Arrays.asList(CaseStatus.OPEN), new QueryContext());
            assertNotNull(byCaseData);
            // mary is not part of the case instance
            assertEquals(0, byCaseData.size());
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            identityProvider.setName("john");
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }
    
    @Test
    public void testUserTaskCaseSearchByInitialCaseFileData() {
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl("john"));

        Map<String, Object> data = new HashMap<>();
        data.put("dataComplete", true);
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), USER_TASK_CASE_P_ID, data, roleAssignments);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), USER_TASK_STAGE_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals(FIRST_CASE_ID, cInstance.getCaseId());
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());

            Collection<CaseInstance> byCaseData = caseRuntimeDataService.getCaseInstancesByDataItem("dataComplete", Arrays.asList(CaseStatus.OPEN), new QueryContext());
            assertNotNull(byCaseData);
            assertEquals(1, byCaseData.size());

            byCaseData = caseRuntimeDataService.getCaseInstancesByDataItemAndValue("dataComplete", "false", Arrays.asList(CaseStatus.OPEN), new QueryContext());
            assertNotNull(byCaseData);
            assertEquals(0, byCaseData.size());

            byCaseData = caseRuntimeDataService.getCaseInstancesByDataItemAndValue("dataComplete", "true", Arrays.asList(CaseStatus.OPEN), new QueryContext());
            assertNotNull(byCaseData);
            assertEquals(1, byCaseData.size());

            identityProvider.setName("mary");
            byCaseData = caseRuntimeDataService.getCaseInstancesByDataItem("dataComplete", Arrays.asList(CaseStatus.OPEN), new QueryContext());
            assertNotNull(byCaseData);
            // mary is not part of the case instance
            assertEquals(0, byCaseData.size());
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            identityProvider.setName("john");
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }

    @Test
    public void testCaseRolesCardinality() {
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl("john"));

        Map<String, Object> data = new HashMap<>();
        data.put("s", "description");
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), USER_TASK_CASE_P_ID, data, roleAssignments);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), USER_TASK_CASE_P_ID, caseFile);

        try {
            caseService.assignToCaseRole(caseId, "contact", new UserImpl("mary"));
            caseService.assignToCaseRole(caseId, "contact", new UserImpl("steve"));

            Throwable error = Assertions.catchThrowable(() -> caseService.assignToCaseRole(caseId, "contact", new UserImpl("jack")));
            assertThat(error).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Cannot add more users for role contact, maximum cardinality 2 already reached");
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }
    
    @Test
    public void testStartEmptyCaseAndClose() {
        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), EMPTY_CASE_P_ID);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());

            caseService.closeCase(caseId, "not needed any more");
            
            cInstance = caseRuntimeDataService.getCaseInstanceById(caseId);
            assertNotNull(cInstance);
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());
            assertEquals("not needed any more", cInstance.getCompletionMessage());
            
            caseId = null;
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }
    
    @Test
    public void testAddSubprocessToEmptyCaseAndClose() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "my first case");
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), EMPTY_CASE_P_ID, data);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), EMPTY_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals(FIRST_CASE_ID, cInstance.getCaseId());
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());

            // add dynamic user task to empty case instance - first by case id
            Map<String, Object> parameters = new HashMap<>();
            caseService.addDynamicSubprocess(FIRST_CASE_ID, SUBPROCESS_P_ID, parameters);

            // second task add by process instance id
            Collection<ProcessInstanceDesc> caseProcessInstances = caseRuntimeDataService.getProcessInstancesForCase(caseId, new QueryContext());
            assertNotNull(caseProcessInstances);
            assertEquals(2, caseProcessInstances.size());
            
            caseService.closeCase(caseId, "not needed any more");
            
            cInstance = caseRuntimeDataService.getCaseInstanceById(caseId);
            assertNotNull(cInstance);
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());
            assertEquals("not needed any more", cInstance.getCompletionMessage());
            
            caseProcessInstances = caseRuntimeDataService.getProcessInstancesForCase(caseId, Arrays.asList(2), new QueryContext());
            assertNotNull(caseProcessInstances);
            assertEquals(2, caseProcessInstances.size());            
            
            caseId = null;
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }

    @Test
    public void testStartExpressionCaseWithCaseFile() {
        Map<String, Object> data = new HashMap<>();
        data.put("person", new Person("john"));
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), EXPRESSION_CASE_P_ID, data);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), EXPRESSION_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId, true, false, false, false);
            assertNotNull(cInstance);
            assertEquals(FIRST_CASE_ID, cInstance.getCaseId());
            assertNotNull(cInstance.getCaseFile());
            assertEquals("john", ((Person) cInstance.getCaseFile().getData("person")).getName());
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());
            
            List<TaskSummary> tasks = caseRuntimeDataService.getCaseTasksAssignedAsPotentialOwner(caseId, "john",null, new QueryContext());
            assertNotNull(tasks);
            assertEquals(1, tasks.size());
            
            Map<String, Object> taskInputs = userTaskService.getTaskInputContentByTaskId(tasks.get(0).getId());
            Object personName = taskInputs.get("personName");
            
            assertEquals("john", personName);

            caseService.cancelCase(caseId);
            caseId = null;
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }
    
    @Test
    public void testCaseWithCommentsWithRestrictions() {
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl("john"));
        roleAssignments.put("participant", new UserImpl("mary"));

        Map<String, Object> data = new HashMap<>();
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), USER_TASK_STAGE_AUTO_START_CASE_P_ID, data, roleAssignments);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), USER_TASK_STAGE_AUTO_START_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals(FIRST_CASE_ID, cInstance.getCaseId());
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());

            Collection<CommentInstance> caseComments = caseService.getCaseComments(FIRST_CASE_ID, new QueryContext());
            assertNotNull(caseComments);
            assertEquals(0, caseComments.size());

            caseService.addCaseComment(FIRST_CASE_ID, "poul", "just a tiny comment", "owner");

            caseComments = caseService.getCaseComments(FIRST_CASE_ID, new QueryContext());
            assertNotNull(caseComments);
            assertEquals(1, caseComments.size());

            CommentInstance comment = caseComments.iterator().next();
            assertComment(comment, "poul", "just a tiny comment");
            
            // mary is not the owner so should not see the comment that is only for role owner role
            identityProvider.setName("mary");
            caseComments = caseService.getCaseComments(FIRST_CASE_ID, new QueryContext());
            assertNotNull(caseComments);
            assertEquals(0, caseComments.size());
            
            try {
                caseService.updateCaseComment(FIRST_CASE_ID, comment.getId(), comment.getAuthor(), "Updated " + comment.getComment(), "participant", "owner");
                fail("mary should not be able to update comment that she has no access to");
            } catch (SecurityException e) {
                
                // mary is not allowed to update comments that she has no access to
                assertTrue(e.getMessage().contains("User mary does not have access to comment"));
            }
            
            identityProvider.setName("john");

            caseService.updateCaseComment(FIRST_CASE_ID, comment.getId(), comment.getAuthor(), "Updated " + comment.getComment(), "participant", "owner");
            caseComments = caseService.getCaseComments(FIRST_CASE_ID, new QueryContext());
            assertNotNull(caseComments);
            assertEquals(1, caseComments.size());

            comment = caseComments.iterator().next();
            assertComment(comment, "poul", "Updated just a tiny comment");
            
            // now mary as participant should see the updated comment
            identityProvider.setName("mary");
            caseComments = caseService.getCaseComments(FIRST_CASE_ID, new QueryContext());
            assertNotNull(caseComments);
            assertEquals(1, caseComments.size());
            
            identityProvider.setName("john");

            // no restrictions
            caseService.addCaseComment(FIRST_CASE_ID, "mary", "another comment");

            caseComments = caseService.getCaseComments(FIRST_CASE_ID, new QueryContext());
            assertNotNull(caseComments);
            assertEquals(2, caseComments.size());

            Iterator<CommentInstance> it = caseComments.iterator();
            assertComment(it.next(), "poul", "Updated just a tiny comment");
            assertComment(it.next(), "mary", "another comment");    
            
            // second comment has no restrictions so should be seen by anyone
            identityProvider.setName("mary");
            caseComments = caseService.getCaseComments(FIRST_CASE_ID, new QueryContext());
            assertNotNull(caseComments);
            assertEquals(2, caseComments.size());
            
            identityProvider.setName("john");
            caseService.addCaseComment(FIRST_CASE_ID, "john", "private comment", "owner");
            
            caseComments = caseService.getCaseComments(FIRST_CASE_ID, CommentSortBy.Author, new QueryContext());
            assertNotNull(caseComments);
            assertEquals(3, caseComments.size());
            
            comment = caseComments.iterator().next();
            assertComment(comment, "john", "private comment");

            identityProvider.setName("mary");
            try {
                caseService.removeCaseComment(FIRST_CASE_ID, comment.getId());
                fail("mary should not be able to remove comment that she has no access to");
            } catch (SecurityException e) {
                
                // mary is not allowed to removed comments that she has no access to
                assertTrue(e.getMessage().contains("User mary does not have access to comment"));
            }

            caseComments = caseService.getCaseComments(FIRST_CASE_ID, CommentSortBy.Author, new QueryContext());
            assertEquals(2, caseComments.size());
            
            identityProvider.setName("john");
            caseComments = caseService.getCaseComments(FIRST_CASE_ID, CommentSortBy.Author, new QueryContext());
            assertEquals(3, caseComments.size());
            
            caseService.removeCaseComment(FIRST_CASE_ID, comment.getId());
            
            caseComments = caseService.getCaseComments(FIRST_CASE_ID, CommentSortBy.Author, new QueryContext());
            assertEquals(2, caseComments.size());
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }
    
    @Test
    public void testUserTaskCaseDataItemWithRestrictions() {
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl("john"));
        roleAssignments.put("participant", new UserImpl("mary"));

        Map<String, Object> data = new HashMap<>();
        data.put("contactInfo", "main street 10, NYC");
        
        Map<String, List<String>> accessRestrictions = new HashMap<>();
        accessRestrictions.put("contactInfo", Arrays.asList("owner"));
        
        CaseFileInstance caseFile = caseService.newCaseFileInstanceWithRestrictions(deploymentUnit.getIdentifier(), USER_TASK_CASE_P_ID, data, roleAssignments, accessRestrictions);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), USER_TASK_STAGE_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId, true, false, false, false);
            assertNotNull(cInstance);
            assertEquals(FIRST_CASE_ID, cInstance.getCaseId());

            Map<String, Object> caseData = cInstance.getCaseFile().getData();
            assertNotNull(caseData);
            assertEquals(1, caseData.size());            
            assertEquals("main street 10, NYC", caseData.get("contactInfo"));
            
            identityProvider.setName("mary");
            cInstance = caseService.getCaseInstance(caseId, true, false, false, false);
            assertNotNull(cInstance);
            assertEquals(FIRST_CASE_ID, cInstance.getCaseId());
            // mary should not see any data yet
            caseData = cInstance.getCaseFile().getData();
            assertNotNull(caseData);
            assertEquals(0, caseData.size());
            
            identityProvider.setName("john");
            Collection<CaseStageInstance> activeStages = caseRuntimeDataService.getCaseInstanceStages(caseId, true, new QueryContext());
            assertNotNull(activeStages);
            assertEquals(1, activeStages.size());

            caseService.addDataToCaseFile(caseId, "dataComplete", true, "owner", "participant");

            activeStages = caseRuntimeDataService.getCaseInstanceStages(caseId, true, new QueryContext());
            assertNotNull(activeStages);
            assertEquals(0, activeStages.size());
            
            cInstance = caseService.getCaseInstance(caseId, true, false, false, false);
            assertNotNull(cInstance);
            assertEquals(FIRST_CASE_ID, cInstance.getCaseId());

            caseData = cInstance.getCaseFile().getData();
            assertNotNull(caseData);
            assertEquals(2, caseData.size());
            assertEquals("main street 10, NYC", caseData.get("contactInfo"));
            assertEquals(true, caseData.get("dataComplete"));
            
            identityProvider.setName("mary");
            cInstance = caseService.getCaseInstance(caseId, true, false, false, false);
            assertNotNull(cInstance);
            assertEquals(FIRST_CASE_ID, cInstance.getCaseId());
            // mary should not see contactInfo
            caseData = cInstance.getCaseFile().getData();
            assertNotNull(caseData);
            assertEquals(1, caseData.size());
            assertEquals(true, caseData.get("dataComplete"));
            
            identityProvider.setName("john");
            
            caseService.addDataToCaseFile(caseId, "anotherDataItem", "first version");
            
            cInstance = caseService.getCaseInstance(caseId, true, false, false, false);
            assertNotNull(cInstance);
            assertEquals(FIRST_CASE_ID, cInstance.getCaseId());
            caseData = cInstance.getCaseFile().getData();
            assertNotNull(caseData);
            assertEquals(3, caseData.size());
            assertEquals("main street 10, NYC", caseData.get("contactInfo"));
            assertEquals(true, caseData.get("dataComplete"));
            assertEquals("first version", caseData.get("anotherDataItem"));
            
            identityProvider.setName("mary");
            cInstance = caseService.getCaseInstance(caseId, true, false, false, false);
            assertNotNull(cInstance);
            assertEquals(FIRST_CASE_ID, cInstance.getCaseId());
            // mary should not see contactInfo
            caseData = cInstance.getCaseFile().getData();
            assertNotNull(caseData);
            assertEquals(2, caseData.size());
            assertEquals(true, caseData.get("dataComplete"));
            assertEquals("first version", caseData.get("anotherDataItem"));

            try {
                caseService.removeDataFromCaseFile(caseId, "contactInfo");
                fail("mary should not be able to remove data that she has not access to");
            } catch (SecurityException e) {
                
                // mary is not allowed to removed comments that she has no role to
                assertTrue(e.getMessage().contains("User mary does not have access to data"));
            }
            
            identityProvider.setName("john");
            cInstance = caseService.getCaseInstance(caseId, true, false, false, false);
            assertNotNull(cInstance);
            assertEquals(FIRST_CASE_ID, cInstance.getCaseId());
            caseData = cInstance.getCaseFile().getData();
            assertNotNull(caseData);
            assertEquals(3, caseData.size());
            assertEquals("main street 10, NYC", caseData.get("contactInfo"));
            assertEquals(true, caseData.get("dataComplete"));
            assertEquals("first version", caseData.get("anotherDataItem"));
            
            caseService.removeDataFromCaseFile(caseId, "contactInfo");
            
            cInstance = caseService.getCaseInstance(caseId, true, false, false, false);
            assertNotNull(cInstance);
            assertEquals(FIRST_CASE_ID, cInstance.getCaseId());
            caseData = cInstance.getCaseFile().getData();
            assertNotNull(caseData);
            assertEquals(2, caseData.size());            
            assertEquals(true, caseData.get("dataComplete"));
            assertEquals("first version", caseData.get("anotherDataItem"));
            
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            identityProvider.setName("john");
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }
    
    @Test
    public void testCaseWithDefinedDataRestrictions() {
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl("john"));
        roleAssignments.put("participant", new UserImpl("mary"));
        
        Map<String, Object> data = new HashMap<>();
        data.put("contactInfo", "main street 10, NYC");
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), USER_TASK_DATA_RESTRICTIONS_CASE_P_ID, data, roleAssignments);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), USER_TASK_DATA_RESTRICTIONS_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId, true, false, false, false);
            assertNotNull(cInstance);
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());
            
            Map<String, Object> caseData = cInstance.getCaseFile().getData();
            assertNotNull(caseData);
            assertEquals(1, caseData.size());            
            assertEquals("main street 10, NYC", caseData.get("contactInfo"));
            
            identityProvider.setName("mary");            
            cInstance = caseService.getCaseInstance(caseId, true, false, false, false);
            assertNotNull(cInstance);
            caseData = cInstance.getCaseFile().getData();
            assertNotNull(caseData);
            assertEquals(0, caseData.size());  
            
            identityProvider.setName("john"); 
            caseService.addDataToCaseFile(caseId, "request", "does it actually work?");
            
            cInstance = caseService.getCaseInstance(caseId, true, false, false, false);
            assertNotNull(cInstance);
            caseData = cInstance.getCaseFile().getData();
            assertNotNull(caseData);
            assertEquals(2, caseData.size());
            assertEquals("main street 10, NYC", caseData.get("contactInfo"));
            assertEquals("does it actually work?", caseData.get("request"));
            
            identityProvider.setName("mary"); 
            cInstance = caseService.getCaseInstance(caseId, true, false, false, false);
            assertNotNull(cInstance);
            caseData = cInstance.getCaseFile().getData();
            assertNotNull(caseData);
            assertEquals(1, caseData.size());
            assertEquals("does it actually work?", caseData.get("request"));
            
            identityProvider.setName("john"); 
            try {
                caseService.addDataToCaseFile(caseId, "reply", "does it actually work?");
                fail("john does not have access to reply data so cannot add it");
            } catch (SecurityException e) {
                assertTrue(e.getMessage().startsWith("User john does not have access to data item named reply"));
            }
            
            identityProvider.setName("mary");
            caseService.addDataToCaseFile(caseId, "reply", "oh yes, it does!");
            
            cInstance = caseService.getCaseInstance(caseId, true, false, false, false);
            assertNotNull(cInstance);
            caseData = cInstance.getCaseFile().getData();
            assertNotNull(caseData);
            assertEquals(2, caseData.size());
            assertEquals("oh yes, it does!", caseData.get("reply"));
            assertEquals("does it actually work?", caseData.get("request"));
            
            identityProvider.setName("john"); 
            cInstance = caseService.getCaseInstance(caseId, true, false, false, false);
            assertNotNull(cInstance);
            caseData = cInstance.getCaseFile().getData();
            assertNotNull(caseData);
            assertEquals(2, caseData.size());
            assertEquals("main street 10, NYC", caseData.get("contactInfo"));
            assertEquals("does it actually work?", caseData.get("request"));
            
            identityProvider.setName("mary");
            caseService.addDataToCaseFile(caseId, "reply", "oh yes, it does! make it visible to all", AuthorizationManager.PUBLIC_GROUP);
            
            identityProvider.setName("john"); 
            cInstance = caseService.getCaseInstance(caseId, true, false, false, false);
            assertNotNull(cInstance);
            caseData = cInstance.getCaseFile().getData();
            assertNotNull(caseData);
            assertEquals(3, caseData.size());
            assertEquals("main street 10, NYC", caseData.get("contactInfo"));
            assertEquals("does it actually work?", caseData.get("request"));
            assertEquals("oh yes, it does! make it visible to all", caseData.get("reply"));
            
            
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                identityProvider.setName("john");
                caseService.cancelCase(caseId);
            }
        }
    }
    
    @Test
    public void testCaseWithDefinedDataRestrictionsSetViaUserTask() {
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl("john"));
        roleAssignments.put("participant", new UserImpl("mary"));
        
        Map<String, Object> data = new HashMap<>();
        data.put("contactInfo", "main street 10, NYC");
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), USER_TASK_DATA_RESTRICTIONS_CASE_P_ID, data, roleAssignments);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), USER_TASK_DATA_RESTRICTIONS_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId, true, false, false, false);
            assertNotNull(cInstance);
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());
            
            Map<String, Object> caseData = cInstance.getCaseFile().getData();
            assertNotNull(caseData);
            assertEquals(1, caseData.size());            
            assertEquals("main street 10, NYC", caseData.get("contactInfo"));
    
            List<TaskSummary> tasks = caseRuntimeDataService.getCaseTasksAssignedAsPotentialOwner(caseId, "john", null, new QueryContext());
            assertNotNull(tasks);
            assertEquals(1, tasks.size());
            
            Map<String, Object> params = new HashMap<>();
            params.put("reply_", "John sets the reply via process");
            userTaskService.completeAutoProgress(tasks.get(0).getId(), "john", params);
            
            cInstance = caseService.getCaseInstance(caseId, true, false, false, false);
            assertNotNull(cInstance);
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());
            
            caseData = cInstance.getCaseFile().getData();
            assertNotNull(caseData);
            assertEquals(1, caseData.size());            
            assertEquals("main street 10, NYC", caseData.get("contactInfo"));
            
            identityProvider.setName("mary");
            cInstance = caseService.getCaseInstance(caseId, true, false, false, false);
            assertNotNull(cInstance);
            caseData = cInstance.getCaseFile().getData();
            assertNotNull(caseData);
            assertEquals(1, caseData.size());
            assertEquals("John sets the reply via process", caseData.get("reply"));
            
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                identityProvider.setName("john");
                caseService.cancelCase(caseId);
            }
        }
    }
    
    @Test
    public void testInclusiveGatewayWithDynamicActivity() {
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("actorRole", new UserImpl("john"));
        roleAssignments.put("groupRole", new GroupImpl("managers"));

        Map<String, Object> data = new HashMap<>();
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), "InclusiveGatewayCase", data, roleAssignments);
                
        ((CaseAssignment)caseFile).assignUser("generalRole", "john");

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), "InclusiveGatewayCase", caseFile);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals(FIRST_CASE_ID, cInstance.getCaseId());
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());
            
            List<TaskSummary> tasks = caseRuntimeDataService.getCaseTasksAssignedAsPotentialOwner(caseId, "john", null, new QueryContext());
            assertThat(tasks).hasSize(2);
            
            caseService.addDynamicTask(caseId, caseService.newHumanTaskSpec("First task", "another test", "actorRole", null, new HashMap<>()));
            
            List<TaskSummary> allTasks = caseRuntimeDataService.getCaseTasksAssignedAsPotentialOwner(caseId, "john", null, new QueryContext());
            assertThat(allTasks).hasSize(3);
            
            // now complete only tasks from case definition and leave dynamic task alone
            for (TaskSummary task : tasks) {
                userTaskService.completeAutoProgress(task.getId(), "john", null);
            }
            
            // dynamic task should still be there
            allTasks = caseRuntimeDataService.getCaseTasksAssignedAsPotentialOwner(caseId, "john", null, new QueryContext());
            assertThat(allTasks).hasSize(1);
            
            cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }

    @Test
    public void testSystemUserCaseAuthorization() {
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl("john"));
        roleAssignments.put("contact", new GroupImpl("HR"));

        Map<String, Object> data = new HashMap<>();
        data.put("s", "description");
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), USER_TASK_CASE_P_ID, data, roleAssignments);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), USER_TASK_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(HR_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals(HR_CASE_ID, cInstance.getCaseId());
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());

            identityProvider.setName("mary");
            try {
                caseService.getCaseInstance(caseId);
                fail("Mary is not owner of the case so should not be allowed to retrieve its CaseInstance object");
            } catch (SecurityException e) {
                // expected
            }

            // System user should be allowed to do anything
            identityProvider.setName(AuthorizationManager.UNKNOWN_USER);
            caseService.getCaseInstance(caseId);

            identityProvider.setName("mary");
            try {
                caseService.cancelCase(caseId);
                fail("Mary is not owner of the case so should not be allowed to cancel the case");
            } catch (SecurityException e) {
                // expected
            }

            // System user should be allowed to do anything
            identityProvider.setName(AuthorizationManager.UNKNOWN_USER);
            caseService.cancelCase(caseId);

            identityProvider.setName("mary");
            try {
                caseService.reopenCase(caseId, deploymentUnit.getIdentifier(), USER_TASK_CASE_P_ID);
                fail("Mary is not owner of the case so should not be allowed to reopen the case");
            } catch (SecurityException e) {
                // expected
            }

            identityProvider.setName(AuthorizationManager.UNKNOWN_USER);
            caseService.reopenCase(caseId, deploymentUnit.getIdentifier(), USER_TASK_CASE_P_ID);
            cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals(HR_CASE_ID, cInstance.getCaseId());
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                identityProvider.setName("john");
                caseService.cancelCase(caseId);
            }
        }
    }

    @Test
    public void testSystemUserCaseDataAuthorization() {
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl("john"));
        roleAssignments.put("participant", new UserImpl("mary"));

        Map<String, Object> data = new HashMap<>();
        data.put("contactInfo", "main street 10, NYC");
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), USER_TASK_DATA_RESTRICTIONS_CASE_P_ID, data, roleAssignments);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), USER_TASK_DATA_RESTRICTIONS_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId, true, false, false, false);
            assertNotNull(cInstance);
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());

            // john can see data
            Map<String, Object> caseData = cInstance.getCaseFile().getData();
            assertNotNull(caseData);
            assertEquals(1, caseData.size());
            assertEquals("main street 10, NYC", caseData.get("contactInfo"));

            // mary cannot see any data yet
            identityProvider.setName("mary");
            cInstance = caseService.getCaseInstance(caseId, true, false, false, false);
            assertNotNull(cInstance);
            caseData = cInstance.getCaseFile().getData();
            assertNotNull(caseData);
            assertEquals(0, caseData.size());

            // system user can see all data
            identityProvider.setName(AuthorizationManager.UNKNOWN_USER);
            cInstance = caseService.getCaseInstance(caseId, true, false, false, false);
            assertNotNull(cInstance);
            caseData = cInstance.getCaseFile().getData();
            assertNotNull(caseData);
            assertEquals(1, caseData.size());

            // add data which are restricted to john and mary
            caseService.addDataToCaseFile(caseId, "request", "does it actually work?");
            // add data which are restricted to mary
            caseService.addDataToCaseFile(caseId, "reply", "oh yes, it does!");

            // system user can see all data
            cInstance = caseService.getCaseInstance(caseId, true, false, false, false);
            assertNotNull(cInstance);
            caseData = cInstance.getCaseFile().getData();
            assertNotNull(caseData);
            assertEquals(3, caseData.size());
            assertEquals("main street 10, NYC", caseData.get("contactInfo"));
            assertEquals("does it actually work?", caseData.get("request"));
            assertEquals("oh yes, it does!", caseData.get("reply"));

            identityProvider.setName("john");
            cInstance = caseService.getCaseInstance(caseId, true, false, false, false);
            assertNotNull(cInstance);
            caseData = cInstance.getCaseFile().getData();
            assertNotNull(caseData);
            assertEquals(2, caseData.size());
            assertEquals("main street 10, NYC", caseData.get("contactInfo"));
            assertEquals("does it actually work?", caseData.get("request"));

            identityProvider.setName("mary");
            cInstance = caseService.getCaseInstance(caseId, true, false, false, false);
            assertNotNull(cInstance);
            caseData = cInstance.getCaseFile().getData();
            assertNotNull(caseData);
            assertEquals(2, caseData.size());
            assertEquals("does it actually work?", caseData.get("request"));
            assertEquals("oh yes, it does!", caseData.get("reply"));

        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                identityProvider.setName("john");
                caseService.cancelCase(caseId);
            }
        }
    }

    @Test
    public void testSystemUserCaseCommentsAuthorization() {
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl("john"));
        roleAssignments.put("participant", new UserImpl("mary"));

        Map<String, Object> data = new HashMap<>();
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), USER_TASK_STAGE_AUTO_START_CASE_P_ID, data, roleAssignments);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), USER_TASK_STAGE_AUTO_START_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals(FIRST_CASE_ID, cInstance.getCaseId());
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());

            Collection<CommentInstance> caseComments = caseService.getCaseComments(FIRST_CASE_ID, new QueryContext());
            assertNotNull(caseComments);
            assertEquals(0, caseComments.size());

            caseService.addCaseComment(FIRST_CASE_ID, "poul", "just a tiny comment", "owner");

            caseComments = caseService.getCaseComments(FIRST_CASE_ID, new QueryContext());
            assertNotNull(caseComments);
            assertEquals(1, caseComments.size());
            CommentInstance comment = caseComments.iterator().next();
            assertComment(comment, "poul", "just a tiny comment");

            // mary is not the owner so should not see the comment that is only for role owner role
            identityProvider.setName("mary");
            caseComments = caseService.getCaseComments(FIRST_CASE_ID, new QueryContext());
            assertNotNull(caseComments);
            assertEquals(0, caseComments.size());

            try {
                caseService.updateCaseComment(FIRST_CASE_ID, comment.getId(), comment.getAuthor(), "Updated " + comment.getComment(), "participant", "owner");
                fail("mary should not be able to update comment that she has no access to");
            } catch (SecurityException e) {
                // mary is not allowed to update comments that she has no access to
                assertTrue(e.getMessage().contains("User mary does not have access to comment"));
            }

            // System user can do anything with comments
            identityProvider.setName(AuthorizationManager.UNKNOWN_USER);
            caseComments = caseService.getCaseComments(FIRST_CASE_ID, new QueryContext());
            assertNotNull(caseComments);
            assertEquals(1, caseComments.size());
            comment = caseComments.iterator().next();
            assertComment(comment, "poul", "just a tiny comment");

            caseService.updateCaseComment(FIRST_CASE_ID, comment.getId(), comment.getAuthor(), "System User unknown Updated " + comment.getComment(), "owner");

            caseComments = caseService.getCaseComments(FIRST_CASE_ID, new QueryContext());
            assertNotNull(caseComments);
            assertEquals(1, caseComments.size());
            comment = caseComments.iterator().next();
            assertComment(comment, "poul", "System User unknown Updated just a tiny comment");

            // john can see the updated comment
            identityProvider.setName("john");
            caseComments = caseService.getCaseComments(FIRST_CASE_ID, new QueryContext());
            assertNotNull(caseComments);
            assertEquals(1, caseComments.size());
            comment = caseComments.iterator().next();
            assertComment(comment, "poul", "System User unknown Updated just a tiny comment");

            // mary still cannot see anything
            identityProvider.setName("mary");
            caseComments = caseService.getCaseComments(FIRST_CASE_ID, new QueryContext());
            assertNotNull(caseComments);
            assertEquals(0, caseComments.size());

            try {
                caseService.removeCaseComment(FIRST_CASE_ID, comment.getId());
                fail("mary should not be able to remove comment that she has no access to");
            } catch (SecurityException e) {
                // mary is not allowed to removed comments that she has no access to
                assertTrue(e.getMessage().contains("User mary does not have access to comment"));
            }

            identityProvider.setName("john");
            caseComments = caseService.getCaseComments(FIRST_CASE_ID, CommentSortBy.Author, new QueryContext());
            assertEquals(1, caseComments.size());

            identityProvider.setName(AuthorizationManager.UNKNOWN_USER);
            caseService.removeCaseComment(FIRST_CASE_ID, comment.getId());

            caseComments = caseService.getCaseComments(FIRST_CASE_ID, CommentSortBy.Author, new QueryContext());
            assertEquals(0, caseComments.size());

            identityProvider.setName("john");
            caseComments = caseService.getCaseComments(FIRST_CASE_ID, CommentSortBy.Author, new QueryContext());
            assertEquals(0, caseComments.size());

        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                identityProvider.setName("john");
                caseService.cancelCase(caseId);
            }
        }
    }

    @Test
    public void testAddAndRemoveMultipleDataFromCaseFile(){
        Map<String, Object> data = new HashMap<>();
        data.put("name", "my first case");
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), EMPTY_CASE_P_ID, data);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), EMPTY_CASE_P_ID, caseFile);
        Assertions.assertThat(caseId).isNotNull().isEqualTo(FIRST_CASE_ID);
        try {
            data = new HashMap<>();
            data.put("person", "john");
            data.put("car", "my first car");

            caseService.addDataToCaseFile(caseId, data);

            Collection<CaseFileItem> caseInstanceDataItems = caseRuntimeDataService.getCaseInstanceDataItems(caseId, new QueryContext());
            Map<String, String> caseFileVariables = caseInstanceDataItems.stream().collect(Collectors.toMap(CaseFileItem::getName, CaseFileItem::getValue));

            Assertions.assertThat(caseFileVariables).containsOnly(entry("person", "john"), entry("car", "my first car"), entry("name", "my first case"));

            data = new HashMap<>();
            data.put("name", "my updated case");
            data.put("car", "my second car");

            caseService.addDataToCaseFile(caseId, data);

            caseInstanceDataItems = caseRuntimeDataService.getCaseInstanceDataItems(caseId, new QueryContext());
            caseFileVariables = caseInstanceDataItems.stream().collect(Collectors.toMap(CaseFileItem::getName, CaseFileItem::getValue));

            Assertions.assertThat(caseFileVariables).containsOnly(entry("person", "john"), entry("car", "my second car"), entry("name", "my updated case"));

            caseService.removeDataFromCaseFile(caseId, "nonexisting");

            caseInstanceDataItems = caseRuntimeDataService.getCaseInstanceDataItems(caseId, new QueryContext());
            Assertions.assertThat(caseInstanceDataItems).hasSize(3);

            caseService.removeDataFromCaseFile(caseId, Arrays.asList("name", "car", "nonexisting"));

            caseInstanceDataItems = caseRuntimeDataService.getCaseInstanceDataItems(caseId, new QueryContext());
            caseFileVariables = caseInstanceDataItems.stream().collect(Collectors.toMap(CaseFileItem::getName, CaseFileItem::getValue));

            Assertions.assertThat(caseFileVariables).containsOnly(entry("person", "john"));

            caseService.removeDataFromCaseFile(caseId, "person");
            caseInstanceDataItems = caseRuntimeDataService.getCaseInstanceDataItems(caseId, new QueryContext());
            Assertions.assertThat(caseInstanceDataItems).isEmpty();
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }

    }

    @Test
    public void testCreateCaseFileInstanceForNotExistingCase() {
        assertThatExceptionOfType(CaseDefinitionNotFoundException.class)
                .isThrownBy(() -> caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), "nonexisting", Collections.emptyMap()));

        assertThatExceptionOfType(CaseDefinitionNotFoundException.class)
                .isThrownBy(() -> caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), "nonexisting", Collections.emptyMap(), Collections.emptyMap()));
    }

    @Test
    public void testGetCaseFileInstanceForNotExistingCase() {
        assertThatExceptionOfType(CaseNotFoundException.class)
                .isThrownBy(() -> caseService.getCaseFileInstance("nonexisting"));
    }
    
    @Test
    public void testStartCaseForMultiInstanceStagesTriggerAdHocFragment() {
        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), MULTI_STAGE_CASE_P_ID);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());
            
            Map<String, Object> data = new HashMap<>();
            data.put("type", "basic");
            caseService.triggerAdHocFragment(caseId, "Stage #{type}", data);
            
            Collection<CaseStageInstance> stages = caseRuntimeDataService.getCaseInstanceStages(caseId, true, new QueryContext());
            Assertions.assertThat(stages).hasSize(1);
            Assertions.assertThat(stages.iterator().next().getName()).isEqualTo("Stage basic");
            
            data = new HashMap<>();
            data.put("type", "advanced");
            caseService.triggerAdHocFragment(caseId, "Stage #{type}", data);
            
            stages = caseRuntimeDataService.getCaseInstanceStages(caseId, true, new QueryContext());
            Assertions.assertThat(stages).hasSize(2);
            Iterator<CaseStageInstance> it = stages.iterator();
            Assertions.assertThat(it.next().getName()).isEqualTo("Stage basic");
            Assertions.assertThat(it.next().getName()).isEqualTo("Stage advanced");
            
            // now trigger ad hoc fragment within give stage            
            caseService.triggerAdHocFragment(caseId, "Stage basic", "Simple task", data);
            
            List<TaskSummary> tasks = runtimeDataService.getTasksAssignedAsPotentialOwner("john", new QueryFilter());
            Assertions.assertThat(tasks).hasSize(1);
            
            caseService.cancelCase(caseId);
            CaseInstance instance = caseService.getCaseInstance(caseId);
            Assertions.assertThat(instance.getStatus()).isEqualTo(CaseStatus.CANCELLED.getId());
            caseId = null;
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }
    
    @Test
    public void testStartCaseForMultiInstanceStagesAddDynamicNodes() {
        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), MULTI_STAGE_CASE_P_ID);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());
            
            Map<String, Object> data = new HashMap<>();
            data.put("type", "basic");
            caseService.triggerAdHocFragment(caseId, "Stage #{type}", data);
            
            Collection<CaseStageInstance> stages = caseRuntimeDataService.getCaseInstanceStages(caseId, true, new QueryContext());
            Assertions.assertThat(stages).hasSize(1);
            Assertions.assertThat(stages.iterator().next().getName()).isEqualTo("Stage basic");
      
            // now add dynamic task to given stage
            TaskSpecification taskSpec = caseService.newHumanTaskSpec("Basic", "just a task", "john", null, null);
            caseService.addDynamicTaskToStage(caseId, "Stage basic", taskSpec);
            
            List<TaskSummary> tasks = runtimeDataService.getTasksAssignedAsPotentialOwner("john", new QueryFilter());
            Assertions.assertThat(tasks).hasSize(1);
            Assertions.assertThat(tasks.get(0).getName()).isEqualTo("Basic");
            Assertions.assertThat(tasks.get(0).getDescription()).isEqualTo("just a task");
            
            Map<String, Object> parameters = new HashMap<>();
            caseService.addDynamicSubprocessToStage(caseId, "Stage basic", SUBPROCESS_P_ID, parameters);

            // second task add by process instance id
            Collection<ProcessInstanceDesc> caseProcessInstances = caseRuntimeDataService.getProcessInstancesForCase(caseId, new QueryContext());            
            Assertions.assertThat(caseProcessInstances).hasSize(2);
            
            caseService.cancelCase(caseId);
            CaseInstance instance = caseService.getCaseInstance(caseId);
            Assertions.assertThat(instance.getStatus()).isEqualTo(CaseStatus.CANCELLED.getId());
            caseId = null;
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }
    
    @Test
    public void testStartEmptyCaseWithCaseFileValueTooLong() {
        System.setProperty("org.jbpm.cases.var.log.length", "10");
        Map<String, Object> data = new HashMap<>();
        data.put("name", "my first case data bit too long");
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), EMPTY_CASE_P_ID, data);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), EMPTY_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId, true, false, false, false);
            assertNotNull(cInstance);
            assertEquals(FIRST_CASE_ID, cInstance.getCaseId());
            assertNotNull(cInstance.getCaseFile());
            assertEquals("my first case data bit too long", cInstance.getCaseFile().getData("name"));
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());

            Collection<VariableDesc> vars = runtimeDataService.getVariablesCurrentState(((CaseInstanceImpl) cInstance).getProcessInstanceId());
            assertNotNull(vars);
            assertEquals(3, vars.size());
            Map<String, Object> mappedVars = vars.stream().collect(toMap(v -> v.getVariableId(), v -> v.getNewValue()));
            assertEquals("my first case data bit too long", mappedVars.get("name"));
            assertEquals(FIRST_CASE_ID, mappedVars.get("CaseId"));
            assertEquals("john", mappedVars.get("initiator"));
            
            Collection<CaseFileItem> caseFileItems = caseRuntimeDataService.getCaseInstanceDataItems(caseId, new QueryContext());
            assertNotNull(caseFileItems);
            assertEquals(1, caseFileItems.size());
            mappedVars = caseFileItems.stream().collect(toMap(cs -> cs.getName(), cs -> cs.getValue()));
            assertEquals("my first c", mappedVars.get("name"));           
            
            caseService.cancelCase(caseId);
            CaseInstance instance = caseService.getCaseInstance(caseId);
            Assertions.assertThat(instance.getStatus()).isEqualTo(CaseStatus.CANCELLED.getId());
            caseId = null;
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
            System.clearProperty("org.jbpm.cases.var.log.length");
        }
    }
    
    @Test
    public void testCaseWithIndexedFromVariableUpdates() {
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl("john"));
        roleAssignments.put("participant", new UserImpl("mary"));
        
        Map<String, Object> data = new HashMap<>();
        data.put("contactInfo", "main street 10, NYC");
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), USER_TASK_DATA_CASE_P_ID, data, roleAssignments);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), USER_TASK_DATA_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId, true, false, false, false);
            assertNotNull(cInstance);
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());
            
            Map<String, Object> caseData = cInstance.getCaseFile().getData();
            assertNotNull(caseData);
            assertEquals(1, caseData.size());            
            assertEquals("main street 10, NYC", caseData.get("contactInfo"));
            
            Collection<CaseFileItem> dataItems = caseRuntimeDataService.getCaseInstanceDataItems(caseId, new QueryContext());
            assertThat(dataItems).isNotNull().hasSize(1);
                        
            Map<String, CaseFileItem> mappedDataItems = dataItems.stream().collect(toMap(CaseFileItem::getName, t -> t));
            assertThat(mappedDataItems).containsKeys("contactInfo");
         
            identityProvider.setName("john");
            List<TaskSummary> tasks = caseRuntimeDataService.getCaseTasksAssignedAsPotentialOwner(caseId, "john", null, new QueryContext());
            assertThat(tasks).isNotNull().hasSize(1);
            
            Map<String, Object> results = new HashMap<>();
            results.put("reply_", "here is my reply");
            
            userTaskService.completeAutoProgress(tasks.get(0).getId(), "john", results);
            
            cInstance = caseService.getCaseInstance(caseId, true, false, false, false);
            assertNotNull(cInstance);
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());
            
            caseData = cInstance.getCaseFile().getData();
            assertNotNull(caseData);
            assertEquals(2, caseData.size());            
            assertEquals("main street 10, NYC", caseData.get("contactInfo"));
            assertEquals("here is my reply", caseData.get("reply"));
            
            dataItems = caseRuntimeDataService.getCaseInstanceDataItems(caseId, new QueryContext());
            assertThat(dataItems).isNotNull().hasSize(2);
                        
            mappedDataItems = dataItems.stream().collect(toMap(CaseFileItem::getName, t -> t));
            assertThat(mappedDataItems).containsKeys("contactInfo", "reply");
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                identityProvider.setName("john");
                caseService.cancelCase(caseId);
            }
        }
    }
    
    @Test
    public void testCaseWithIndexedFromVariableUpdatesResetVariable() {
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl("john"));
        roleAssignments.put("participant", new UserImpl("mary"));
        
        Map<String, Object> data = new HashMap<>();
        data.put("contactInfo", "main street 10, NYC");
        data.put("reply", "here is my reply");
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), USER_TASK_DATA_CASE_P_ID, data, roleAssignments);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), USER_TASK_DATA_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId, true, false, false, false);
            assertNotNull(cInstance);
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());
            
            Map<String, Object> caseData = cInstance.getCaseFile().getData();
            assertNotNull(caseData);
            assertEquals(2, caseData.size());            
            assertEquals("main street 10, NYC", caseData.get("contactInfo"));
            assertEquals("here is my reply", caseData.get("reply"));
            
            Collection<CaseFileItem> dataItems = caseRuntimeDataService.getCaseInstanceDataItems(caseId, new QueryContext());
            assertThat(dataItems).isNotNull().hasSize(2);
                        
            Map<String, CaseFileItem> mappedDataItems = dataItems.stream().collect(toMap(CaseFileItem::getName, t -> t));
            assertThat(mappedDataItems).containsKeys("contactInfo", "reply");
         
            identityProvider.setName("john");
            List<TaskSummary> tasks = caseRuntimeDataService.getCaseTasksAssignedAsPotentialOwner(caseId, "john", null, new QueryContext());
            assertThat(tasks).isNotNull().hasSize(1);
            
            Map<String, Object> results = new HashMap<>();
            results.put("reply_", null);
            
            userTaskService.completeAutoProgress(tasks.get(0).getId(), "john", results);
            
            cInstance = caseService.getCaseInstance(caseId, true, false, false, false);
            assertNotNull(cInstance);
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());
            
            caseData = cInstance.getCaseFile().getData();
            assertNotNull(caseData);
            assertEquals(1, caseData.size());            
            assertEquals("main street 10, NYC", caseData.get("contactInfo"));            
            
            dataItems = caseRuntimeDataService.getCaseInstanceDataItems(caseId, new QueryContext());
            assertThat(dataItems).isNotNull().hasSize(1);
                        
            mappedDataItems = dataItems.stream().collect(toMap(CaseFileItem::getName, t -> t));
            assertThat(mappedDataItems).containsKeys("contactInfo");
            assertThat(mappedDataItems).doesNotContainKeys("reply");
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                identityProvider.setName("john");
                caseService.cancelCase(caseId);
            }
        }
    }

    @Before
    public void setUp() throws Exception {
        registerListenerMvelDefinition("org.jbpm.casemgmt.impl.util.CountDownListenerFactory.get(\"" +
                                       getClass().getSimpleName() + "1\", " +
                                       "\"end 1\", 1)");
        registerListenerMvelDefinition("org.jbpm.casemgmt.impl.util.CountDownListenerFactory.get(\"" +
                                       getClass().getSimpleName() + "2\", " +
                                       "\"Stage 1\", 1)");
        super.setUp();
    }

    @After
    public void tear() {
        CountDownListenerFactory.reset();
    }

    @Test
    public void testCaseWithStageAndBoundaryTimerFired() throws InterruptedException {
        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), "CaseWithStageAndBoundaryTimer");
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);

        // wait till we hit end node
        ((NodeLeftCountDownProcessEventListener) CountDownListenerFactory.getExisting(getClass().getSimpleName() + "1")).waitTillCompleted();
        
        try {
            Collection<CaseStageInstance> stages = caseRuntimeDataService.getCaseInstanceStages(caseId, false, null);
            assertThat(stages).isNotNull().hasSize(1);
            Iterator<CaseStageInstance> iterator = stages.iterator();

            CaseStageInstance stage1 = iterator.next();
            assertThat(stage1.getName()).isEqualTo("Stage 1");
            assertThat(stage1.getStatus()).isEqualTo(StageStatus.Completed);

            caseService.cancelCase(caseId);
            CaseInstance instance = caseService.getCaseInstance(caseId);
            assertThat(instance.getStatus()).isEqualTo(CaseStatus.CANCELLED.getId());
            caseId = null;
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }



    @Test
    public void testCaseWithBoundaryTimerFiredAtStage() throws InterruptedException {

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), "CaseWithBoundaryTimerStage");
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);

        // wait till we hit end node
        ((NodeLeftCountDownProcessEventListener) CountDownListenerFactory.getExisting(getClass().getSimpleName() + "2")).waitTillCompleted();
        try {
            Collection<CaseStageInstance> stages = caseRuntimeDataService.getCaseInstanceStages(caseId, false, null);
            assertThat(stages).isNotNull().hasSize(2);
            Iterator<CaseStageInstance> iterator = stages.iterator();

            CaseStageInstance stage1 = iterator.next();
            assertThat(stage1.getName()).isEqualTo("Stage 1");
            assertThat(stage1.getStatus()).isEqualTo(StageStatus.Completed);

            CaseStageInstance stage2 = iterator.next();
            assertThat(stage2.getName()).isEqualTo("Stage 2");
            assertThat(stage2.getStatus()).isEqualTo(StageStatus.Active);

            caseService.cancelCase(caseId);
            CaseInstance instance = caseService.getCaseInstance(caseId);
            assertThat(instance.getStatus()).isEqualTo(CaseStatus.CANCELLED.getId());
            caseId = null;
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }
    
    @Test(timeout=15000)
    public void testCaseWithHumanTaskAfterReopen() {
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl("john"));
        
        Map<String, Object> data = new HashMap<>();
        data.put("contactInfo", "main street 10, NYC");
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), USER_TASK_DATA_CASE_P_ID, data, roleAssignments);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), USER_TASK_DATA_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId, true, false, false, false);
            assertNotNull(cInstance);
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());
            
            identityProvider.setName("john");
            List<TaskSummary> tasks = caseRuntimeDataService.getCaseTasksAssignedAsPotentialOwner(caseId, "john", null, new QueryContext());
            assertThat(tasks).isNotNull().hasSize(1);
            
            Map<String, Object> results = new HashMap<>();
            results.put("reply_", "here is my reply");
            
            userTaskService.completeAutoProgress(tasks.get(0).getId(), "john", results);
            
            caseService.closeCase(caseId, "closed temporarily");
            caseService.reopenCase(caseId, deploymentUnit.getIdentifier(), USER_TASK_DATA_CASE_P_ID);
            cInstance = caseService.getCaseInstance(caseId, true, false, false, false);
            assertNotNull(cInstance);
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());
            
            Map<String, Object> caseData = cInstance.getCaseFile().getData();
            assertNotNull(caseData);
            assertEquals(2, caseData.size());  
            assertEquals("main street 10, NYC", caseData.get("contactInfo"));
            assertEquals("here is my reply", caseData.get("reply"));
            
            Collection<CaseFileItem> dataItems = caseRuntimeDataService.getCaseInstanceDataItems(caseId, new QueryContext());
            assertThat(dataItems).isNotNull().hasSize(2);
                        
            Map<String, CaseFileItem> mappedDataItems = dataItems.stream().collect(toMap(CaseFileItem::getName, t -> t));
            assertThat(mappedDataItems).containsKeys("reply", "contactInfo");
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                identityProvider.setName("john");
                caseService.cancelCase(caseId);
            }
        }
    }
    
    @Test(timeout=15000)
    public void testCaseWithBoundaryTimerFiredAtStageAndThenReopen() throws InterruptedException {
        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), STAGE_WITH_BOUNDARY_EVENT_CONDITION);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        
        Collection<CaseStageInstance> activeStages = caseRuntimeDataService.getCaseInstanceStages(caseId, true, new QueryContext());
        assertThat(activeStages).hasSize(0);

        caseService.addDataToCaseFile(caseId, "readyToActivate", true);
        
        activeStages = caseRuntimeDataService.getCaseInstanceStages(caseId, true, new QueryContext());
        assertThat(activeStages).hasSize(1);
        
        // wait till we hit end node
        ((NodeLeftCountDownProcessEventListener) CountDownListenerFactory.getExisting(getClass().getSimpleName() + "2")).waitTillCompleted();
        try {
            Collection<CaseStageInstance> stages = caseRuntimeDataService.getCaseInstanceStages(caseId, false, null);
            assertThat(stages).isNotNull().hasSize(2);
            Iterator<CaseStageInstance> iterator = stages.iterator();

            CaseStageInstance stage = iterator.next();
            assertThat(stage.getName()).isEqualTo("Stage 1");
            assertThat(stage.getStatus()).isEqualTo(StageStatus.Completed);

            caseService.cancelCase(caseId);
            
            caseService.reopenCase(caseId, deploymentUnit.getIdentifier(), STAGE_WITH_BOUNDARY_EVENT_CONDITION);
            
            activeStages = caseRuntimeDataService.getCaseInstanceStages(caseId, true, new QueryContext());
            //Stage 1 should be not active as boundary event was fired and completion condition set,
            //and Stage 2 should be
            assertThat(activeStages).hasSize(1);
            stage = activeStages.iterator().next();
            assertThat(stage.getName()).isEqualTo("Stage 2");
            assertThat(stage.getStatus()).isEqualTo(StageStatus.Active);
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }
    
    @Test(timeout=15000)
    public void testReopenAfterAddingDynamicSubprocess() {
        Map<String, Object> data = new HashMap<>();
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), USER_TASK_CASE_P_ID, data);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), USER_TASK_STAGE_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals(FIRST_CASE_ID, cInstance.getCaseId());
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());

            CaseDefinition caseDef = caseRuntimeDataService.getCase(deploymentUnit.getIdentifier(), USER_TASK_STAGE_CASE_P_ID);
            assertNotNull(caseDef);
            assertEquals(1, caseDef.getCaseStages().size());
            assertEquals(deploymentUnit.getIdentifier(), caseDef.getDeploymentId());

            CaseStage stage = caseDef.getCaseStages().iterator().next();

            // add dynamic user task to empty case instance - first by case id
            Map<String, Object> parameters = new HashMap<>();
                        
            caseService.addDynamicSubprocessToStage(FIRST_CASE_ID, stage.getId(), DYNAMIC_SUBPROCESS_P_ID, parameters);
            
            caseService.addDataToCaseFile(FIRST_CASE_ID, "stage_subprocess_finished", true);
            
            caseService.cancelCase(FIRST_CASE_ID);
            caseService.reopenCase(FIRST_CASE_ID, deploymentUnit.getIdentifier(), USER_TASK_STAGE_CASE_P_ID);
            
            Map<String, Object> caseData = caseService.getCaseFileInstance(FIRST_CASE_ID).getData();
            assertNotNull(caseData);
            assertEquals("is not easy to say", caseData.get("mySecret"));
            
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }

    @Test
    public void testCaseWithRequiredCaseFileItem() {
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl("john"));

        Map<String, Object> data = new HashMap<>();
        data.put("s", "this is a required value");
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), USER_TASK_REQUIRED_V_CASE_P_ID, data, roleAssignments);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), USER_TASK_REQUIRED_V_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals(FIRST_CASE_ID, cInstance.getCaseId());
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());

        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }
    
    @Test
    public void testCaseWithMissingRequiredCaseFileItem() {
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl("john"));

        Map<String, Object> data = new HashMap<>();
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), USER_TASK_REQUIRED_V_CASE_P_ID, data, roleAssignments);

        assertThatExceptionOfType(VariableViolationException.class)
            .isThrownBy(() -> caseService.startCase(deploymentUnit.getIdentifier(), USER_TASK_REQUIRED_V_CASE_P_ID, caseFile));
      
        
        assertThatExceptionOfType(CaseNotFoundException.class)
            .isThrownBy(() -> caseService.getCaseInstance(FIRST_CASE_ID));
        
    }
    
    @Test
    public void testCaseWithMissingRequiredEmptyCaseFileItem() {
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl("john"));

        Map<String, Object> data = new HashMap<>();
        data.put("s", "");
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), USER_TASK_REQUIRED_V_CASE_P_ID, data, roleAssignments);

        assertThatExceptionOfType(VariableViolationException.class)
            .isThrownBy(() -> caseService.startCase(deploymentUnit.getIdentifier(), USER_TASK_REQUIRED_V_CASE_P_ID, caseFile));
      
        
        assertThatExceptionOfType(CaseNotFoundException.class)
            .isThrownBy(() -> caseService.getCaseInstance(FIRST_CASE_ID));
        
    }
    
    @Test
    public void testCaseWithRestrictedCaseFileItem() {
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl("john"));

        Map<String, Object> data = new HashMap<>();
        data.put("s", "first restricted value");
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), USER_TASK_RESTRICTED_V_CASE_P_ID, data, roleAssignments);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), USER_TASK_RESTRICTED_V_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals(FIRST_CASE_ID, cInstance.getCaseId());
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());
            
            caseService.cancelCase(caseId);
            data.put("s", NEW_RESTRICTED_VALUE);
            caseService.reopenCase(FIRST_CASE_ID, deploymentUnit.getIdentifier(), USER_TASK_RESTRICTED_V_CASE_P_ID, data);
            Map<String, Object> caseData = caseService.getCaseFileInstance(FIRST_CASE_ID).getData();
            assertNotNull(caseData);
            assertEquals(NEW_RESTRICTED_VALUE, caseData.get("s"));
            
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }
    
    @Test
    public void testCaseWithViolatingRestrictedCaseFileItem() {
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl("john"));

        Map<String, Object> data = new HashMap<>();
        data.put("s", "this is a restricted value");
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), USER_TASK_RESTRICTED_V_CASE_P_ID, data, roleAssignments);

        assertThatExceptionOfType(VariableViolationException.class)
        .isThrownBy(() -> caseService.startCase(deploymentUnit.getIdentifier(), USER_TASK_RESTRICTED_V_CASE_P_ID, caseFile));
    
        assertThatExceptionOfType(CaseNotFoundException.class)
        .isThrownBy(() -> caseService.getCaseInstance(FIRST_CASE_ID));
    }
    
    @Test
    public void testCaseReopenWithViolatingRestrictedCaseFileItem() {
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl("john"));

        Map<String, Object> data = new HashMap<>();
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), USER_TASK_RESTRICTED_V_CASE_P_ID, data, roleAssignments);
        
        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), USER_TASK_RESTRICTED_V_CASE_P_ID, caseFile);
        
        CaseInstance cInstance = caseService.getCaseInstance(caseId);
        assertNotNull(cInstance);
        assertEquals(FIRST_CASE_ID, cInstance.getCaseId());
        assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());
        
        caseService.cancelCase(caseId);
        data.put("s", NEW_RESTRICTED_VALUE);
        
        assertThatExceptionOfType(VariableViolationException.class)
        .isThrownBy(() -> caseService.reopenCase(FIRST_CASE_ID, deploymentUnit.getIdentifier(), USER_TASK_RESTRICTED_V_CASE_P_ID, data));
    }
    
    @Test
    public void testCaseReopenWithViolatingReadOnlyCaseFileItemReopen() {
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl("john"));

        Map<String, Object> data = new HashMap<>();
        data.put("r", "unmodifiable value");
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), USER_TASK_READONLY_V_CASE_P_ID, data, roleAssignments);
        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), USER_TASK_READONLY_V_CASE_P_ID, caseFile);

        CaseInstance cInstance = caseService.getCaseInstance(caseId);
        assertNotNull(cInstance);
        assertEquals(FIRST_CASE_ID, cInstance.getCaseId());
        assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());

        caseService.cancelCase(caseId);
        data.put("r", "modifiedvalue");

        assertThatExceptionOfType(VariableViolationException.class)
                                                                   .isThrownBy(() -> caseService.reopenCase(FIRST_CASE_ID, deploymentUnit.getIdentifier(), USER_TASK_READONLY_V_CASE_P_ID, data));
    }
    
    @Test
    public void testCaseViolatingReadOnlyCaseFileItemAddData() {
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl("john"));

        Map<String, Object> data = new HashMap<>();
        data.put("r", "unmodifiable value");
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), USER_TASK_READONLY_V_CASE_P_ID, data, roleAssignments);
        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), USER_TASK_READONLY_V_CASE_P_ID, caseFile);

        CaseInstance cInstance = caseService.getCaseInstance(caseId);
        assertNotNull(cInstance);
        assertEquals(FIRST_CASE_ID, cInstance.getCaseId());
        assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());

        assertThatExceptionOfType(VariableViolationException.class)
                                                                   .isThrownBy(() -> caseService.addDataToCaseFile(caseId,"r", "modidiedValue")); 
        assertThatExceptionOfType(VariableViolationException.class)
        .isThrownBy(() -> caseService.addDataToCaseFile(caseId,Collections.singletonMap("r", "modidiedValue")));
    }
    
    @Test
    public void testCaseReadOnlyCaseFileItemAddDataOnce() {
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl("john"));

        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), USER_TASK_READONLY_V_CASE_P_ID, Collections.emptyMap(), roleAssignments);
        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), USER_TASK_READONLY_V_CASE_P_ID, caseFile);

        CaseInstance cInstance = caseService.getCaseInstance(caseId);
        assertNotNull(cInstance);
        assertEquals(FIRST_CASE_ID, cInstance.getCaseId());
        assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());
        
        final String newValue = "newValue";
        caseService.addDataToCaseFile(caseId,"r", newValue);
        Map<String, Object> caseData = caseService.getCaseFileInstance(FIRST_CASE_ID).getData();
        assertEquals(newValue, caseData.get("r"));
    }
    
    @Test
    public void testCaseReadOnlyCaseFileItemRemoveData() {
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl("john"));
        
        Map<String, Object> data = new HashMap<>();
        data.put("r", "unmodifiable value");
      

        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), USER_TASK_READONLY_V_CASE_P_ID, data, roleAssignments);
        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), USER_TASK_READONLY_V_CASE_P_ID, caseFile);

        CaseInstance cInstance = caseService.getCaseInstance(caseId);
        assertNotNull(cInstance);
        assertEquals(FIRST_CASE_ID, cInstance.getCaseId());
        assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());
        
        assertThatExceptionOfType(VariableViolationException.class).isThrownBy(() -> caseService.removeDataFromCaseFile(caseId,"r"));
    }

    
    @Test
    public void testAddDataCaseWithViolatingRestrictedCaseFileItem() {
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl("john"));

        Map<String, Object> data = new HashMap<>();
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), USER_TASK_RESTRICTED_V_CASE_P_ID, data, roleAssignments);
        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), USER_TASK_RESTRICTED_V_CASE_P_ID, caseFile);
        CaseInstance cInstance = caseService.getCaseInstance(caseId);
        assertNotNull(cInstance);
        assertEquals(FIRST_CASE_ID, cInstance.getCaseId());
        assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());
        
        assertThatExceptionOfType(VariableViolationException.class)
        .isThrownBy(() -> caseService.addDataToCaseFile(caseId, "s", NEW_RESTRICTED_VALUE));
    }
    
    @Test
    public void testCaseWithRequiredRestrictedCaseFileItem() {
        Map<String, OrganizationalEntity> roleAssignments = new HashMap<>();
        roleAssignments.put("owner", new UserImpl("john"));

        Map<String, Object> data = new HashMap<>();
        data.put("s", "required and restricted value");
        CaseFileInstance caseFile = caseService.newCaseFileInstance(deploymentUnit.getIdentifier(), USER_TASK_REQUIRED_RESTRICTED_V_CASE_P_ID, data, roleAssignments);

        String caseId = caseService.startCase(deploymentUnit.getIdentifier(), USER_TASK_REQUIRED_RESTRICTED_V_CASE_P_ID, caseFile);
        assertNotNull(caseId);
        assertEquals(FIRST_CASE_ID, caseId);
        try {
            CaseInstance cInstance = caseService.getCaseInstance(caseId);
            assertNotNull(cInstance);
            assertEquals(FIRST_CASE_ID, cInstance.getCaseId());
            assertEquals(deploymentUnit.getIdentifier(), cInstance.getDeploymentId());
        } catch (Exception e) {
            logger.error("Unexpected error {}", e.getMessage(), e);
            fail("Unexpected exception " + e.getMessage());
        } finally {
            if (caseId != null) {
                caseService.cancelCase(caseId);
            }
        }
    }
    
    @Override
    protected List<ObjectModel> getProcessListeners() {
        List<ObjectModel> listeners = super.getProcessListeners();

        if (RESTRICTED_TESTS.contains(name.getMethodName())) {
          identityProvider.setRoles(Arrays.asList("admin"));
          listeners.add(new ObjectModel("mvel", "new org.jbpm.process.instance.event.listeners.VariableGuardProcessEventListener(\"admin\", identityProvider)"));
        } else if (VIOLATING_RESTRICTED_TESTS.contains(name.getMethodName())) {
          identityProvider.setRoles(Arrays.asList("normal"));
          listeners.add(new ObjectModel("mvel", "new org.jbpm.process.instance.event.listeners.VariableGuardProcessEventListener(\"admin\", identityProvider)"));
        }

        return listeners;
    }
}
