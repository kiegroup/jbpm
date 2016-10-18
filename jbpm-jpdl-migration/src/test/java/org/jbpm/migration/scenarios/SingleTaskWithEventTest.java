/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.migration.scenarios;

import static org.jbpm.migration.tools.listeners.TrackingListenerAssert.assertLeft;
import static org.jbpm.migration.tools.listeners.TrackingListenerAssert.assertProcessStarted;
import static org.jbpm.migration.tools.listeners.TrackingListenerAssert.assertTriggered;

import java.util.Set;

import org.assertj.core.api.Assertions;
import org.jbpm.migration.JbpmMigrationRuntimeTest;
import org.jbpm.migration.tools.bpmn2.ExecutionInfo;
import org.jbpm.migration.tools.bpmn2.JavaNodeHandler;
import org.jbpm.migration.tools.jpdl.JpdlAssert;
import org.jbpm.migration.tools.jpdl.JpdlHelper;
import org.jbpm.migration.tools.jpdl.handlers.DefaultActionHandler;
import org.jbpm.migration.tools.jpdl.listeners.TrackingActionListener;
import org.jbpm.migration.tools.listeners.TrackingProcessEventListener;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.taskmgmt.exe.TaskInstance;
import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.runtime.process.WorkItem;

/**
 * Testing single task with event on node enter and leave. translated BPMN2
 * contains Java task->userTask->Java task.
 */
public class SingleTaskWithEventTest extends JbpmMigrationRuntimeTest {
    public static final String definition =
            "org/jbpm/migration/scenarios/singleTaskWithEvent/processdefinition.xml";
    public static final String processId = "singleTask_Process";

    private static final String BPMN_HT_NODE_NAME = "Expanded to provide: human-task";

    private static final String HANDLER_NAME =
            "org.jbpm.migration.tools.jpdl.handlers.DefaultActionHandler";
    private static final String HANDLER_EXECUTE_METHOD_NAME = "execute";

    @BeforeClass
    public static void getTestReady() {
        prepareProcess(definition);
    }

    @Test
    public void testJpdl() throws Exception {
        ProcessInstance pi = processDef.createProcessInstance();

        TrackingActionListener listener = new TrackingActionListener();
        // pi.getContextInstance().createVariable("listener", listener);
        DefaultActionHandler.setTrackingListener(listener);
        DefaultActionHandler.setSignalization(false);

        pi.signal();
        JpdlAssert.assertProcessStarted(pi);

        TaskInstance ti = JpdlHelper.getTaskInstance("Test task", pi);
        Assertions.assertThat(ti.getActorId()).isEqualTo("EXPERT");
        ti.end();
        JpdlAssert.assertTaskEnded(ti);

        Assertions.assertThat(listener.wasCalledOnNode("human-task")).isTrue();
        Assertions.assertThat(listener.wasEventAccepted("node-enter")).isTrue();
        Assertions.assertThat(listener.wasEventAccepted("node-leave")).isTrue();

        JpdlAssert.assertProcessCompleted(pi);
    }

    @Test
    public void testBpmn() {
        ksession = kbase.newKieSession();

        TrackingProcessEventListener listener = new TrackingProcessEventListener();
        ksession.addEventListener(listener);

        final JbpmJUnitBaseTestCase.TestWorkItemHandler humanTaskHandler = new JbpmJUnitBaseTestCase.TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", humanTaskHandler);

        final JavaNodeHandler javaNodeHandler = new JavaNodeHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("JavaNode", javaNodeHandler);

        ksession.startProcess(processId);
        assertProcessStarted(listener, processId);

        assertTriggered(listener, BPMN_HT_NODE_NAME);

        verifyWorkItem(javaNodeHandler.getExecutionInfo(), 1);

        final WorkItem wi = humanTaskHandler.getWorkItem();
        ksession.getWorkItemManager().completeWorkItem(wi.getId(), null);

        assertLeft(listener, BPMN_HT_NODE_NAME);
        verifyWorkItem(javaNodeHandler.getExecutionInfo(), 2);
    }

    private void verifyWorkItem(final Set<ExecutionInfo> handlerInfo, final int expectedExecutions) {
        Assertions.assertThat((long) handlerInfo.size()).as("Unexpected number of ExecutionInfo").isEqualTo((long) 1);

        for (ExecutionInfo executionInfo : handlerInfo) {
            Assertions.assertThat(executionInfo.getClassName()).as("Unexpected class name").isEqualTo(HANDLER_NAME);
            Assertions.assertThat(executionInfo.getMethodName()).as("Unexpected method name").isEqualTo(HANDLER_EXECUTE_METHOD_NAME);
            Assertions.assertThat((long) executionInfo.ExecutionCount()).as("Unexpected number of handler executions").isEqualTo((long) expectedExecutions);
        }
    }
}
