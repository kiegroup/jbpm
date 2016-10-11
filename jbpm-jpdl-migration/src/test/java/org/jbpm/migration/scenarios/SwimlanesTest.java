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
import static org.jbpm.migration.tools.listeners.TrackingListenerAssert.assertTriggeredAndLeft;

import java.util.HashMap;

import org.assertj.core.api.Assertions;
import org.jbpm.migration.JbpmMigrationRuntimeTest;
import org.jbpm.migration.tools.jpdl.JpdlAssert;
import org.jbpm.migration.tools.jpdl.JpdlHelper;
import org.jbpm.migration.tools.jpdl.handlers.DefaultActionHandler;
import org.jbpm.migration.tools.jpdl.listeners.TrackingActionListener;
import org.jbpm.migration.tools.listeners.TrackingProcessEventListener;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.taskmgmt.exe.TaskInstance;
import org.jbpm.test.JbpmJUnitTestCase;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.runtime.process.WorkItem;

/**
 * Testing migration of swimlanes.
 *
 */
public class SwimlanesTest extends JbpmMigrationRuntimeTest {

    public static final String definition
            = "org/jbpm/migration/scenarios/swimlanesTest/processdefinition.xml";

    public static final String processId = "swimlanesTest_Process";

    @BeforeClass
    public static void getTestReady() {
        prepareProcess(definition);
    }

    @Test
    public void testJpdl() {
        ProcessInstance pi = processDef.createProcessInstance();

        TrackingActionListener listener = new TrackingActionListener();
        DefaultActionHandler.setTrackingListener(listener);

        pi.signal();

        JpdlAssert.assertProcessStarted(pi);

        TaskInstance ti = JpdlHelper.getTaskInstance("task-node1", pi);
        Assertions.assertThat(ti.getActorId()).isEqualTo("1111");
        ti.end();

        ti = JpdlHelper.getTaskInstance("task-node2", pi);
        Assertions.assertThat(ti.getActorId()).isEqualTo("2222");
        ti.end();

        JpdlAssert.assertProcessCompleted(pi);
    }

    @Test
    public void testBpmn() {
        ksession = kbase.newKieSession();
        TrackingProcessEventListener listener = new TrackingProcessEventListener();
        ksession.addEventListener(listener);

        JbpmJUnitTestCase.TestWorkItemHandler handler = new JbpmJUnitTestCase.TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", handler);

        ksession.startProcess(processId);

        WorkItem wi = handler.getWorkItem();
        ksession.getWorkItemManager().completeWorkItem(wi.getId(), new HashMap<String, Object>());
        assertTriggeredAndLeft(listener, "task-node1");

        wi = handler.getWorkItem();
        ksession.getWorkItemManager().completeWorkItem(wi.getId(), new HashMap<String, Object>());
        assertTriggeredAndLeft(listener, "task-node2");

        assertProcessCompleted(listener, processId);
    }
}
