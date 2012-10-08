package org.jbpm.bpmn2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.KnowledgeBase;
import org.drools.event.process.DefaultProcessEventListener;
import org.drools.event.process.ProcessNodeLeftEvent;
import org.drools.event.process.ProcessNodeTriggeredEvent;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.drools.runtime.process.WorkItem;
import org.jbpm.bpmn2.JbpmBpmn2TestCase.TestWorkItemHandler;
import org.jbpm.process.instance.impl.demo.DoNothingWorkItemHandler;

public class SimplePersistenceBPMNProcessTest extends JbpmBpmn2TestCase {
    
    public SimplePersistenceBPMNProcessTest() {
        super(true);
    }
    
    public void testCompensateEndEventProcess() throws Exception {
        KnowledgeBase kbase = createKnowledgeBase("BPMN2-CompensateEndEvent.bpmn2");
        StatefulKnowledgeSession ksession = createKnowledgeSession(kbase);
        ProcessInstance processInstance = ksession
                .startProcess("CompensateEndEvent");
        assertProcessInstanceCompleted(processInstance.getId(), ksession);
        assertNodeTriggered(processInstance.getId(), "StartProcess", "Task", "CompensateEvent", "CompensateEvent2", "Compensate", "EndEvent");
    }
    
    public void testSignalBoundaryEventOnTask() throws Exception {
        KnowledgeBase kbase = createKnowledgeBase("BPMN2-BoundarySignalEventOnTaskbpmn2.bpmn");
        StatefulKnowledgeSession ksession = createKnowledgeSession(kbase);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                new TestWorkItemHandler());
        ksession.addEventListener(new DefaultProcessEventListener() {

            @Override
            public void afterNodeLeft(ProcessNodeLeftEvent event) {
                System.out.println("After node left " + event.getNodeInstance().getNodeName());
            }

            @Override
            public void afterNodeTriggered(ProcessNodeTriggeredEvent event) {
                System.out.println("After node triggered " + event.getNodeInstance().getNodeName());
            }

            @Override
            public void beforeNodeLeft(ProcessNodeLeftEvent event) {
                System.out.println("Before node left " + event.getNodeInstance().getNodeName());
            }

            @Override
            public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
                System.out.println("Before node triggered " + event.getNodeInstance().getNodeName());
            }
           
        });
        ProcessInstance processInstance = ksession.startProcess("BoundarySignalOnTask");
        ksession.signalEvent("MySignal", "hello");
        assertProcessInstanceCompleted(processInstance.getId(), ksession);
        assertNodeTriggered(processInstance.getId(), "StartProcess", "User Task", "Boundary event", "Signal received", "End2");
    }
    
    public void testIntermediateCatchEventSignal() throws Exception {
        KnowledgeBase kbase = createKnowledgeBase("BPMN2-IntermediateCatchEventSignal.bpmn2");
        StatefulKnowledgeSession ksession = createKnowledgeSession(kbase);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                new DoNothingWorkItemHandler());
        ProcessInstance processInstance = ksession
                .startProcess("IntermediateCatchEvent");
        assertTrue(processInstance.getState() == ProcessInstance.STATE_ACTIVE);
        ksession = restoreSession(ksession, true);
        // now signal process instance
        ksession.signalEvent("MyMessage", "SomeValue", processInstance.getId());
        assertProcessInstanceCompleted(processInstance.getId(), ksession);
        assertNodeTriggered(processInstance.getId(), "StartProcess", "UserTask", "EndProcess", "event");
    }

    public void testInclusiveSplitAndJoin() throws Exception {
        KnowledgeBase kbase = createKnowledgeBase("BPMN2-InclusiveSplitAndJoin.bpmn2");
        StatefulKnowledgeSession ksession = createKnowledgeSession(kbase);
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", 15);
        ProcessInstance processInstance = ksession.startProcess(
                "com.sample.test", params);
        
        List<WorkItem> activeWorkItems = workItemHandler.getWorkItems();
        
        assertEquals(2, activeWorkItems.size());
        restoreSession(ksession, true);
        
        for (WorkItem wi : activeWorkItems) {
            ksession.getWorkItemManager().completeWorkItem(wi.getId(), null);
        }
        assertProcessInstanceCompleted(processInstance.getId(), ksession);
    }
    
    public void testInclusiveSplitAndJoinNested() throws Exception {
        KnowledgeBase kbase = createKnowledgeBase("BPMN2-InclusiveSplitAndJoinNested.bpmn2");
        StatefulKnowledgeSession ksession = createKnowledgeSession(kbase);
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", 15);
        ProcessInstance processInstance = ksession.startProcess(
                "com.sample.test", params);
        
        List<WorkItem> activeWorkItems = workItemHandler.getWorkItems();
        
        assertEquals(2, activeWorkItems.size());
        restoreSession(ksession, true);
        
        for (WorkItem wi : activeWorkItems) {
            ksession.getWorkItemManager().completeWorkItem(wi.getId(), null);
        }
        
        activeWorkItems = workItemHandler.getWorkItems();
        assertEquals(2, activeWorkItems.size());
        restoreSession(ksession, true);
        
        for (WorkItem wi : activeWorkItems) {
            ksession.getWorkItemManager().completeWorkItem(wi.getId(), null);
        }
        assertProcessInstanceCompleted(processInstance.getId(), ksession);
    }
    
    public void testInclusiveSplitAndJoinEmbedded() throws Exception {
        KnowledgeBase kbase = createKnowledgeBase("BPMN2-InclusiveSplitAndJoinEmbedded.bpmn2");
        StatefulKnowledgeSession ksession = createKnowledgeSession(kbase);
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", 15);
        ProcessInstance processInstance = ksession.startProcess(
                "com.sample.test", params);
        
        List<WorkItem> activeWorkItems = workItemHandler.getWorkItems();
        
        assertEquals(2, activeWorkItems.size());
        restoreSession(ksession, true);
        
        for (WorkItem wi : activeWorkItems) {
            ksession.getWorkItemManager().completeWorkItem(wi.getId(), null);
        }
        assertProcessInstanceCompleted(processInstance.getId(), ksession);
    }
}
