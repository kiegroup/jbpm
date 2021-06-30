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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.kie.test.util.AbstractKieServicesBaseTest;
import org.jbpm.services.api.ProcessInstanceNotFoundException;
import org.jbpm.services.api.model.DeploymentUnit;
import org.jbpm.services.api.model.ProcessInstanceWithVarsDesc;
import org.jbpm.services.api.model.UserTaskInstanceDesc;
import org.jbpm.services.api.model.UserTaskInstanceWithPotOwnerDesc;
import org.jbpm.services.api.query.model.QueryParam;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.internal.query.QueryContext;
import org.kie.internal.task.api.TaskVariable;
import org.kie.scanner.KieMavenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.jbpm.services.api.AdvanceRuntimeDataService.TASK_ATTR_NAME;
import static org.jbpm.services.api.query.model.QueryParam.equalsTo;
import static org.jbpm.services.api.query.model.QueryParam.list;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.kie.scanner.KieMavenRepository.getKieMavenRepository;

public class AdvanceRuntimeDataServiceImplWithVariableChangesTest extends AbstractKieServicesBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(AdvanceRuntimeDataServiceImplWithVariableChangesTest.class);

    private List<DeploymentUnit> units = new ArrayList<>();

    private List<Long> processIds;
    private KModuleDeploymentUnit deploymentUnit = null;


    @Before
    public void prepare() {

        configureServices();
        logger.debug("Preparing kjar");
        KieServices ks = KieServices.Factory.get();
        ReleaseId releaseId = ks.newReleaseId(GROUP_ID, ARTIFACT_ID, VERSION);
        List<String> processes = new ArrayList<>();
        processes.add("repo/processes/general/SingleHumanTaskWithVarsA.bpmn2");
        processes.add("repo/processes/general/SingleHumanTaskWithVarsB.bpmn2");
        processes.add("repo/processes/general/SingleHumanTaskWithVarsC.bpmn2");

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

        Map<String, Object> inputsA1 = new HashMap<>();
        inputsA1.put("var_a", "myvalue");
        inputsA1.put("var_b", "othervalue");
        processIds.add(processService.startProcess(deploymentUnit.getIdentifier(), "test.test_A", inputsA1));

        Map<String, Object> inputsB1 = new HashMap<>();
        inputsB1.put("var_a", "somethingelse");
        inputsB1.put("var_b", "b_var");
        processIds.add(processService.startProcess(deploymentUnit.getIdentifier(), "test.test_B", inputsB1));

        Map<String, Object> inputsC1 = new HashMap<>();
        inputsC1.put("var_c", "somethingelse");
        inputsC1.put("var_b", "b_var");
        processIds.add(processService.startProcess(deploymentUnit.getIdentifier(), "test.test_C", inputsC1));

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
    public void testQueryProcessTaskByVariablesWithOwners() {

        List<UserTaskInstanceWithPotOwnerDesc> userTasks = advanceVariableDataService.queryUserTasksByVariables(emptyList(), emptyList(), emptyList(), (List<String>) null, new QueryContext(0, 0));

        for (UserTaskInstanceWithPotOwnerDesc userTask : userTasks) {
            Long taskId = userTask.getTaskId();
            String user = userTask.getPotentialOwners().get(0);
            userTaskService.start(taskId, user);
            Map<String, Object> inputs = userTaskService.getTaskInputContentByTaskId(taskId);
            Map<String, Object> output = new HashMap<>(inputs);
            output.put("task_out_a1", "3");
            userTaskService.complete(taskId, user, output);
        }

        List<QueryParam> attributes = list(equalsTo(TASK_ATTR_NAME, "CustomTask"));
        List<QueryParam> processVariables = list(equalsTo("var_b", "3"));
        List<QueryParam> taskVariables = list(equalsTo("task_in_a1", "somethingelse"), QueryParam.type("task_in_a2", TaskVariable.VariableType.INPUT.ordinal()));
        QueryParam potOwners = new QueryParam(null, "ALL", singletonList("kieserver"));

        List<ProcessInstanceWithVarsDesc> data = advanceVariableDataService.queryProcessByVariablesAndTask(attributes, processVariables, taskVariables, potOwners, new QueryContext(0, 0));
        assertThat(data.size(), is(1));
        assertThat(data.get(0).getVariables().get("var_b"), is("3"));

        List<Long> taksIds = data.stream().map(ProcessInstanceWithVarsDesc::getId).map(id -> runtimeDataService.getTasksByProcessInstanceId(id)).flatMap(List::stream).collect(toList());
        for (Long taskId : taksIds) {
            UserTaskInstanceDesc userTask = runtimeDataService.getTaskById(taskId);
            assertThat(userTask.getName(), is("CustomTask"));
        }
    }

}
