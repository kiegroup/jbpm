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
import org.jbpm.migration.tools.jpdl.JpdlAssert;
import org.jbpm.migration.tools.listeners.TrackingProcessEventListener;
import org.jbpm.graph.exe.ProcessInstance;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Testing single state. State is waiting node - external signal is needed to
 * continue.
 *
 *
 */
public class SingleStateTest extends JbpmMigrationRuntimeTest {
    public static final String definition =
            "org/jbpm/migration/scenarios/singleState/processdefinition.xml";

    public static final String processId = "singleState_Process";

    @BeforeClass
    public static void getTestReady() {
        prepareProcess(definition);
    }

    @Test
    public void testJpdl() {
        ProcessInstance pi = processDef.createProcessInstance();
        pi.signal();
        JpdlAssert.assertProcessStarted(pi);
        pi.signal();
        JpdlAssert.assertProcessCompleted(pi);
    }

    @Test
    public void testBpmn() {
        ksession = kbase.newKieSession();

        TrackingProcessEventListener listener = new TrackingProcessEventListener();
        ksession.addEventListener(listener);

        ksession.startProcess(processId);
        assertProcessStarted(listener, processId);

        assertTriggeredAndLeft(listener, "start-state");

        ksession.signalEvent("signal", null);

        assertTriggeredAndLeft(listener, "state");
        assertTriggeredAndLeft(listener, "end-state");

        assertProcessCompleted(listener, processId);
    }
}
