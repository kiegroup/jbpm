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

import static org.jbpm.migration.tools.listeners.TrackingListenerAssert.assertProcessCompleted;
import static org.jbpm.migration.tools.listeners.TrackingListenerAssert.assertProcessStarted;
import static org.jbpm.migration.tools.listeners.TrackingListenerAssert.assertTriggeredAndLeft;

import java.util.Set;

import org.assertj.core.api.Assertions;
import org.jbpm.migration.JbpmMigrationRuntimeTest;
import org.jbpm.migration.tools.bpmn2.ExecutionInfo;
import org.jbpm.migration.tools.bpmn2.JavaNodeHandler;
import org.jbpm.migration.tools.jpdl.JpdlAssert;
import org.jbpm.migration.tools.jpdl.handlers.DefaultActionHandler;
import org.jbpm.migration.tools.jpdl.listeners.TrackingActionListener;
import org.jbpm.migration.tools.listeners.TrackingProcessEventListener;
import org.jbpm.graph.exe.ProcessInstance;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests that handler attached to single state node's enter and leave events
 * gets executed.
 *
 * https://issues.jboss.org/browse/JBPM-3681
 *
 */
public class SingleStateWithEventTest extends JbpmMigrationRuntimeTest {
    public static final String DEFINITION =
            "org/jbpm/migration/scenarios/singleStateWithEvent/processdefinition.xml";
    public static final String PROCESS_ID = "singleState_Process";

    private static final String BPMN_PRE_EVENT_NODE_NAME = "Expanded to execute: state enter";
    private static final String BPMN_POST_EVENT_NODE_NAME = "Expanded to execute: state leave";

    private static final String HANDLER_NAME =
            "org.jbpm.migration.tools.jpdl.handlers.DefaultActionHandler";
    private static final String HANDLER_EXECUTE_METHOD_NAME = "execute";

    @BeforeClass
    public static void getTestReady() {
        prepareProcess(DEFINITION);
    }

    @Test
    public void testJpdl() {
        ProcessInstance pi = processDef.createProcessInstance();

        TrackingActionListener listener = new TrackingActionListener();
        DefaultActionHandler.setTrackingListener(listener);
        DefaultActionHandler.setSignalization(false);

        pi.signal();
        JpdlAssert.assertProcessStarted(pi);

        listener.wasCalledOnNode("state");
        listener.wasEventAccepted("node-enter");

        pi.signal();

        listener.wasEventAccepted("node-leave");

        JpdlAssert.assertProcessCompleted(pi);
    }

    @Test
    public void testBpmn() {
        ksession = kbase.newKieSession();

        TrackingProcessEventListener listener = new TrackingProcessEventListener();
        ksession.addEventListener(listener);

        final JavaNodeHandler javaNodeHandler = new JavaNodeHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("JavaNode", javaNodeHandler);

        ksession.startProcess(PROCESS_ID);

        assertProcessStarted(listener, PROCESS_ID);
        assertTriggeredAndLeft(listener, BPMN_PRE_EVENT_NODE_NAME);
        verifyWorkItem(javaNodeHandler.getExecutionInfo(), 1);

        ksession.signalEvent("signal", null);

        assertTriggeredAndLeft(listener, BPMN_POST_EVENT_NODE_NAME);
        assertProcessCompleted(listener, PROCESS_ID);
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
