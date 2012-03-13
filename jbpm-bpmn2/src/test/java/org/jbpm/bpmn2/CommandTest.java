/*
 * Copyright 2012 JBoss by Red Hat.
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
package org.jbpm.bpmn2;

import java.util.HashMap;
import java.util.Map;

import org.drools.KnowledgeBase;
import org.drools.command.Command;
import org.drools.command.CommandFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.drools.runtime.process.WorkflowProcessInstance;
import org.junit.Test;

/**
 * Test for commands involving processes
 */
public class CommandTest extends JbpmBpmn2TestCase {

    @Test
    // Reproducer - BZ 802721 : CommandFactory.newStartProcess did not pass parameters to process instance:
    public void testNewStartProcess() {
        KnowledgeBase kbase = createKnowledgeBase("BPMN2-MinimalProcess.bpmn2");
        StatefulKnowledgeSession ksession = createKnowledgeSession(kbase);
        
        final String processId = "Minimal";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", 10);        

        ProcessInstance pi = (ProcessInstance) ksession.execute((Command<?>) CommandFactory.newStartProcess(processId, params));
        WorkflowProcessInstance wpi = (WorkflowProcessInstance) pi;
        Object x = wpi.getVariable("x");
        assertNotNull(x);
        assertEquals(10, ((Integer) x).intValue());
    }
}
