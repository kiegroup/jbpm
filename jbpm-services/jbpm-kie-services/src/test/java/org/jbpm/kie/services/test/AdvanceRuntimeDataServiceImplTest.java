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

package org.jbpm.kie.services.test;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.kie.test.util.AbstractKieServicesBaseTest;
import org.jbpm.services.api.AdvanceRuntimeDataService;
import org.jbpm.services.api.ProcessInstanceNotFoundException;
import org.jbpm.services.api.model.DeploymentUnit;
import org.jbpm.services.api.model.ProcessInstanceWithVarsDesc;
import org.jbpm.services.api.model.UserTaskInstanceDesc;
import org.jbpm.services.api.model.UserTaskInstanceWithPotOwnerDesc;
import org.jbpm.services.api.query.model.QueryParam;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.query.QueryContext;
import org.kie.scanner.KieMavenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.jbpm.services.api.AdvanceRuntimeDataService.PROCESS_ATTR_CORRELATION_KEY;
import static org.jbpm.services.api.AdvanceRuntimeDataService.PROCESS_ATTR_DEFINITION_ID;
import static org.jbpm.services.api.AdvanceRuntimeDataService.PROCESS_ATTR_DEPLOYMENT_ID;
import static org.jbpm.services.api.AdvanceRuntimeDataService.PROCESS_COLLECTION_VARIABLES;
import static org.jbpm.services.api.AdvanceRuntimeDataService.TASK_ATTR_NAME;
import static org.jbpm.services.api.AdvanceRuntimeDataService.TASK_ATTR_OWNER;
import static org.jbpm.services.api.query.model.QueryParam.all;
import static org.jbpm.services.api.query.model.QueryParam.any;
import static org.jbpm.services.api.query.model.QueryParam.between;
import static org.jbpm.services.api.query.model.QueryParam.equalsTo;
import static org.jbpm.services.api.query.model.QueryParam.exclude;
import static org.jbpm.services.api.query.model.QueryParam.greaterOrEqualTo;
import static org.jbpm.services.api.query.model.QueryParam.greaterThan;
import static org.jbpm.services.api.query.model.QueryParam.history;
import static org.jbpm.services.api.query.model.QueryParam.in;
import static org.jbpm.services.api.query.model.QueryParam.isNotNull;
import static org.jbpm.services.api.query.model.QueryParam.isNull;
import static org.jbpm.services.api.query.model.QueryParam.likeTo;
import static org.jbpm.services.api.query.model.QueryParam.list;
import static org.jbpm.services.api.query.model.QueryParam.lowerOrEqualTo;
import static org.jbpm.services.api.query.model.QueryParam.lowerThan;
import static org.jbpm.services.api.query.model.QueryParam.notEqualsTo;
import static org.jbpm.services.api.query.model.QueryParam.notIn;
import static org.jbpm.services.api.query.model.QueryParam.type;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.kie.internal.task.api.TaskVariable.VariableType.INPUT;
import static org.kie.internal.task.api.TaskVariable.VariableType.OUTPUT;
import static org.kie.scanner.KieMavenRepository.getKieMavenRepository;

@RunWith(Parameterized.class)
public class AdvanceRuntimeDataServiceImplTest extends AbstractKieServicesBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(AdvanceRuntimeDataServiceImplTest.class);

    @Parameters(name = "Pagination {0}")
    public static Iterable<? extends Object> data() {
        return Arrays.asList(0, 2);
    }

    private List<DeploymentUnit> units = new ArrayList<>();


    private List<Long> processIds;
    private KModuleDeploymentUnit deploymentUnit = null;
    private QueryContext queryContext;

    public AdvanceRuntimeDataServiceImplTest(Integer count) {
        this.queryContext = new QueryContext(0, count);
    }

    @Before
    public void prepare() {

        configureServices();
        logger.debug("Preparing kjar");
        KieServices ks = KieServices.Factory.get();
        ReleaseId releaseId = ks.newReleaseId(GROUP_ID, ARTIFACT_ID, VERSION);
        List<String> processes = new ArrayList<>();
        processes.add("repo/processes/general/SingleHumanTaskWithVarsA.bpmn2");
        processes.add("repo/processes/general/SingleHumanTaskWithVarsB.bpmn2");
        processes.add("repo/processes/general/SingleHumanTaskWithVarsD.bpmn2");

        InternalKieModule kJar1 = createKieJar(ks, releaseId, processes);
        File pom = new File("target/kmodule", "pom.xml");
        pom.getParentFile().mkdir();
        try (FileOutputStream fs = new FileOutputStream(pom);) {
            fs.write(getPom(releaseId).getBytes());
        } catch (Exception e) {

        }
        KieMavenRepository repository = getKieMavenRepository();
        repository.deployArtifact(releaseId, kJar1, pom);

        assertNotNull(deploymentService);

        deploymentUnit = new KModuleDeploymentUnit(GROUP_ID, ARTIFACT_ID, VERSION);

        deploymentService.deploy(deploymentUnit);
        units.add(deploymentUnit);
        assertNotNull(processService);

        processIds = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Map<String, Object> inputsA1 = new HashMap<>();
            inputsA1.put("var_a", "a" + (i % 3));
            inputsA1.put("var_b", (i % 3));
            processIds.add(processService.startProcess(deploymentUnit.getIdentifier(), "test.test_A", inputsA1));
        }
        for (int i = 0; i < 10; i++) {
            Map<String, Object> inputsB1 = new HashMap<>();
            inputsB1.put("var_a", "b" + (i % 3));
            inputsB1.put("var_b", (i % 3));
            processIds.add(processService.startProcess(deploymentUnit.getIdentifier(), "test.test_B", inputsB1));
        }
    }

    @After
    public void cleanup() {
        for (Long processInstanceId : processIds) {
            try {
                // let's abort process instance to leave the system in clear state
                processService.abortProcessInstance(processInstanceId);

                ProcessInstance pi = processService.getProcessInstance(processInstanceId);
                assertNull(pi);
            } catch (ProcessInstanceNotFoundException e) {
                // ignore it as it was already completed/aborted
            }
        }
        cleanupSingletonSessionId();
        if (units != null && !units.isEmpty()) {
            for (DeploymentUnit unit : units) {
                try {
                    deploymentService.undeploy(unit);
                } catch (Exception e) {
                    // do nothing in case of some failed tests to avoid next test to fail as well
                }
            }
            units.clear();
        }
        close();
    }

    @Test
    public void testQueryProcessByVariables() {
        List<QueryParam> variables = list(equalsTo("var_a", "a1"), equalsTo("var_b", "1"));
        List<QueryParam> attributes = list(equalsTo(PROCESS_ATTR_DEFINITION_ID, "test.test_A"));

        List<ProcessInstanceWithVarsDesc> data = advanceVariableDataService.queryProcessByVariables(attributes, variables, queryContext);
        if (queryContext.getCount() > 0) {
            assertThat(data.size(), is(queryContext.getCount()));
        }
        for (ProcessInstanceWithVarsDesc p : data) {
            assertEquals("a1", p.getVariables().get("var_a"));
            assertEquals("1", p.getVariables().get("var_b"));
            assertEquals("test.test_A", p.getProcessId());
        }
    }

    @Test
    public void testQueryInOperator() {

        List<QueryParam> variables = list(in("var_a", "a1", "a2"));
        List<QueryParam> attributes = list(equalsTo(PROCESS_ATTR_DEFINITION_ID, "test.test_A"));

        List<ProcessInstanceWithVarsDesc> data = advanceVariableDataService.queryProcessByVariables(attributes, variables, queryContext);
        if (queryContext.getCount() > 0) {
            assertThat(data.size(), is(queryContext.getCount()));
        }
        List<String> values = Arrays.asList("a1", "a2");
        for (ProcessInstanceWithVarsDesc p : data) {
            assertTrue(values.contains(p.getVariables().get("var_a")));
            assertEquals("test.test_A", p.getProcessId());
        }
    }

    @Test
    public void testQueryLikeToOperator() {

        List<QueryParam> variables = list(likeTo("var_a", false, "a%"));
        List<QueryParam> attributes = list(likeTo(PROCESS_ATTR_DEFINITION_ID, false, "%test_A%"));

        List<ProcessInstanceWithVarsDesc> data = advanceVariableDataService.queryProcessByVariables(attributes, variables, queryContext);
        if (queryContext.getCount() > 0) {
            assertThat(data.size(), is(queryContext.getCount()));
        }

        for (ProcessInstanceWithVarsDesc p : data) {
            assertTrue(((String) p.getVariables().get("var_a")).startsWith("a"));
            assertEquals("test.test_A", p.getProcessId());
        }
    }

    @Test
    public void testQueryProcVarsExclusion() {

        List<QueryParam> variables = list(likeTo("var_a", false, "a%"));
        List<QueryParam> attributes = list(exclude(PROCESS_COLLECTION_VARIABLES));

        List<ProcessInstanceWithVarsDesc> data = advanceVariableDataService.queryProcessByVariables(attributes, variables, queryContext);
        if (queryContext.getCount() > 0) {
            assertEquals((int) queryContext.getCount(), data.size());
        }

        for (ProcessInstanceWithVarsDesc p : data) {
            assertEquals(0, p.getVariables().size());
        }
    }

    @Test
    public void testQueryIsNotNullOperator() {

        List<QueryParam> attributes = list(isNotNull(TASK_ATTR_OWNER));

        List<UserTaskInstanceWithPotOwnerDesc> data = advanceVariableDataService.queryUserTasksByVariables(attributes, emptyList(), emptyList(), emptyList(), queryContext);
        assertThat(data.size(), is(0));

    }

    @Test
    public void testQueryUserTasksGetDescription() {

        List<QueryParam> attributes = emptyList();

        List<UserTaskInstanceWithPotOwnerDesc> data = advanceVariableDataService.queryUserTasksByVariables(attributes, emptyList(), emptyList(), emptyList(), queryContext);
        if (queryContext.getCount() > 0) {
            assertThat(data.size(), is(queryContext.getCount()));
        }

        for (UserTaskInstanceWithPotOwnerDesc p : data) {
            Assert.assertNotNull(p.getDescription());
        }
    }

    @Test
    public void testQueryUserAuditTasksGetDescription() {

        List<QueryParam> attributes = list(history());

        List<UserTaskInstanceWithPotOwnerDesc> data = advanceVariableDataService.queryUserTasksByVariables(attributes, emptyList(), emptyList(), emptyList(), queryContext);
        if (queryContext.getCount() > 0) {
            assertThat(data.size(), is(queryContext.getCount()));
        }

        for (UserTaskInstanceWithPotOwnerDesc p : data) {
            Assert.assertNotNull(p.getDescription());
        }
    }

    @Test
    public void testQueryIsNullOperator() {

        List<QueryParam> attributes = list(isNull(TASK_ATTR_OWNER));

        List<UserTaskInstanceWithPotOwnerDesc> data = advanceVariableDataService.queryUserTasksByVariables(attributes, emptyList(), emptyList(), emptyList(), queryContext);
        if (queryContext.getCount() > 0) {
            assertThat(data.size(), is(queryContext.getCount()));
        }

        for (UserTaskInstanceWithPotOwnerDesc p : data) {
            Assert.assertNull(p.getActualOwner());
            Assert.assertNotNull(p.getDescription());
        }
    }

    @Test
    public void testQueryNotInOperator() {

        List<QueryParam> variables = list(notIn("var_a", "a1", "a2"));
        List<QueryParam> attributes = list(equalsTo(PROCESS_ATTR_DEFINITION_ID, "test.test_A"));

        List<ProcessInstanceWithVarsDesc> data = advanceVariableDataService.queryProcessByVariables(attributes, variables, queryContext);
        if (queryContext.getCount() > 0) {
            assertThat(data.size(), is(queryContext.getCount()));
        }
        List<String> values = Arrays.asList("a1", "a2");
        for (ProcessInstanceWithVarsDesc p : data) {
            assertTrue(!values.contains(p.getVariables().get("var_a")));
            assertEquals("test.test_A", p.getProcessId());
        }
    }

    @Test
    public void testQueryNotEqualsOperator() {
        List<QueryParam> attributes = list(notEqualsTo(PROCESS_ATTR_DEFINITION_ID, "test.test_A"));

        List<ProcessInstanceWithVarsDesc> data = advanceVariableDataService.queryProcessByVariables(attributes, emptyList(), queryContext);
        if (queryContext.getCount() > 0) {
            assertThat(data.size(), is(queryContext.getCount()));
        }

        for (ProcessInstanceWithVarsDesc p : data) {
            Assert.assertNotEquals("test.test_A", p.getProcessId());
        }
    }

    @Test
    public void testQueryProcessByAttributes() {
        List<QueryParam> attributes = list(equalsTo(PROCESS_ATTR_DEFINITION_ID, "test.test_A"), equalsTo(PROCESS_ATTR_CORRELATION_KEY, "1"));

        List<ProcessInstanceWithVarsDesc> data = advanceVariableDataService.queryProcessByVariables(attributes, emptyList(), queryContext);
        assertThat(data.size(), is(1));
    }

    @Test
    public void testQueryTaskByVariables() {
        List<QueryParam> variables = list(equalsTo("task_in_a1", "a0"));
        List<QueryParam> attributes = list(equalsTo(PROCESS_ATTR_DEPLOYMENT_ID, "org.jbpm.test:test-module:1.0.0"));

        List<UserTaskInstanceWithPotOwnerDesc> data = advanceVariableDataService.queryUserTasksByVariables(attributes, variables, emptyList(), emptyList(), queryContext);
        if (queryContext.getCount() > 0) {
            assertThat(data.size(), is(queryContext.getCount()));
        }
        for (UserTaskInstanceWithPotOwnerDesc p : data) {
            assertEquals("a0", p.getInputdata().get("task_in_a1"));
            assertEquals("org.jbpm.test:test-module:1.0.0", p.getDeploymentId());
        }
    }

    @Test
    public void testQueryTaskExcludeByVariables() {
        List<QueryParam> variables = list(equalsTo("task_in_a1", "a0"));
        List<QueryParam> attributes = list(equalsTo(PROCESS_ATTR_DEPLOYMENT_ID, "org.jbpm.test:test-module:1.0.0"), exclude(PROCESS_COLLECTION_VARIABLES));

        List<UserTaskInstanceWithPotOwnerDesc> data = advanceVariableDataService.queryUserTasksByVariables(attributes, variables, emptyList(), emptyList(), queryContext);
        if (queryContext.getCount() > 0) {
            assertEquals((int) queryContext.getCount(), data.size());
        }
        for (UserTaskInstanceWithPotOwnerDesc p : data) {
            assertEquals(0, p.getProcessVariables().size());
        }
    }
    
    @Test
    public void testQueryTaskNotEqualsByVariables() {
        List<QueryParam> attributes = list(notEqualsTo(PROCESS_ATTR_DEFINITION_ID, "test.test_A"));

        List<UserTaskInstanceWithPotOwnerDesc> data = advanceVariableDataService.queryUserTasksByVariables(attributes, emptyList(), emptyList(), emptyList(), queryContext);
        if (queryContext.getCount() > 0) {
            assertThat(data.size(), is(queryContext.getCount()));
        }
        for (UserTaskInstanceWithPotOwnerDesc p : data) {
            Assert.assertNotEquals("test.test_A", p.getProcessId());
        }
    }

    @Test
    public void testQueryTaskCreatedOnDateRanges() {
        List<QueryParam> attributes = emptyList();

        List<String> potOwners = emptyList();
        List<UserTaskInstanceWithPotOwnerDesc> data = advanceVariableDataService.queryUserTasksByVariables(attributes, emptyList(), emptyList(), potOwners, queryContext);
        if (queryContext.getCount() > 0) {
            assertThat(data.size(), is(queryContext.getCount()));
            return;
        } else {
            assertThat(data.size(), is(20));
        }

        List<UserTaskInstanceWithPotOwnerDesc> sortedTasks = data.stream().sorted(Comparator.comparing(UserTaskInstanceDesc::getCreatedOn)).collect(Collectors.toList());
        String column = "createdOn";

        // Between
        attributes = list(between(column, sortedTasks.get(0).getCreatedOn(), sortedTasks.get(10).getCreatedOn()));
        data = advanceVariableDataService.queryUserTasksByVariables(attributes, emptyList(), emptyList(), potOwners, queryContext);
        assertThat(data.size(), is(11));

        // Greater than
        attributes = list(greaterThan(column, sortedTasks.get(1).getCreatedOn()));
        data = advanceVariableDataService.queryUserTasksByVariables(attributes, emptyList(), emptyList(), potOwners, queryContext);
        assertThat(data.size(), is(18));

        // Greater or Equals to
        attributes = list(greaterOrEqualTo(column, sortedTasks.get(1).getCreatedOn()));
        data = advanceVariableDataService.queryUserTasksByVariables(attributes, emptyList(), emptyList(), potOwners, queryContext);
        assertThat(data.size(), is(19));

        // Lower than
        attributes = list(lowerThan(column, sortedTasks.get(10).getCreatedOn()));
        data = advanceVariableDataService.queryUserTasksByVariables(attributes, emptyList(), emptyList(), potOwners, queryContext);
        assertThat(data.size(), is(10));

        // Lower or Equals to
        attributes = list(lowerOrEqualTo(column, sortedTasks.get(10).getCreatedOn()));
        data = advanceVariableDataService.queryUserTasksByVariables(attributes, emptyList(), emptyList(), potOwners, queryContext);
        assertThat(data.size(), is(11));
    }

    @Test
    public void testQueryTaskByVariablesWithOwners() {
        QueryParam potOwners = all(Collections.singletonList("katy"));
        List<UserTaskInstanceWithPotOwnerDesc> data = advanceVariableDataService.queryUserTasksByVariables(emptyList(), emptyList(), emptyList(), potOwners, queryContext);
        if (queryContext.getCount() > 0) {
            assertThat(data.size(), is(queryContext.getCount()));
        }

        for (UserTaskInstanceWithPotOwnerDesc p : data) {
            assertTrue(p.getPotentialOwners().contains("katy"));
        }
    }

    @Test
    public void testQueryTaskByVariablesWithNullOwners() {
        QueryParam potOwners = null;
        List<UserTaskInstanceWithPotOwnerDesc> data = advanceVariableDataService.queryUserTasksByVariables(emptyList(), emptyList(), emptyList(), potOwners, queryContext);
        if (queryContext.getCount() > 0) {
            assertThat(data.size(), is(queryContext.getCount()));
        }
    }

    @Test
    public void testQueryTaskByVariablesWithAllNullOwners() {
        QueryParam potOwners = all(null);
        List<UserTaskInstanceWithPotOwnerDesc> data = advanceVariableDataService.queryUserTasksByVariables(emptyList(), emptyList(), emptyList(), potOwners, queryContext);
        assertTrue(data.isEmpty());
    }

    @Test
    public void testQueryTaskByVariablesWithAnyNullOwners() {
        QueryParam potOwners = any(null);
        List<UserTaskInstanceWithPotOwnerDesc> data = advanceVariableDataService.queryUserTasksByVariables(emptyList(), emptyList(), emptyList(), potOwners, queryContext);
        assertTrue(data.isEmpty());
    }

    @Test
    public void testQueryTaskByVariablesWithAllOwners() {
        QueryParam potOwners = all(Arrays.asList("katy", "kieserver"));
        List<UserTaskInstanceWithPotOwnerDesc> data = advanceVariableDataService.queryUserTasksByVariables(emptyList(), emptyList(), emptyList(), potOwners, queryContext);
        assertEquals(0, data.size());

        potOwners = all(Arrays.asList("katy", "wbadmin"));
        data = advanceVariableDataService.queryUserTasksByVariables(emptyList(), emptyList(), emptyList(), potOwners, queryContext);
        if (queryContext.getCount() > 0) {
            assertThat(data.size(), is(queryContext.getCount()));
        }
        for (UserTaskInstanceWithPotOwnerDesc p : data) {
            assertTrue(p.getPotentialOwners().contains("katy") && p.getPotentialOwners().contains("wbadmin"));
        }
    }

    @Test
    public void testQueryTaskByVariablesWithAnyOwners() {
        QueryParam potOwners = any(Arrays.asList("katy", "kieserver"));
        List<UserTaskInstanceWithPotOwnerDesc> data = advanceVariableDataService.queryUserTasksByVariables(emptyList(), emptyList(), emptyList(), potOwners, queryContext);
        if (queryContext.getCount() > 0) {
            assertThat(data.size(), is(queryContext.getCount()));
        }
        for (UserTaskInstanceWithPotOwnerDesc p : data) {
            assertTrue(p.getPotentialOwners().contains("katy") || p.getPotentialOwners().contains("kieserver"));
        }
    }

    @Test
    public void testQueryTaskByVariablesWithByProcessVar() {
        List<QueryParam> processVariables = list(equalsTo("var_a", "a1"));

        List<UserTaskInstanceWithPotOwnerDesc> data = advanceVariableDataService.queryUserTasksByVariables(emptyList(), emptyList(), processVariables, emptyList(), queryContext);
        if (queryContext.getCount() > 0) {
            assertThat(data.size(), is(queryContext.getCount()));
        } else {
            assertThat(data.size(), is(3));
        }

    }

    @Test
    public void testQueryTaskByAttributes() {
        List<QueryParam> attributes = list(equalsTo(PROCESS_ATTR_DEFINITION_ID, "test.test_A"), equalsTo(PROCESS_ATTR_CORRELATION_KEY, "1"), equalsTo(TASK_ATTR_NAME, "Task"));

        List<UserTaskInstanceWithPotOwnerDesc> data = advanceVariableDataService.queryUserTasksByVariables(attributes, emptyList(), emptyList(), emptyList(), queryContext);
        assertThat(data.size(), is(1));
    }

    @Test
    public void testQueryTaskByAttributesOwner() {
        List<QueryParam> attributes = list(equalsTo(TASK_ATTR_OWNER, "Error"));
        List<UserTaskInstanceWithPotOwnerDesc> data = advanceVariableDataService.queryUserTasksByVariables(attributes, emptyList(), emptyList(), emptyList(), queryContext);
        assertThat(data.size(), is(0));
    }

    @Test
    public void testQueryTaskByStatus() {
        List<QueryParam> attributes = list(in(AdvanceRuntimeDataService.TASK_ATTR_STATUS,
                                              Arrays.asList("Ready")));

        List<UserTaskInstanceWithPotOwnerDesc> data = advanceVariableDataService.queryUserTasksByVariables(attributes, emptyList(), emptyList(), emptyList(), queryContext);
        if (queryContext.getCount() > 0) {
            assertThat(data.size(), is(queryContext.getCount()));
        } else {
            assertThat(data.size(), is(20));
        }

        long[] taskIds = data.stream().mapToLong(UserTaskInstanceWithPotOwnerDesc::getTaskId).toArray();
        for (long taskId : taskIds) {
            UserTaskInstanceDesc task = runtimeDataService.getTaskById(taskId);
            assertThat(task.getStatus(), is("Ready"));
        }
    }

    @Test
    public void testQueryTaskByNotStatus() {
        List<QueryParam> attributes = list(in(AdvanceRuntimeDataService.TASK_ATTR_STATUS,
                                              Arrays.asList("Created")));

        List<UserTaskInstanceWithPotOwnerDesc> data = advanceVariableDataService.queryUserTasksByVariables(attributes, emptyList(), emptyList(), emptyList(), queryContext);
        assertThat(data.size(), is(0));
    }

    @Test
    public void testQueryAllNull() {
        List<UserTaskInstanceWithPotOwnerDesc> data = advanceVariableDataService.queryUserTasksByVariables(null, null, null, (List<String>) null, queryContext);
        if (queryContext.getCount() > 0) {
            assertThat(data.size(), is(queryContext.getCount()));
        } else {
            assertThat(data.size(), is(20));
        }
    }

    @Test
    public void testQueryTaskByVariablesWithMultipleExpressionsPerVariable() {
        List<QueryParam> processVariables = list(notEqualsTo("var_a", "a1"), isNotNull("var_a"));
        List<QueryParam> variables = list(notEqualsTo("task_in_a1", "a0"), isNotNull("task_in_a1"));

        List<UserTaskInstanceWithPotOwnerDesc> data = advanceVariableDataService.queryUserTasksByVariables(emptyList(), variables, processVariables, emptyList(), queryContext);
        if (queryContext.getCount() > 0) {
            assertThat(data.size(), is(queryContext.getCount()));
        } else {
            assertThat(data.size(), is(3));
        }
        for (UserTaskInstanceWithPotOwnerDesc userTask : data) {
            assertThat(userTask.getProcessVariables().get("var_a"), notNullValue());
            assertThat(userTask.getProcessVariables().get("var_a"), is(not("a1")));

            assertThat(userTask.getInputdata().get("task_in_a1"), notNullValue());
            assertThat(userTask.getInputdata().get("task_in_a1"), is(not("a0")));
        }
    }

    @Test
    public void testQueryTaskByVariablesWithInputOutput() {
        List<QueryParam> processVariables = list(notEqualsTo("var_a", "a1"), isNotNull("var_a"));

        List<QueryParam> variablesOuput = list(notEqualsTo("task_in_a1", "a0"), isNotNull("task_in_a1"), type("task_in_a1", OUTPUT.ordinal()));
        List<UserTaskInstanceWithPotOwnerDesc> dataOutput = advanceVariableDataService.queryUserTasksByVariables(emptyList(), variablesOuput, processVariables, emptyList(), queryContext);
        assertThat(dataOutput.size(), is(0));

        List<QueryParam> variablesInput = list(notEqualsTo("task_in_a1", "a0"), isNotNull("task_in_a1"), type("task_in_a1", INPUT.ordinal()));
        List<UserTaskInstanceWithPotOwnerDesc> dataInput = advanceVariableDataService.queryUserTasksByVariables(emptyList(), variablesInput, processVariables, emptyList(), queryContext);

        for (UserTaskInstanceWithPotOwnerDesc userTask : dataInput) {
            Long taskId = userTask.getTaskId();
            String user = userTask.getPotentialOwners().get(0);
            userTaskService.start(taskId, user);
            Map<String, Object> inputs = userTaskService.getTaskInputContentByTaskId(taskId);
            Map<String, Object> output = new HashMap<>(inputs);
            output.put("task_out_a1", "3");
            userTaskService.saveContentFromUser(taskId, user, output);
            userTaskService.complete(taskId, user, output);
        }

        processVariables = list(notEqualsTo("var_b", "2"), isNotNull("var_b"));
        variablesOuput = list(equalsTo("task_out_a1", "3"), type("task_out_a1", OUTPUT.ordinal()));
        dataOutput = advanceVariableDataService.queryUserTasksByVariables(emptyList(), variablesOuput, processVariables, emptyList(), queryContext);

        if (queryContext.getCount() > 0) {
            assertThat(dataOutput.size(), is(queryContext.getCount()));
        } else {
            assertThat(dataOutput.size(), is(3));
        }

        for (UserTaskInstanceWithPotOwnerDesc userTask : dataOutput) {
            assertThat(userTask.getProcessVariables().get("var_b"), is("3"));
        }
    }
    
    @Test
    public void testQueryHistoryAllNull() {
        List<QueryParam> attributes = list(history());
        List<UserTaskInstanceWithPotOwnerDesc> data = advanceVariableDataService.queryUserTasksByVariables(attributes, null, null, (List<String>) null, queryContext);
        if (queryContext.getCount() > 0) {
            assertThat(data.size(), is(queryContext.getCount()));
        } else {
            assertThat(data.size(), is(20));
        }
    }
    
    @Test
    public void testQueryHistoryIsNotNullOperator() {

        List<QueryParam> attributes = list(history(), isNull(TASK_ATTR_OWNER));

        List<UserTaskInstanceWithPotOwnerDesc> data = advanceVariableDataService.queryUserTasksByVariables(attributes, emptyList(), emptyList(), emptyList(), queryContext);
        assertThat(data.size(), is(0));

    }

    @Test
    public void testQueryHistoryProcessByAttributes() {
        List<QueryParam> attributes = list(history(), equalsTo(PROCESS_ATTR_DEFINITION_ID, "test.test_A"), equalsTo(PROCESS_ATTR_CORRELATION_KEY, "1"));

        List<ProcessInstanceWithVarsDesc> data = advanceVariableDataService.queryProcessByVariables(attributes, emptyList(), queryContext);
        assertThat(data.size(), is(1));
    }

    @Test
    public void testQueryHistoryProcessByVariablesAndTask() {
        List<QueryParam> attributes = list(history());

        List<ProcessInstanceWithVarsDesc> data = advanceVariableDataService.queryProcessByVariablesAndTask(attributes, emptyList(), emptyList(), emptyList(), queryContext);
        if (queryContext.getCount() > 0) {
            assertThat(data.size(), is(queryContext.getCount()));
        } else {
            assertThat(data.size(), is(20));
        }
    }

    @Test
    public void testQueryProcessByVariablesAndTask() {
        List<ProcessInstanceWithVarsDesc> data = advanceVariableDataService.queryProcessByVariablesAndTask(emptyList(), emptyList(), emptyList(), emptyList(), queryContext);
        if (queryContext.getCount() > 0) {
            assertThat(data.size(), is(queryContext.getCount()));
        } else {
            assertThat(data.size(), is(20));
        }
    }

    @Test
    public void testParameterWithDash() {
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("var-a", "a");
        inputs.put("var-b", "b");
        long processInstanceId = processService.startProcess(deploymentUnit.getIdentifier(), "test.test_D", inputs);

        List<QueryParam> processVariables = list(notEqualsTo("var-b", "c"), isNotNull("var-b"));
        List<UserTaskInstanceWithPotOwnerDesc> data = advanceVariableDataService.queryUserTasksByVariables(emptyList(), emptyList(), processVariables, emptyList(), queryContext);
        assertThat(data.size(), is(1));
        assertEquals(data.get(0).getProcessVariables().get("var-b"), "b");
        assertEquals(data.get(0).getProcessVariables().get("var-a"), "a");
        processService.abortProcessInstance(processInstanceId);

    }

    @Test
    public void testQueryProcessByVariablesSorted() {
        List<QueryParam> variables = list(equalsTo("var_b", "1"));
        List<QueryParam> attributes = list(in(PROCESS_ATTR_DEFINITION_ID, "test.test_A", "test.test_B"));
        assertQueryProcessByVariablesSorted(attributes, variables);
    }

    @Test
    public void testQueryProcessByVariablesSortedWithHistory(){
        List<QueryParam> variables = list(equalsTo("var_b", "1"));
        List<QueryParam> attributes = list(history());
        assertQueryProcessByVariablesSorted(attributes, variables);
    }

    @Test
    public void testQueryUserTaskByVariablesSorted() {
        List<QueryParam> variables = list(equalsTo("task_in_a1", "a0"));
        List<QueryParam> attributes = list(equalsTo(PROCESS_ATTR_DEPLOYMENT_ID, "org.jbpm.test:test-module:1.0.0"));
        assertQueryUserTaskByVariablesSorted(attributes, variables);
    }

    @Test
    public void testQueryUserTaskByVariablesSortedWithHistory() {
        List<QueryParam> variables = list(equalsTo("task_in_a1", "a0"));
        List<QueryParam> attributes = list(history());
        assertQueryUserTaskByVariablesSorted(attributes, variables);
    }

    @Test
    public void testQueryProcessByVariablesAndTaskSorted() {
        List<QueryParam> variables = list(equalsTo("var_b", "1"));
        List<QueryParam> attributes = list(in(PROCESS_ATTR_DEFINITION_ID, "test.test_A", "test.test_B"));
        assertQueryProcessByVariablesAndTaskSorted(attributes, variables);
    }

    @Test
    public void testQueryProcessByVariablesAndTaskSortedWithHistory() {
        List<QueryParam> variables = list(equalsTo("var_b", "1"));
        List<QueryParam> attributes = list(history());
        assertQueryProcessByVariablesAndTaskSorted(attributes, variables);
    }

    private void assertQueryProcessByVariablesSorted(List<QueryParam> attributes, List<QueryParam> variables) {
        List<ProcessInstanceWithVarsDesc> actualDataList = advanceVariableDataService.queryProcessByVariables(attributes, variables, new QueryContext());
        List<Long> expectedProcessInstanceIdsList = actualDataList
                .stream()
                .map(ProcessInstanceWithVarsDesc::getId)
                .collect(Collectors.toList());
        List<Date> expectedStartDatesList = actualDataList
                .stream()
                .map(ProcessInstanceWithVarsDesc::getDataTimeStamp)
                .collect(Collectors.toList());
        assertThat(expectedProcessInstanceIdsList.size(), is(6));
        assertThat(expectedStartDatesList.size(), is(6));

        // Order by processInstanceId ascending
        queryContext.setOrderBy("processInstanceId");
        queryContext.setAscending(true);
        assertSortedListByColumn(true,
                                 expectedProcessInstanceIdsList,
                                 advanceVariableDataService.queryProcessByVariables(attributes, variables, queryContext)
                                         .stream()
                                         .map(ProcessInstanceWithVarsDesc::getId)
                                         .collect(Collectors.toList()));

        // Order by processInstanceId descending
        queryContext.setAscending(false);
        assertSortedListByColumn(false,
                                 expectedProcessInstanceIdsList,
                                 advanceVariableDataService.queryProcessByVariables(attributes, variables, queryContext)
                                         .stream()
                                         .map(ProcessInstanceWithVarsDesc::getId)
                                         .collect(Collectors.toList()));

        // Order by start_date ascending
        queryContext.setOrderBy("start_date");
        queryContext.setAscending(true);
        assertSortedListByColumn(true,
                                 expectedStartDatesList,
                                 advanceVariableDataService.queryProcessByVariables(attributes, variables, queryContext)
                                         .stream()
                                         .map(ProcessInstanceWithVarsDesc::getDataTimeStamp)
                                         .collect(Collectors.toList()));

        // Order by start_date descending
        queryContext.setAscending(false);
        assertSortedListByColumn(false,
                                 expectedStartDatesList,
                                 advanceVariableDataService.queryProcessByVariables(attributes, variables, queryContext)
                                         .stream()
                                         .map(ProcessInstanceWithVarsDesc::getDataTimeStamp)
                                         .collect(Collectors.toList()));
    }

    private void assertQueryUserTaskByVariablesSorted(List<QueryParam> attributes, List<QueryParam> variables) {
        List<UserTaskInstanceWithPotOwnerDesc> actualDataList = advanceVariableDataService.queryUserTasksByVariables(attributes, variables, emptyList(), emptyList(), new QueryContext());
        List<Long> expectedTaskIdsList = actualDataList
                .stream()
                .map(UserTaskInstanceWithPotOwnerDesc::getTaskId)
                .collect(Collectors.toList());
        List<Date> expectedCreatedOnList = actualDataList
                .stream()
                .map(UserTaskInstanceWithPotOwnerDesc::getCreatedOn)
                .collect(Collectors.toList());
        assertThat(expectedTaskIdsList.size(), is(4));
        assertThat(expectedCreatedOnList.size(), is(4));

        // Order by taskId ascending
        queryContext.setOrderBy("taskId");
        queryContext.setAscending(true);
        assertSortedListByColumn(true,
                                 expectedTaskIdsList,
                                 advanceVariableDataService.queryUserTasksByVariables(attributes, variables, emptyList(), emptyList(), queryContext)
                                         .stream()
                                         .map(UserTaskInstanceWithPotOwnerDesc::getTaskId)
                                         .collect(Collectors.toList()));

        // Order by taskId descending
        queryContext.setAscending(false);
        assertSortedListByColumn(false,
                                 expectedTaskIdsList,
                                 advanceVariableDataService.queryUserTasksByVariables(attributes, variables, emptyList(), emptyList(), queryContext)
                                         .stream()
                                         .map(UserTaskInstanceWithPotOwnerDesc::getTaskId)
                                         .collect(Collectors.toList()));

        // Order by createdOn ascending
        queryContext.setOrderBy("createdOn");
        queryContext.setAscending(true);
        assertSortedListByColumn(true,
                                 expectedCreatedOnList,
                                 advanceVariableDataService.queryUserTasksByVariables(attributes, variables, emptyList(), emptyList(), queryContext)
                                         .stream()
                                         .map(UserTaskInstanceWithPotOwnerDesc::getCreatedOn)
                                         .collect(Collectors.toList()));

        // Order by createdOn descending
        queryContext.setAscending(false);
        assertSortedListByColumn(false,
                                 expectedCreatedOnList,
                                 advanceVariableDataService.queryUserTasksByVariables(attributes, variables, emptyList(), emptyList(), queryContext)
                                         .stream()
                                         .map(UserTaskInstanceWithPotOwnerDesc::getCreatedOn)
                                         .collect(Collectors.toList()));
    }

    private void assertQueryProcessByVariablesAndTaskSorted(List<QueryParam> attributes, List<QueryParam> variables) {
        List<ProcessInstanceWithVarsDesc> actualDataList = advanceVariableDataService.queryProcessByVariablesAndTask(attributes, variables, emptyList(), emptyList(), new QueryContext());

        List<Long> expectedProcessInstanceIdsList = actualDataList
                .stream()
                .map(ProcessInstanceWithVarsDesc::getId)
                .collect(Collectors.toList());
        List<String> expectedCorrelationKeysList = actualDataList
                .stream()
                .map(ProcessInstanceWithVarsDesc::getCorrelationKey)
                .collect(Collectors.toList());
        assertThat(expectedProcessInstanceIdsList.size(), is(6));
        assertThat(expectedCorrelationKeysList.size(), is(6));

        // Order by processInstanceId ascending
        queryContext.setOrderBy("processInstanceId");
        queryContext.setAscending(true);
        assertSortedListByColumn(true,
                                 expectedProcessInstanceIdsList,
                                 advanceVariableDataService.queryProcessByVariablesAndTask(attributes, variables, emptyList(), emptyList(), queryContext)
                                         .stream()
                                         .map(ProcessInstanceWithVarsDesc::getId)
                                         .collect(Collectors.toList()));

        // Order by processInstanceId descending
        queryContext.setAscending(false);
        assertSortedListByColumn(false,
                                 expectedProcessInstanceIdsList,
                                 advanceVariableDataService.queryProcessByVariablesAndTask(attributes, variables, emptyList(), emptyList(), queryContext)
                                         .stream()
                                         .map(ProcessInstanceWithVarsDesc::getId)
                                         .collect(Collectors.toList()));

        // Order by correlationKey ascending
        queryContext.setOrderBy("correlationKey");
        queryContext.setAscending(true);
        assertSortedListByColumn(true,
                                 expectedCorrelationKeysList,
                                 advanceVariableDataService.queryProcessByVariablesAndTask(attributes, variables, emptyList(), emptyList(), queryContext)
                                         .stream()
                                         .map(ProcessInstanceWithVarsDesc::getCorrelationKey)
                                         .collect(Collectors.toList()));

        // Order by correlationKey descending
        queryContext.setAscending(false);
        assertSortedListByColumn(false,
                                 expectedCorrelationKeysList,
                                 advanceVariableDataService.queryProcessByVariablesAndTask(attributes, variables, emptyList(), emptyList(), queryContext)
                                         .stream()
                                         .map(ProcessInstanceWithVarsDesc::getCorrelationKey)
                                         .collect(Collectors.toList()));
    }

    private <T extends Comparable<? super T>> void assertSortedListByColumn(boolean ascending, List<T> expectedSortedList, List<T> actualSortedList){
        int count = queryContext.getCount() > 0 ? queryContext.getCount() : expectedSortedList.size();

        expectedSortedList.sort(ascending ? null : Collections.reverseOrder());
        assertEquals(expectedSortedList.subList(queryContext.getOffset(), count), actualSortedList);
    }

}
