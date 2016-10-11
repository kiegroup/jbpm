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

import org.jbpm.migration.JbpmMigrationRuntimeTest;
import org.jbpm.migration.tools.jpdl.JpdlAssert;
import org.jbpm.migration.tools.jpdl.handlers.DefaultActionHandler;
import org.jbpm.migration.tools.jpdl.listeners.TrackingActionListener;
import org.jbpm.migration.tools.listeners.TrackingProcessEventListener;
import org.jbpm.migration.tools.listeners.TrackingListenerAssert;
import org.jbpm.graph.exe.ProcessInstance;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests node group inside super state.
 *
 * Verifies https://issues.jboss.org/browse/JBPM-3693
 *
 */
public class SingleSuperStateTest extends JbpmMigrationRuntimeTest {
    public static final String definition =
            "org/jbpm/migration/scenarios/singleSuperState/processdefinition.xml";

    public static final String processId = "singleSuperState_Process";

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

        JpdlAssert.assertProcessCompleted(pi);
    }

    @Test
    public void testBpmn() {
        ksession = kbase.newKieSession();

        TrackingProcessEventListener listener = new TrackingProcessEventListener();
        ksession.addEventListener(listener);

        ksession.startProcess(processId);

        TrackingListenerAssert.assertProcessStarted(listener, processId);
        TrackingListenerAssert.assertProcessCompleted(listener, processId);
    }
}
