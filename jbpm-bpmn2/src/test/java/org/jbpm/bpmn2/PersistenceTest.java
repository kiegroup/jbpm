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
import org.drools.KnowledgeBase;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.impl.ClassPathResource;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.drools.runtime.process.WorkItem;
import org.junit.Test;

/**
 * bz818251 reproducer.
 * Process instance stays in "active" state after user task has been completed
 * @author rsynek
 */
public class PersistenceTest extends JbpmBpmn2TestCase {
    
    public PersistenceTest() {
        super(true);
    }
    
    @Test
    public void testUserTask() {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add( new ClassPathResource( "human-task.bpmn" ), ResourceType.BPMN2 );
        for (KnowledgeBuilderError error: kbuilder.getErrors()) {
            throw new RuntimeException("error while building knowledge base");
        }
        KnowledgeBase kbase = kbuilder.newKnowledgeBase();

        StatefulKnowledgeSession ksession = createKnowledgeSession(kbase);
        
        TestWorkItemHandler wih = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", wih);
        ProcessInstance pi = (ProcessInstance) ksession.startProcess("eclipse.human-task");
        
        assertEquals(ProcessInstance.STATE_ACTIVE, pi.getState());        
        WorkItem wi = wih.getWorkItem();
              

        assertEquals("worker1", wi.getParameter("ActorId"));
        ksession.getWorkItemManager().completeWorkItem(wi.getId(), new HashMap<String,Object>());        
             
        assertEquals(ProcessInstance.STATE_COMPLETED, pi.getState());
    }
}
