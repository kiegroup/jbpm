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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.kie.test.util.AbstractKieServicesBaseTest;
import org.jbpm.services.api.ProcessInstanceNotFoundException;
import org.jbpm.services.api.model.DeploymentUnit;
import org.jbpm.services.api.model.ProcessInstanceWithVarsDesc;
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
import static org.jbpm.services.api.AdvanceRuntimeDataService.PROCESS_ATTR_CORRELATION_KEY;
import static org.jbpm.services.api.AdvanceRuntimeDataService.PROCESS_ATTR_DEFINITION_ID;
import static org.jbpm.services.api.AdvanceRuntimeDataService.PROCESS_ATTR_DEPLOYMENT_ID;
import static org.jbpm.services.api.AdvanceRuntimeDataService.TASK_ATTR_NAME;
import static org.jbpm.services.api.AdvanceRuntimeDataService.TASK_ATTR_OWNER;
import static org.jbpm.services.api.query.model.QueryParam.equalsTo;
import static org.jbpm.services.api.query.model.QueryParam.in;
import static org.jbpm.services.api.query.model.QueryParam.isNotNull;
import static org.jbpm.services.api.query.model.QueryParam.isNull;
import static org.jbpm.services.api.query.model.QueryParam.likeTo;
import static org.jbpm.services.api.query.model.QueryParam.list;
import static org.jbpm.services.api.query.model.QueryParam.notEqualsTo;
import static org.jbpm.services.api.query.model.QueryParam.notIn;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
            Assert.assertThat(data.size(), is(queryContext.getCount()));
        }
        for (ProcessInstanceWithVarsDesc p : data) {
            Assert.assertEquals("a1", p.getVariables().get("var_a"));
            Assert.assertEquals("1", p.getVariables().get("var_b"));
            Assert.assertEquals("test.test_A", p.getProcessId());
        }
    }

    @Test
    public void testQueryInOperator() {

        List<QueryParam> variables = list(in("var_a", "a1", "a2"));
        List<QueryParam> attributes = list(equalsTo(PROCESS_ATTR_DEFINITION_ID, "test.test_A"));

        List<ProcessInstanceWithVarsDesc> data = advanceVariableDataService.queryProcessByVariables(attributes, variables, queryContext);
        if (queryContext.getCount() > 0) {
            Assert.assertThat(data.size(), is(queryContext.getCount()));
        }
        List<String> values = Arrays.asList("a1", "a2");
        for (ProcessInstanceWithVarsDesc p : data) {
            Assert.assertTrue(values.contains(p.getVariables().get("var_a")));
            Assert.assertEquals("test.test_A", p.getProcessId());
        }
    }

    @Test
    public void testQueryLikeToOperator() {

        List<QueryParam> variables = list(likeTo("var_a", false, "a%"));
        List<QueryParam> attributes = list(likeTo(PROCESS_ATTR_DEFINITION_ID, false, "%test_A%"));

        List<ProcessInstanceWithVarsDesc> data = advanceVariableDataService.queryProcessByVariables(attributes, variables, queryContext);
        if (queryContext.getCount() > 0) {
            Assert.assertThat(data.size(), is(queryContext.getCount()));
        }

        for (ProcessInstanceWithVarsDesc p : data) {
            Assert.assertTrue(((String) p.getVariables().get("var_a")).startsWith("a"));
            Assert.assertEquals("test.test_A", p.getProcessId());
        }
    }

    @Test
    public void testQueryIsNotNullOperator() {

        List<QueryParam> attributes = list(isNotNull(TASK_ATTR_OWNER));

        List<UserTaskInstanceWithPotOwnerDesc> data = advanceVariableDataService.queryUserTasksByVariables(attributes, emptyList(), emptyList(), emptyList(), queryContext);
        Assert.assertThat(data.size(), is(0));

    }

    @Test
    public void testQueryIsNullOperator() {

        List<QueryParam> attributes = list(isNull(TASK_ATTR_OWNER));

        List<UserTaskInstanceWithPotOwnerDesc> data = advanceVariableDataService.queryUserTasksByVariables(attributes, emptyList(), emptyList(), emptyList(), queryContext);
        if (queryContext.getCount() > 0) {
            Assert.assertThat(data.size(), is(queryContext.getCount()));
        }

        for (UserTaskInstanceWithPotOwnerDesc p : data) {
            Assert.assertNull(p.getActualOwner());
        }
    }

    @Test
    public void testQueryNotInOperator() {

        List<QueryParam> variables = list(notIn("var_a", "a1", "a2"));
        List<QueryParam> attributes = list(equalsTo(PROCESS_ATTR_DEFINITION_ID, "test.test_A"));

        List<ProcessInstanceWithVarsDesc> data = advanceVariableDataService.queryProcessByVariables(attributes, variables, queryContext);
        if (queryContext.getCount() > 0) {
            Assert.assertThat(data.size(), is(queryContext.getCount()));
        }
        List<String> values = Arrays.asList("a1", "a2");
        for (ProcessInstanceWithVarsDesc p : data) {
            Assert.assertTrue(!values.contains(p.getVariables().get("var_a")));
            Assert.assertEquals("test.test_A", p.getProcessId());
        }
    }

    @Test
    public void testQueryNotEqualsOperator() {
        List<QueryParam> attributes = list(notEqualsTo(PROCESS_ATTR_DEFINITION_ID, "test.test_A"));

        List<ProcessInstanceWithVarsDesc> data = advanceVariableDataService.queryProcessByVariables(attributes, emptyList(), queryContext);
        if (queryContext.getCount() > 0) {
            Assert.assertThat(data.size(), is(queryContext.getCount()));
        }

        for (ProcessInstanceWithVarsDesc p : data) {
            Assert.assertNotEquals("test.test_A", p.getProcessId());
        }
    }

    @Test
    public void testQueryProcessByAttributes() {
        List<QueryParam> attributes = list(equalsTo(PROCESS_ATTR_DEFINITION_ID, "test.test_A"), equalsTo(PROCESS_ATTR_CORRELATION_KEY, "1"));

        List<ProcessInstanceWithVarsDesc> data = advanceVariableDataService.queryProcessByVariables(attributes, emptyList(), queryContext);
        Assert.assertThat(data.size(), is(1));
    }

    @Test
    public void testQueryTaskByVariables() {
        List<QueryParam> variables = list(equalsTo("task_in_a1", "a0"));
        List<QueryParam> attributes = list(equalsTo(PROCESS_ATTR_DEPLOYMENT_ID, "org.jbpm.test:test-module:1.0.0"));

        List<String> potOwners = emptyList();
        List<UserTaskInstanceWithPotOwnerDesc> data = advanceVariableDataService.queryUserTasksByVariables(attributes, variables, emptyList(), potOwners, queryContext);
        if (queryContext.getCount() > 0) {
            Assert.assertThat(data.size(), is(queryContext.getCount()));
        }
        for (UserTaskInstanceWithPotOwnerDesc p : data) {
            Assert.assertEquals("a0", p.getInputdata().get("task_in_a1"));
            Assert.assertEquals("org.jbpm.test:test-module:1.0.0", p.getDeploymentId());
        }
    }

    @Test
    public void testQueryTaskNotEqualsByVariables() {
        List<QueryParam> attributes = list(notEqualsTo(PROCESS_ATTR_DEFINITION_ID, "test.test_A"));

        List<String> potOwners = emptyList();
        List<UserTaskInstanceWithPotOwnerDesc> data = advanceVariableDataService.queryUserTasksByVariables(attributes, emptyList(), emptyList(), potOwners, queryContext);
        if (queryContext.getCount() > 0) {
            Assert.assertThat(data.size(), is(queryContext.getCount()));
        }
        for (UserTaskInstanceWithPotOwnerDesc p : data) {
            Assert.assertNotEquals("test.test_A", p.getProcessId());
        }
    }

    @Test
    public void testQueryTaskByVariablesWithOwners() {

        List<String> potOwners = Collections.singletonList("katy");
        List<UserTaskInstanceWithPotOwnerDesc> data = advanceVariableDataService.queryUserTasksByVariables(emptyList(), emptyList(), emptyList(), potOwners, queryContext);
        if (queryContext.getCount() > 0) {
            Assert.assertThat(data.size(), is(queryContext.getCount()));
        }

        for (UserTaskInstanceWithPotOwnerDesc p : data) {
            Assert.assertTrue(p.getPotentialOwners().contains("katy"));
        }
    }

    @Test
    public void testQueryTaskByVariablesWithAllOwners() {
        List<String> potOwners = Arrays.asList("katy", "nobody");
        List<UserTaskInstanceWithPotOwnerDesc> data = advanceVariableDataService.queryUserTasksByVariables(emptyList(), emptyList(), emptyList(), potOwners, queryContext);
        Assert.assertEquals(0, data.size());

    }

    @Test
    public void testQueryTaskByVariablesWithByProcessVar() {
        List<QueryParam> processVariables = list(equalsTo("var_a", "a1"));

        List<String> potOwners = Collections.emptyList();
        List<UserTaskInstanceWithPotOwnerDesc> data = advanceVariableDataService.queryUserTasksByVariables(emptyList(), emptyList(), processVariables, potOwners, queryContext);
        if (queryContext.getCount() > 0) {
            Assert.assertThat(data.size(), is(queryContext.getCount()));
        } else {
            Assert.assertThat(data.size(), is(3));
        }

    }

    @Test
    public void testQueryTaskByAttributes() {
        List<QueryParam> attributes = list(equalsTo(PROCESS_ATTR_DEFINITION_ID, "test.test_A"), equalsTo(PROCESS_ATTR_CORRELATION_KEY, "1"), equalsTo(TASK_ATTR_NAME, "Task"));

        List<UserTaskInstanceWithPotOwnerDesc> data = advanceVariableDataService.queryUserTasksByVariables(attributes, emptyList(), emptyList(), emptyList(), queryContext);
        Assert.assertThat(data.size(), is(1));
    }

    @Test
    public void testQueryTaskByAttributesOwner() {
        List<QueryParam> attributes = list(equalsTo(TASK_ATTR_OWNER, "Error"));
        List<UserTaskInstanceWithPotOwnerDesc> data = advanceVariableDataService.queryUserTasksByVariables(attributes, emptyList(), emptyList(), emptyList(), queryContext);
        Assert.assertThat(data.size(), is(0));
    }

}
