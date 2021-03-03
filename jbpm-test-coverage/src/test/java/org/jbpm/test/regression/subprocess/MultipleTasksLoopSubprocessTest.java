/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.test.regression.subprocess;

import java.util.List;

import org.jbpm.test.JbpmTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.Task;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jbpm.test.JbpmJUnitBaseTestCase.Strategy.PROCESS_INSTANCE;
import static org.junit.Assert.assertEquals;

public class MultipleTasksLoopSubprocessTest extends JbpmTestCase {

    private static final String DYNAMIC_PARENT_PROCESS = "org/jbpm/test/regression/subprocess/MultipleTasksLoopParentProcess.bpmn";
    private static final String DYNAMIC_CHILD_SUBPROCESS = "org/jbpm/test/regression/subprocess/MultipleTasksLoopSubProcess.bpmn";
    private static final String DYNAMIC_PARENT_PROCESS_ID = "MultipleTasksLoopParentProcess";

    private static final String JOHN = "john";

    private KieSession ksession;
    private TaskService taskService;
    private RuntimeManager runtimeManager;
    private RuntimeEngine engine;

    @Before
    public void init() {
        System.setProperty("jbpm.enable.multi.con", "true");
        runtimeManager = createRuntimeManager(PROCESS_INSTANCE, "MultipleTasksLoopSubprocessTest",
                                              DYNAMIC_PARENT_PROCESS,
                                              DYNAMIC_CHILD_SUBPROCESS);
        engine = getRuntimeEngine();
        ksession = engine.getKieSession();
        taskService = engine.getTaskService();
    }

    @After
    public void cleanup() {
        runtimeManager.disposeRuntimeEngine(engine);
        System.clearProperty("jbpm.enable.multi.con");
    }

    @Test
    public void testDuplicateTasksAfterSubProcess() {
        assertThat(ksession).isNotNull();
        long pid = ksession.startProcess(DYNAMIC_PARENT_PROCESS_ID).getId();
        assertProcessInstanceActive(pid);

        // We need to call getProcessInstance with readOnly=true to force
        assertThat(ksession.getProcessInstance(pid, true)).isNotNull();

        assertEquals(1, getLogService().findSubProcessInstances(pid).size());
        long subPid = getLogService().findSubProcessInstances(pid).get(0).getProcessInstanceId();

        List<Long> tasks = taskService.getTasksByProcessInstanceId(subPid);
        Task task = taskService.getTaskById(tasks.get(0));
        taskService.start(task.getId(), JOHN);
        taskService.complete(task.getId(), JOHN, emptyMap());

        assertProcessInstanceCompleted(subPid); // sub-process is completed
        assertProcessInstanceActive(pid); 

        tasks = taskService.getTasksByProcessInstanceId(pid);
        assertEquals(1, tasks.size()); // only 1 task should be available at this point

        ksession.abortProcessInstance(pid);
        assertProcessInstanceAborted(pid);
    }
}
