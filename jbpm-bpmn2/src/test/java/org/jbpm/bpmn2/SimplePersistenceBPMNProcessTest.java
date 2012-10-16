package org.jbpm.bpmn2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.drools.KnowledgeBase;
import org.drools.event.process.DefaultProcessEventListener;
import org.drools.event.process.ProcessNodeLeftEvent;
import org.drools.event.process.ProcessNodeTriggeredEvent;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.NodeInstance;
import org.drools.runtime.process.ProcessInstance;
import org.drools.runtime.process.WorkItem;
import org.jbpm.process.instance.impl.demo.DoNothingWorkItemHandler;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.jbpm.workflow.instance.node.CompositeContextNodeInstance;
import org.jbpm.workflow.instance.node.ForEachNodeInstance;

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
    
    public void testInclusiveSplitAndJoinWithParallel() throws Exception {
        KnowledgeBase kbase = createKnowledgeBase("BPMN2-InclusiveSplitAndJoinWithParallel.bpmn2");
        StatefulKnowledgeSession ksession = createKnowledgeSession(kbase);
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", 25);
        ProcessInstance processInstance = ksession.startProcess(
                "com.sample.test", params);
        
        List<WorkItem> activeWorkItems = workItemHandler.getWorkItems();
        
        assertEquals(4, activeWorkItems.size());
        restoreSession(ksession, true);
        
        for (WorkItem wi : activeWorkItems) {
            ksession.getWorkItemManager().completeWorkItem(wi.getId(), null);
        }
        assertProcessInstanceCompleted(processInstance.getId(), ksession);
    }
    
    public void testInclusiveSplitAndJoinWithEnd() throws Exception {
        KnowledgeBase kbase = createKnowledgeBase("BPMN2-InclusiveSplitAndJoinWithEnd.bpmn2");
        StatefulKnowledgeSession ksession = createKnowledgeSession(kbase);
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", 25);
        ProcessInstance processInstance = ksession.startProcess(
                "com.sample.test", params);
        
        List<WorkItem> activeWorkItems = workItemHandler.getWorkItems();
        
        assertEquals(3, activeWorkItems.size());
        restoreSession(ksession, true);
        
        for (int i = 0; i < 2; i++) {
            ksession.getWorkItemManager().completeWorkItem(activeWorkItems.get(i).getId(), null);
        }
        assertProcessInstanceActive(processInstance.getId(), ksession);
        
        ksession.getWorkItemManager().completeWorkItem(activeWorkItems.get(2).getId(), null);
        assertProcessInstanceCompleted(processInstance.getId(), ksession);
    }
    
    public void testInclusiveSplitAndJoinWithTimer() throws Exception {
        KnowledgeBase kbase = createKnowledgeBase("BPMN2-InclusiveSplitAndJoinWithTimer.bpmn2");
        StatefulKnowledgeSession ksession = createKnowledgeSession(kbase);
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", 15);
        ProcessInstance processInstance = ksession.startProcess(
                "com.sample.test", params);
 
        List<WorkItem> activeWorkItems = workItemHandler.getWorkItems();

        assertEquals(1, activeWorkItems.size());
        ksession.getWorkItemManager().completeWorkItem(activeWorkItems.get(0).getId(), null);
        ksession.fireAllRules();
        Thread.sleep(3000);
        assertProcessInstanceActive(processInstance.getId(), ksession);

        activeWorkItems = workItemHandler.getWorkItems();
        assertEquals(2, activeWorkItems.size());
        
        ksession.getWorkItemManager().completeWorkItem(activeWorkItems.get(0).getId(), null);
        assertProcessInstanceActive(processInstance.getId(), ksession);

        ksession.getWorkItemManager().completeWorkItem(activeWorkItems.get(1).getId(), null);
        assertProcessInstanceCompleted(processInstance.getId(), ksession);
    }
    
    public void testInclusiveSplitAndJoinExtraPath() throws Exception {
        KnowledgeBase kbase = createKnowledgeBase("BPMN2-InclusiveSplitAndJoinExtraPath.bpmn2");
        StatefulKnowledgeSession ksession = createKnowledgeSession(kbase);
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", 25);
        ProcessInstance processInstance = ksession.startProcess(
                "com.sample.test", params);
        
        ksession.signalEvent("signal", null);
        
        List<WorkItem> activeWorkItems = workItemHandler.getWorkItems();
        
        assertEquals(4, activeWorkItems.size());
        restoreSession(ksession, true);
        
        for (int i = 0; i < 3; i++) {
            ksession.getWorkItemManager().completeWorkItem(activeWorkItems.get(i).getId(), null);
        }
        assertProcessInstanceActive(processInstance.getId(), ksession);

        ksession.getWorkItemManager().completeWorkItem(activeWorkItems.get(3).getId(), null);
        assertProcessInstanceCompleted(processInstance.getId(), ksession);
    }
    
    public void testMultiInstanceLoopCharacteristicsProcessWithORGateway() throws Exception {
        KnowledgeBase kbase = createKnowledgeBase("BPMN2-MultiInstanceLoopCharacteristicsProcessWithORgateway.bpmn2");
        StatefulKnowledgeSession ksession = createKnowledgeSession(kbase);
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);
        Map<String, Object> params = new HashMap<String, Object>();
        List<Integer> myList = new ArrayList<Integer>();
        myList.add(12);
        myList.add(15);
        params.put("list", myList);
        ProcessInstance processInstance = ksession.startProcess(
                "MultiInstanceLoopCharacteristicsProcess", params);
        
        List<WorkItem> workItems = workItemHandler.getWorkItems();
        assertEquals(4, workItems.size());
        
        Collection<NodeInstance> nodeInstances = ((WorkflowProcessInstanceImpl) processInstance).getNodeInstances();
        assertEquals(1, nodeInstances.size());
        NodeInstance nodeInstance = nodeInstances.iterator().next(); 
        assertTrue(nodeInstance instanceof ForEachNodeInstance);
        
        Collection<NodeInstance> nodeInstancesChild = ((ForEachNodeInstance) nodeInstance).getNodeInstances();
        assertEquals(2, nodeInstancesChild.size());
        
        for (NodeInstance child : nodeInstancesChild) {
            assertTrue(child instanceof CompositeContextNodeInstance);
            assertEquals(2, ((CompositeContextNodeInstance) child).getNodeInstances().size());
        }
        
        ksession.getWorkItemManager().completeWorkItem(workItems.get(0).getId(), null);
        ksession.getWorkItemManager().completeWorkItem(workItems.get(1).getId(), null);
        
        processInstance = ksession.getProcessInstance(processInstance.getId());
        
        nodeInstances = ((WorkflowProcessInstanceImpl) processInstance).getNodeInstances();
        assertEquals(1, nodeInstances.size());
        nodeInstance = nodeInstances.iterator().next(); 
        assertTrue(nodeInstance instanceof ForEachNodeInstance);
        
        nodeInstancesChild = ((ForEachNodeInstance) nodeInstance).getNodeInstances();
        assertEquals(1, nodeInstancesChild.size());
        
        Iterator<NodeInstance> childIterator = nodeInstancesChild.iterator();
        
        assertTrue(childIterator.next() instanceof CompositeContextNodeInstance);
        
        ksession.getWorkItemManager().completeWorkItem(workItems.get(2).getId(), null);
        ksession.getWorkItemManager().completeWorkItem(workItems.get(3).getId(), null);
        
        assertProcessInstanceCompleted(processInstance.getId(), ksession);
    }
}
