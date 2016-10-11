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

import java.util.HashMap;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.jbpm.migration.JbpmMigrationRuntimeTest;
import org.jbpm.migration.tools.jpdl.JpdlAssert;
import org.jbpm.migration.tools.jpdl.handlers.VariableActionHandler;
import org.jbpm.migration.tools.jpdl.listeners.TrackingVariableChangeListener;
import org.jbpm.migration.tools.listeners.TrackingProcessEventListener;
import org.jbpm.graph.exe.ProcessInstance;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.runtime.process.WorkflowProcessInstance;

/**
 * Testing process with single subprocess node.
 */
@org.junit.Ignore("take a loot at https://issues.jboss.org/browse/JBPM-3684 , comment #3")
public class SingleSubFlowVariableChangeTest extends JbpmMigrationRuntimeTest {
    public static final String topProcessDefinition =
            "org/jbpm/migration/scenarios/singleSubFlowVariableChange/processdefinition.xml";
    public static final String subProcessDefinition =
            "org/jbpm/migration/scenarios/singleSubFlowVariableChange/subprocessdefinition.xml";

    public static final String processId = "singleSubFlowVariableChange_Process";

    private static final int OLD_VALUE = 10;
    private static final int NEW_VALUE = 20;

    @BeforeClass
    public static void getTestReady() {
        prepareProcess(topProcessDefinition);
        addBpmnProcessFromClassPath(
                "/org/jbpm/migration/scenarios/singleSubFlowVariableChange/subprocess.bpmn");
    }

    @Test
    public void testJpdl() {
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("price", OLD_VALUE);

        ProcessInstance pi = processDef.createProcessInstance(variables);

        // preparation for recording variable changes
        TrackingVariableChangeListener variableListener = new TrackingVariableChangeListener();
        prepareValidationHandler(variableListener);
        variableListener.recordOldValues(pi.getContextInstance());

        pi.signal();
        JpdlAssert.assertProcessStarted(pi);

        variableListener.recordNewValues(pi.getContextInstance());

        JpdlAssert.assertProcessCompleted(pi);

        JpdlAssert.assertVarLastChange(variableListener, "newPrice", OLD_VALUE, NEW_VALUE);
        JpdlAssert.assertVarLastChange(variableListener, "price", OLD_VALUE, NEW_VALUE);
    }

    @Test
    public void testBpmn() {
        ksession = kbase.newKieSession();
        TrackingProcessEventListener listener = new TrackingProcessEventListener();
        ksession.addEventListener(listener);

        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("price", OLD_VALUE);

        WorkflowProcessInstance wpi = (WorkflowProcessInstance) ksession.startProcess(processId, params);
        Assertions.assertThat(wpi.getVariable("price")).isEqualTo((Object) NEW_VALUE);
        assertProcessCompleted(listener, processId);
    }

    private void prepareValidationHandler(final TrackingVariableChangeListener variableListener) {
        VariableActionHandler.setVariableListener(variableListener);

        Map<String, Object> variableChanges = new HashMap<String, Object>();
        variableChanges.put("newPrice", NEW_VALUE);
        VariableActionHandler.setVariableChanges(variableChanges);
    }
}
