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

import org.jbpm.migration.JbpmMigrationRuntimeTest;
import org.jbpm.migration.tools.bpmn2.ExecutionAssert;
import org.jbpm.migration.tools.bpmn2.JavaNodeHandler;
import org.jbpm.migration.tools.jpdl.JpdlAssert;
import org.jbpm.migration.tools.jpdl.handlers.DefaultActionHandler;
import org.jbpm.migration.tools.jpdl.listeners.TrackingActionListener;
import org.jbpm.migration.tools.listeners.TrackingProcessEventListener;
import org.jbpm.graph.exe.ProcessInstance;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Single node scenario with two events inside - each the same action.
 *
 */
public class SingleNodeWithEventTest extends JbpmMigrationRuntimeTest {
    public static final String definition =
            "org/jbpm/migration/scenarios/singleNodeWithEvent/processdefinition.xml";

    public static final String processId = "SingleNode_Process";

    @BeforeClass
    public static void getTestReady() {
        prepareProcess(definition);
    }

    @Test
    public void testJpdl() {
        ProcessInstance pi = processDef.createProcessInstance();

        TrackingActionListener listener = new TrackingActionListener();
        DefaultActionHandler.setTrackingListener(listener);
        DefaultActionHandler.setSignalization(false);
        pi.signal();

        JpdlAssert.assertProcessStarted(pi);
        JpdlAssert.assertCalledOnNode(listener, "node", 2);
        JpdlAssert.assertCalledOnEvent(listener, "node-enter");
        JpdlAssert.assertCalledOnEvent(listener, "node-leave");
        JpdlAssert.assertProcessCompleted(pi);
    }

    @Test
    public void testBpmn() {
        ksession = kbase.newKieSession();

        TrackingProcessEventListener listener = new TrackingProcessEventListener();
        ksession.addEventListener(listener);

        JavaNodeHandler jwih = new JavaNodeHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("JavaNode", jwih);

        ksession.startProcess(processId);

        assertProcessStarted(listener, processId);

        assertTriggeredAndLeft(listener, "node");

        if (!jwih.getExceptions().isEmpty()) {
            Exception ex = jwih.getExceptions().iterator().next();
            throw new RuntimeException(ex);
        }

        ExecutionAssert.assertExecutedExactly(jwih,
                "org.jbpm.migration.tools.jpdl.handlers.DefaultActionHandler", "execute", 2);

        assertProcessCompleted(listener, processId);
    }
}
