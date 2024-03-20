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

package org.jbpm.test.functional.task;

import org.apache.commons.lang3.mutable.MutableInt;
import org.jbpm.services.task.commands.CompleteTaskCommand;
import org.jbpm.services.task.events.DefaultTaskEventListener;
import org.jbpm.test.JbpmTestCase;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskEvent;
import org.kie.api.task.TaskService;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test listeners tied to adding assignments - RHPAM-4442
 */
public class TaskCompletedListenersTest extends JbpmTestCase {

    private KieSession ksession;
    private TaskService ts;


    Map<String, Object> formerVars;
    Map<String, Object> actualVars;


    MutableInt triggeredBeforeTaskCompletedListenerCounter;


    private static final String PROCESS = "org/jbpm/test/functional/task/HumanTask-simple-with-outputvars.bpmn2";
    private static final String PROCESS_ID = "org.jbpm.test.functional.task.HumanTask_simple_with_outputvars";


    private void init() {
        createRuntimeManager(PROCESS);
        RuntimeEngine runtimeEngine = getRuntimeEngine();
        ksession = runtimeEngine.getKieSession();
        ts = runtimeEngine.getTaskService();
    }
//
//    @After
//    public void clenaup() {
//        if (ksession != null) {
//            ksession.dispose();
//        }
//        disposeRuntimeManager();
//    }

    @Test
    public void testBeforeCompletedListenersRegression() {
        DefaultTaskEventListener listener = new DefaultTaskEventListener() {
            @Override
            public void beforeTaskCompletedEvent(TaskEvent event) {

                putAllNullSafe(actualVars, event.getTask().getTaskData().getTaskOutputVariables());
                triggeredBeforeTaskCompletedListenerCounter.increment();

                logger.debug("taskOutputVariables: " + event.getTask().getTaskData().getTaskOutputVariables());
            }
        };

        triggeredBeforeTaskCompletedListenerCounter = new MutableInt(0);

        actualVars = new HashMap<>();

        addTaskEventListener(listener);

        init();

        ProcessInstance pi = ksession.startProcess(PROCESS_ID);
        long pid = pi.getId();

        assertProcessInstanceActive(pi.getId(), ksession);
        assertNodeTriggered(pi.getId(), "Start", "Task");

        Map<String, Object> outputParams = new HashMap<>();
        outputParams.put("Output", "RHPAM-4446");
        for (long taskId : ts.getTasksByProcessInstanceId(pid)) {
            ts.start(taskId, "john");
            ts.complete(taskId, "john", outputParams);
        }

        assertThat(triggeredBeforeTaskCompletedListenerCounter.getValue()).isEqualTo(1);
        assertThat(actualVars).hasSize(1);
        assertThat(actualVars).containsKey("Output");
    }

    @Test
    public void testBeforeCompletedListener() {
        triggeredBeforeTaskCompletedListenerCounter = new MutableInt(0);

        formerVars = new HashMap<>();
        actualVars = new HashMap<>();

        DefaultTaskEventListener listener = new DefaultTaskEventListener() {
            @Override
            public void beforeTaskCompletedEvent(TaskEvent event) {

                putAllNullSafe(actualVars, event.getTask().getTaskData().getTaskOutputVariables());
                Map<String, Object> contextData = event.getTaskContext().getContextData();
                Map<String, Object> taskOutputVarsCtx = (Map<String,Object>) contextData.get(CompleteTaskCommand.TASK_OUT_VARS_CONTEXT_KEY);
                putAllNullSafe(formerVars, taskOutputVarsCtx);

                triggeredBeforeTaskCompletedListenerCounter.increment();

                logger.debug("taskOutputVariables: " + event.getTask().getTaskData().getTaskOutputVariables());
                logger.debug("before completed TaskOutputVariables: " + taskOutputVarsCtx);
            }
        };

        addTaskEventListener(listener);

        init();

        ProcessInstance pi = ksession.startProcess(PROCESS_ID);
        long pid = pi.getId();

        assertProcessInstanceActive(pi.getId(), ksession);
        assertNodeTriggered(pi.getId(), "Start", "Task");

        Map<String, Object> outputParams = new HashMap<>();
        outputParams.put("Output", "RHPAM-4446");
        for (long taskId : ts.getTasksByProcessInstanceId(pid)) {
            ts.start(taskId, "john");
            ts.complete(taskId, "john", outputParams);
        }

        assertThat(triggeredBeforeTaskCompletedListenerCounter.getValue()).isEqualTo(1);
        assertThat(formerVars).hasSize(0);
        assertThat(formerVars).doesNotContainKey("Output");

        assertThat(actualVars).hasSize(1);
        assertThat(actualVars).containsKey("Output");

    }

    private <K, V> void putAllNullSafe(Map<K, V> target, Map<K, V> source) {
        if ((source == null || target == null)) {
            return;
        }
        target.putAll(source);
    }
}
