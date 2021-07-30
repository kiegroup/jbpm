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

package org.jbpm.test.functional.subprocess;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.tools.ant.Task;
import org.assertj.core.api.Assertions;
import org.jbpm.test.JbpmTestCase;
import org.jbpm.test.listener.IterableProcessEventListener;
import org.junit.Test;
import org.kie.api.command.Command;
import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.event.process.ProcessNodeTriggeredEvent;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.audit.ProcessInstanceLog;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.task.TaskService;

import static org.jbpm.test.tools.IterableListenerAssert.assertChangedVariable;
import static org.jbpm.test.tools.IterableListenerAssert.assertLeft;
import static org.jbpm.test.tools.IterableListenerAssert.assertNextNode;
import static org.jbpm.test.tools.IterableListenerAssert.assertProcessCompleted;
import static org.jbpm.test.tools.IterableListenerAssert.assertProcessStarted;
import static org.jbpm.test.tools.IterableListenerAssert.assertTriggered;

public class ReusableSubProcessTest extends JbpmTestCase {

    private static final String CALL_ACTIVITY_PARENT =
            "org/jbpm/test/functional/subprocess/ReusableSubProcess-parent.bpmn";
    private static final String CALL_ACTIVITY_PARENT_ID =
            "org.jbpm.test.functional.subprocess.ReusableSubProcess-parent";

    private static final String CALL_ACTIVITY_CHILD =
            "org/jbpm/test/functional/subprocess/ReusableSubProcess-child.bpmn";
    private static final String CALL_ACTIVITY_CHILD_ID =
            "org.jbpm.test.functional.subprocess.ReusableSubProcess-child";

    
    private static final String INVOKE_ACTIVITY_PARENT =
            "org/jbpm/test/functional/subprocess/ReusableSubprocess-parent-trigger.bpmn2";
    private static final String INVOKE_ACTIVITY_PARENT_ID =
            "org.jbpm.test.functional.subprocess.ReusableSubprocess-parent";

    private static final String INVOKE_ACTIVITY_CHILD =
            "org/jbpm/test/functional/subprocess/ReusableSubprocess-child-trigger.bpmn2";
    private static final String INVOKE_ACTIVITY_CHILD_ID =
            "org.jbpm.test.functional.subprocess.ReusableSubprocess-child";
    
    
    private static final String ERROR_HANDLING_MAIN_PROCESS =
            "org/jbpm/test/functional/subprocess/ErrorHandlingMainProcess.bpmn";

    private static final String ERROR_HALDING_CHILD_PROCESS =
            "org/jbpm/test/functional/subprocess/ErrorHandlingSubprocess.bpmn";

    private static final String ERROR_HANDLING_MAIN_PROCESS_ID =
            "ExceptionHandling.MainProcess";

    public ReusableSubProcessTest() {
        super(true);
    }

    @Test(timeout = 30000)
    public void testCallActivity() {
        KieSession ksession = createKSession(CALL_ACTIVITY_CHILD, CALL_ACTIVITY_PARENT);
        IterableProcessEventListener eventListener = new IterableProcessEventListener();

        ksession.addEventListener(eventListener);
        ksession.execute((Command<?>) getCommands().newStartProcess(CALL_ACTIVITY_PARENT_ID));
        assertProcessStarted(eventListener, CALL_ACTIVITY_PARENT_ID);

        assertNextNode(eventListener, "start");
        assertTriggered(eventListener, "script");
        assertChangedVariable(eventListener, "var", null, 1);
        assertLeft(eventListener, "script");

        assertTriggered(eventListener, "reusable");

        assertChangedVariable(eventListener, "inSubVar", null, 1);
        assertProcessStarted(eventListener, CALL_ACTIVITY_CHILD_ID);

        assertNextNode(eventListener, "rs-start");
        assertTriggered(eventListener, "rs-script");
        assertChangedVariable(eventListener, "outSubVar", null, "one");
        assertLeft(eventListener, "rs-script");
        assertNextNode(eventListener, "rs-end");
        assertProcessCompleted(eventListener, CALL_ACTIVITY_CHILD_ID);
        assertChangedVariable(eventListener, "var", 1, "one");
        assertLeft(eventListener, "reusable");
        assertNextNode(eventListener, "end");

        assertProcessCompleted(eventListener, CALL_ACTIVITY_PARENT_ID);
    }

    @Test(timeout = 30000)
    public void testInvokeReusableOnce() {
        MutableInt counter = new MutableInt();
        addProcessEventListener(new DefaultProcessEventListener() {
            @Override
            public void afterProcessCompleted(ProcessCompletedEvent event) {
                counter.increment();
            }
        });
        KieSession ksession = createKSession(INVOKE_ACTIVITY_CHILD, INVOKE_ACTIVITY_PARENT);
        long pid = ksession.startProcess(INVOKE_ACTIVITY_PARENT_ID, Collections.singletonMap("varProcesoPadre", true)).getId();

        assertProcessInstanceActive(pid);
        List<? extends ProcessInstanceLog> log = this.getLogService().findActiveProcessInstances(INVOKE_ACTIVITY_CHILD_ID);
        Assertions.assertThat(log).hasSize(1);
        RuntimeEngine runtimeEngine = getRuntimeEngine();
        TaskService taskService = runtimeEngine.getTaskService();

        long cpid = log.get(0).getProcessInstanceId();
        List<Long> cpids = taskService.getTasksByProcessInstanceId(cpid);
        long taskId = cpids.get(0);
        taskService.claim(taskId, "Administrator");
        taskService.start(taskId, "Administrator");
        taskService.complete(taskId, "Administrator", Collections.emptyMap());
        assertProcessInstanceCompleted(cpid);

        List<Long> pids = taskService.getTasksByProcessInstanceId(pid);
        taskId = pids.get(0);
        taskService.claim(taskId, "Administrator");
        taskService.start(taskId, "Administrator");
        taskService.complete(taskId, "Administrator", Collections.emptyMap());
        assertProcessInstanceCompleted(pid);
        System.out.println(counter);
    }

    

    class RestWorkItemHandler implements WorkItemHandler {

        @Override
        public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
            throw new RuntimeException("failure for rest handler");
        }

        @Override
        public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {

        }

    }

    @Test(timeout = 30000)
    public void testErrorHandlingActivity() throws Exception {
        KieSession ksession = createKSession(ERROR_HANDLING_MAIN_PROCESS, ERROR_HALDING_CHILD_PROCESS);
        final CountDownLatch latch = new CountDownLatch(1);
        ksession.addEventListener(new DefaultProcessEventListener() {

            @Override
            public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
                if ("Task".equals(event.getNodeInstance().getNodeName())) {
                    latch.countDown();
                }
            }

        });
        ksession.getWorkItemManager().registerWorkItemHandler("Rest", new RestWorkItemHandler());
        ksession.startProcess(ERROR_HANDLING_MAIN_PROCESS_ID);
        latch.await();
        Assertions.assertThat(latch.getCount()).isEqualTo(0);
    }
}
