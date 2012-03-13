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
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.command.Command;
import org.drools.command.CommandFactory;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.drools.runtime.process.WorkflowProcessInstance;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Reproducer - bz802721
 * Simple start-process test through CommandFactory.newStartProcess.
 * This method does not pass parameters to process instance:
 * 
 * public static Command newStartProcess(String processId,
 *                                         Map<String, Object> parameters) {
 *       return getCommandFactoryProvider().newStartProcess( processId );
 *   }
 *
 * @author rsynek
 */
public class ProcessExecutionTest extends Assert {

    private StatefulKnowledgeSession ksession;

    @Before
    public void configureSession() {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newClassPathResource("sampleProcess.bpmn", getClass()), ResourceType.BPMN2);
        if (kbuilder.hasErrors()) {
            throw new RuntimeException(kbuilder.getErrors().toString());
        }

        ksession = kbuilder.newKnowledgeBase().newStatefulKnowledgeSession();
    }

    @Test
    public void testNewStartProcess() {
        final String processId = "org.drools.command.SampleProcess";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", 10);        

        ProcessInstance pi = (ProcessInstance) ksession.execute((Command<?>) CommandFactory.newStartProcess(processId, params));
        WorkflowProcessInstance wpi = (WorkflowProcessInstance) pi;
        Object x = wpi.getVariable("x");
        assertNotNull(x);
        assertEquals(10, ((Integer) x).intValue());
    }
}
