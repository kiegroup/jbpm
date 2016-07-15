package org.jbpm.bpmn2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.drools.core.command.impl.GenericCommand;
import org.drools.core.command.impl.KnowledgeCommandContext;
import org.jbpm.bpmn2.objects.TestWorkItemHandler;
import org.jbpm.bpmn2.test.Broken;
import org.jbpm.bpmn2.test.Fixed;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.instance.context.variable.VariableScopeInstance;
import org.jbpm.process.instance.impl.demo.SystemOutWorkItemHandler;
import org.jbpm.test.util.CountDownProcessEventListener;
import org.jbpm.workflow.instance.impl.NodeInstanceImpl;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.jbpm.workflow.instance.node.CompositeContextNodeInstance;
import org.jbpm.workflow.instance.node.ForEachNodeInstance;
import org.jbpm.workflow.instance.node.ForEachNodeInstance.ForEachJoinNodeInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.kie.api.KieBase;
import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessNodeTriggeredEvent;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItem;
import org.kie.internal.command.Context;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Parameterized.class)
public class MultiInstanceTest extends JbpmBpmn2TestCase {

    @Parameters
    public static Collection<Object[]> persistence() {
        Object[][] data = new Object[][] {
            { false, false },
            { false, true },
            { true, false },
            { true, true }
            };
        return Arrays.asList(data);
    };

    private static final Logger logger = LoggerFactory.getLogger(FlowTest.class);

    private KieSession ksession;

    public MultiInstanceTest(boolean persistence, boolean stackless) {
        super(persistence, false, stackless);
    }

    @BeforeClass
    public static void setup() throws Exception {
        setUpDataSource();
        VariableScope.setVariableStrictOption(true);
    }

    @Before
    public void setProperty() {
        System.setProperty("jbpm.enable.multi.con", "true");
    }

    @After
    public void dispose() {
        if (ksession != null) {
            ksession.dispose();
            ksession = null;
        }
        System.clearProperty("jbpm.enable.multi.con");
    }

    static class GetProcessVariableCommand implements GenericCommand<Object> {

        private long processInstanceId;
        private String variableName;


        public GetProcessVariableCommand(long processInstanceId, String variableName) {
            this.processInstanceId = processInstanceId;
            this.variableName = variableName;
        }


        public Object execute(Context context) {
            KieSession ksession = ((KnowledgeCommandContext) context).getKieSession();

            org.jbpm.process.instance.ProcessInstance processInstance =
                    (org.jbpm.process.instance.ProcessInstance) ksession.getProcessInstance(processInstanceId);

            VariableScopeInstance variableScope =
                    (VariableScopeInstance) processInstance.getContextInstance(VariableScope.VARIABLE_SCOPE);

            Object variable = variableScope.getVariable(variableName);

            return variable;
        }

    }

    // TESTS ----------------------------------------------------------------------------------------------------------------------

    @Test(timeout=10000)
    public void testTimerMultipleInstances() throws Exception {
        CountDownProcessEventListener countDownListener = new CountDownProcessEventListener("timer", 3);
        KieBase kbase = createKnowledgeBase("BPMN2-MultiInstanceLoopBoundaryTimer.bpmn2");

        ksession = createKnowledgeSession(kbase);
        ksession.addEventListener(countDownListener);
        TestWorkItemHandler handler = new TestWorkItemHandler();

        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", handler);
        ProcessInstance processInstance = ksession.startProcess("boundaryTimerMultipleInstances");
        assertProcessInstanceActive(processInstance);

        countDownListener.waitTillCompleted();

        List<WorkItem> workItems = handler.getWorkItems();
        assertNotNull(workItems);
        assertEquals(3, workItems.size());

        for (WorkItem wi : workItems) {
            ksession.getWorkItemManager().completeWorkItem(wi.getId(), null);
        }

        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    public void testSignalBoundaryEventOnMultiInstanceSubprocess() throws Exception {
        KieBase kbase = createKnowledgeBase(
                "subprocess/BPMN2-MultiInstanceSubprocessWithBoundarySignal.bpmn2");
        StatefulKnowledgeSession ksession = createKnowledgeSession(kbase);
        TestWorkItemHandler handler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", handler);

        Map<String, Object> params = new HashMap<String, Object>();
        List<String> approvers = new ArrayList<String>();
        approvers.add("john");
        approvers.add("john");

        params.put("approvers", approvers);

        ProcessInstance processInstance = ksession.startProcess("boundary-catch-error-event", params);
        assertProcessInstanceActive(processInstance);

        List<WorkItem> workItems = handler.getWorkItems();
        assertNotNull(workItems);
        assertEquals(2, workItems.size());

        ksession.signalEvent("Outside", null, processInstance.getId());
        assertProcessInstanceFinished(processInstance, ksession);

        ksession.dispose();
    }

    @Test
    public void testSignalBoundaryEventNoInteruptOnMultiInstanceSubprocess() throws Exception {
        KieBase kbase = createKnowledgeBase(
                "subprocess/BPMN2-MultiInstanceSubprocessWithBoundarySignalNoInterupting.bpmn2");
        StatefulKnowledgeSession ksession = createKnowledgeSession(kbase);
        TestWorkItemHandler handler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", handler);

        Map<String, Object> params = new HashMap<String, Object>();
        List<String> approvers = new ArrayList<String>();
        approvers.add("john");
        approvers.add("john");

        params.put("approvers", approvers);

        ProcessInstance processInstance = ksession.startProcess("boundary-catch-error-event", params);
        assertProcessInstanceActive(processInstance);

        List<WorkItem> workItems = handler.getWorkItems();
        assertNotNull(workItems);
        assertEquals(2, workItems.size());

        ksession.signalEvent("Outside", null, processInstance.getId());

        assertProcessInstanceActive(processInstance.getId(), ksession);

        for (WorkItem wi : workItems) {
            ksession.getWorkItemManager().completeWorkItem(wi.getId(), null);
        }
        assertProcessInstanceFinished(processInstance, ksession);

        ksession.dispose();
    }

    @Test
    public void testErrorBoundaryEventOnMultiInstanceSubprocess() throws Exception {
        KieBase kbase = createKnowledgeBase(
                "subprocess/BPMN2-MultiInstanceSubprocessWithBoundaryError.bpmn2");
        StatefulKnowledgeSession ksession = createKnowledgeSession(kbase);
        TestWorkItemHandler handler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", handler);

        Map<String, Object> params = new HashMap<String, Object>();
        List<String> approvers = new ArrayList<String>();
        approvers.add("john");
        approvers.add("john");

        params.put("approvers", approvers);

        ProcessInstance processInstance = ksession.startProcess("boundary-catch-error-event", params);
        assertProcessInstanceActive(processInstance);

        List<WorkItem> workItems = handler.getWorkItems();
        assertNotNull(workItems);
        assertEquals(2, workItems.size());

        ksession.signalEvent("Inside", null, processInstance.getId());
        assertProcessInstanceFinished(processInstance, ksession);

        ksession.dispose();
    }

    @Test
    public void testMultiInstanceLoopCharacteristicsProcess() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-MultiInstanceLoopCharacteristicsProcess.bpmn2");
        ksession = createKnowledgeSession(kbase);
        Map<String, Object> params = new HashMap<String, Object>();
        List<String> myList = new ArrayList<String>();
        myList.add("First Item");
        myList.add("Second Item");
        params.put("list", myList);
        ProcessInstance processInstance = ksession.startProcess(
                "MultiInstanceLoopCharacteristicsProcess", params);
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testMultiInstanceLoopNumberTest() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-MultiInstanceLoop-Numbering.bpmn2");
        ksession = createKnowledgeSession(kbase);
        Map<String, Object> params = new HashMap<String, Object>();

        final Map<String, String> nodeIdNodeNameMap = new HashMap<String, String>();
        ksession.addEventListener(new DefaultProcessEventListener() {

            @Override
            public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
                NodeInstance nodeInstance = event.getNodeInstance();
                String uniqId = ((NodeInstanceImpl) nodeInstance).getUniqueId();
                String nodeName = ((NodeInstanceImpl) nodeInstance).getNode().getName();

                String prevNodeName = nodeIdNodeNameMap.put( uniqId, nodeName );
                if( prevNodeName != null ) {
                    assertEquals(uniqId + " is used for more than one node instance: ", prevNodeName, nodeName);
                }
            }

        });

        TestWorkItemHandler handler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", handler);

        ProcessInstance processInstance = ksession.startProcess("Test.MultipleInstancesBug", params);

        List<WorkItem> workItems = handler.getWorkItems();
        logger.debug( "COMPLETING TASKS.");
        ksession.getWorkItemManager().completeWorkItem(workItems.remove(0).getId(), null);
        ksession.getWorkItemManager().completeWorkItem(workItems.remove(0).getId(), null);

        assertProcessInstanceCompleted(processInstance);

    }

    @Test
    public void testMultiInstanceLoopCharacteristicsProcess2() throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("BPMN2-MultiInstanceProcessWithOutputOnTask.bpmn2");
        ksession = createKnowledgeSession(kbase);
        TestWorkItemHandler handler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", handler);
        Map<String, Object> params = new HashMap<String, Object>();
        List<String> myList = new ArrayList<String>();
        List<String> myOutList = null;
        myList.add("John");
        myList.add("Mary");
        params.put("miinput", myList);

        ProcessInstance processInstance = ksession.startProcess("miprocess", params);
        List<WorkItem> workItems = handler.getWorkItems();
        assertNotNull(workItems);
        assertEquals(2, workItems.size());

        myOutList = (List<String>) ksession.execute(new GetProcessVariableCommand(processInstance.getId(), "mioutput"));
        assertNull(myOutList);


        Map<String, Object> results = new HashMap<String, Object>();
        results.put("reply", "Hello John");
        ksession.getWorkItemManager().completeWorkItem(workItems.get(0).getId(), results);

        myOutList = (List<String>) ksession.execute(new GetProcessVariableCommand(processInstance.getId(), "mioutput"));
        assertNull(myOutList);

        results = new HashMap<String, Object>();
        results.put("reply", "Hello Mary");
        ksession.getWorkItemManager().completeWorkItem(workItems.get(1).getId(), results);

        myOutList = (List<String>) ksession.execute(new GetProcessVariableCommand(processInstance.getId(), "mioutput"));
        assertNotNull(myOutList);
        assertEquals(2, myOutList.size());
        assertTrue(myOutList.contains("Hello John"));
        assertTrue(myOutList.contains("Hello Mary"));

        ksession.getWorkItemManager().completeWorkItem(handler.getWorkItem().getId(), null);

        assertProcessInstanceFinished(processInstance, ksession);

    }

    @Test
    @Fixed
    public void testMultiInstanceLoopCharacteristicsProcessWithOutput()
            throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("BPMN2-MultiInstanceLoopCharacteristicsProcessWithOutput.bpmn2");
        ksession = createKnowledgeSession(kbase);
        Map<String, Object> params = new HashMap<String, Object>();
        List<String> myList = new ArrayList<String>();
        List<String> myListOut = new ArrayList<String>();
        myList.add("First Item");
        myList.add("Second Item");
        params.put("list", myList);
        params.put("listOut", myListOut);
        assertEquals(0, myListOut.size());
        ProcessInstance processInstance = ksession.startProcess(
                "MultiInstanceLoopCharacteristicsProcessWithOutput", params);
        assertProcessInstanceCompleted(processInstance);
        assertEquals(2, myListOut.size());

    }

    @Test
    @Fixed
    public void testMultiInstanceLoopCharacteristicsProcessWithOutputCompletionCondition()
            throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("BPMN2-MultiInstanceLoopCharacteristicsProcessWithOutputCmpCond.bpmn2");
        ksession = createKnowledgeSession(kbase);
        Map<String, Object> params = new HashMap<String, Object>();
        List<String> myList = new ArrayList<String>();
        List<String> myListOut = new ArrayList<String>();
        myList.add("First Item");
        myList.add("Second Item");
        params.put("list", myList);
        params.put("listOut", myListOut);
        assertEquals(0, myListOut.size());
        ProcessInstance processInstance = ksession.startProcess(
                "MultiInstanceLoopCharacteristicsProcessWithOutput", params);
        assertProcessInstanceCompleted(processInstance);
        assertEquals(1, myListOut.size());

    }

    @Test
    @Fixed
    public void testMultiInstanceLoopCharacteristicsProcessWithOutputAndScripts()
            throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("BPMN2-MultiInstanceLoopCharacteristicsProcessWithOutputAndScripts.bpmn2");
        ksession = createKnowledgeSession(kbase);
        Map<String, Object> params = new HashMap<String, Object>();
        List<String> myList = new ArrayList<String>();
        List<String> myListOut = new ArrayList<String>();
        List<String> scriptList = new ArrayList<String>();
        myList.add("First Item");
        myList.add("Second Item");
        params.put("list", myList);
        params.put("listOut", myListOut);
        params.put("scriptList", scriptList);
        assertEquals(0, myListOut.size());
        ProcessInstance processInstance = ksession.startProcess("MultiInstanceLoopCharacteristicsProcessWithOutput", params);
        assertProcessInstanceCompleted(processInstance);
        assertEquals(2, myListOut.size());
        assertEquals(2, scriptList.size());

    }

    @Test
    public void testMultiInstanceLoopCharacteristicsTaskWithOutput()
            throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("BPMN2-MultiInstanceLoopCharacteristicsTaskWithOutput.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                new SystemOutWorkItemHandler());
        Map<String, Object> params = new HashMap<String, Object>();
        List<String> myList = new ArrayList<String>();
        List<String> myListOut = new ArrayList<String>();
        myList.add("First Item");
        myList.add("Second Item");
        params.put("list", myList);
        params.put("listOut", myListOut);
        assertEquals(0, myListOut.size());
        ProcessInstance processInstance = ksession.startProcess(
                "MultiInstanceLoopCharacteristicsTask", params);
        assertProcessInstanceCompleted(processInstance);
        assertEquals(2, myListOut.size());

    }

    @Test
    public void testMultiInstanceLoopCharacteristicsTaskWithOutputCompletionCondition()
            throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("BPMN2-MultiInstanceLoopCharacteristicsTaskWithOutputCmpCond.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                new SystemOutWorkItemHandler());
        Map<String, Object> params = new HashMap<String, Object>();
        List<String> myList = new ArrayList<String>();
        List<String> myListOut = new ArrayList<String>();
        myList.add("First Item");
        myList.add("Second Item");
        params.put("list", myList);
        params.put("listOut", myListOut);
        assertEquals(0, myListOut.size());
        ProcessInstance processInstance = ksession.startProcess(
                "MultiInstanceLoopCharacteristicsTask", params);
        assertProcessInstanceCompleted(processInstance);
        assertEquals(1, myListOut.size());

    }

    @Test
    public void testMultiInstanceLoopCharacteristicsTaskWithOutputCompletionCondition2()
            throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("BPMN2-MultiInstanceLoopCharacteristicsTaskWithOutputCmpCond2.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                new SystemOutWorkItemHandler());
        Map<String, Object> params = new HashMap<String, Object>();
        List<String> myList = new ArrayList<String>();
        List<String> myListOut = new ArrayList<String>();
        myList.add("approved");
        myList.add("rejected");
        myList.add("approved");
        myList.add("approved");
        myList.add("rejected");
        params.put("list", myList);
        params.put("listOut", myListOut);
        assertEquals(0, myListOut.size());
        ProcessInstance processInstance = ksession.startProcess(
                "MultiInstanceLoopCharacteristicsTask", params);
        assertProcessInstanceCompleted(processInstance);
        // only two approved outcomes are required to complete multiinstance and since there was reject in between we should have
        // three elements in the list
        assertEquals(3, myListOut.size());

    }

    @Test
    public void testMultiInstanceLoopCharacteristicsTask() throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("BPMN2-MultiInstanceLoopCharacteristicsTask.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                new SystemOutWorkItemHandler());
        Map<String, Object> params = new HashMap<String, Object>();
        List<String> myList = new ArrayList<String>();
        myList.add("First Item");
        myList.add("Second Item");
        params.put("list", myList);
        ProcessInstance processInstance = ksession.startProcess(
                "MultiInstanceLoopCharacteristicsTask", params);
        assertProcessInstanceCompleted(processInstance);

    }

    @Test
    public void testMultipleInOutgoingSequenceFlows() throws Exception {
        CountDownProcessEventListener countDownListener = new CountDownProcessEventListener("timer", 1);
        KieBase kbase = createKnowledgeBase("BPMN2-MultipleInOutgoingSequenceFlows.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ksession.addEventListener(countDownListener);
        final List<Long> list = new ArrayList<Long>();
        ksession.addEventListener(new DefaultProcessEventListener() {
            public void beforeProcessStarted(ProcessStartedEvent event) {
                list.add(event.getProcessInstance().getId());
            }
        });

        assertEquals(0, list.size());

        countDownListener.waitTillCompleted();

        assertEquals(1, list.size());
    }

    @Test
    public void testMultipleIncomingFlowToEndNode() throws Exception {

        KieBase kbase = createKnowledgeBase("BPMN2-MultipleFlowEndNode.bpmn2");
        ksession = createKnowledgeSession(kbase);

        ProcessInstance processInstance = ksession.startProcess("MultipleFlowEndNode");
        assertProcessInstanceCompleted(processInstance);
    }

    @Test
    public void testMultipleEnabledOnSingleConditionalSequenceFlow() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-MultiConnEnabled.bpmn2");
        ksession = createKnowledgeSession(kbase);

        final List<Long> list = new ArrayList<Long>();
        ksession.addEventListener(new DefaultProcessEventListener() {
            public void afterNodeTriggered(org.kie.api.event.process.ProcessNodeTriggeredEvent event) {
                if ("Task2".equals(event.getNodeInstance().getNodeName())) {
                    list.add(event.getNodeInstance().getNodeId());
                }
            }
        });

        assertEquals(0, list.size());
        ProcessInstance processInstance = ksession.startProcess("BPMN2-MultiConnEnabled");
        assertProcessInstanceActive(processInstance);
        ksession.signalEvent("signal", null, processInstance.getId());
        assertProcessInstanceCompleted(processInstance);

        assertEquals(1, list.size());

    }

    @Test
    public void testMultipleInOutgoingSequenceFlowsDisable() throws Exception {
        System.setProperty("jbpm.enable.multi.con", "false");
        try {
            KieBase kbase = createKnowledgeBase("BPMN2-MultipleInOutgoingSequenceFlows.bpmn2");
            fail("Should fail as multiple outgoing and incoming connections are disabled by default");
        } catch (Exception e) {
            assertEquals(
                    "This type of node [ScriptTask_1, Script Task] cannot have more than one outgoing connection!",
                    e.getMessage());
        }
    }

    @Test
    public void testMultiInstanceLoopCharacteristicsProcessWithORGateway()
            throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("BPMN2-MultiInstanceLoopCharacteristicsProcessWithORgateway.bpmn2");
        ksession = createKnowledgeSession(kbase);
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

        Collection<NodeInstance> nodeInstances = ((WorkflowProcessInstanceImpl) processInstance)
                .getNodeInstances();
        assertEquals(1, nodeInstances.size());
        NodeInstance nodeInstance = nodeInstances.iterator().next();
        assertTrue(nodeInstance instanceof ForEachNodeInstance);

        Collection<NodeInstance> nodeInstancesChild = ((ForEachNodeInstance) nodeInstance)
                .getNodeInstances();
        assertEquals(2, nodeInstancesChild.size());

        for (NodeInstance child : nodeInstancesChild) {
            assertTrue(child instanceof CompositeContextNodeInstance);
            assertEquals(2, ((CompositeContextNodeInstance) child)
                    .getNodeInstances().size());
        }

        ksession.getWorkItemManager().completeWorkItem(
                workItems.get(0).getId(), null);
        ksession.getWorkItemManager().completeWorkItem(
                workItems.get(1).getId(), null);

        processInstance = ksession.getProcessInstance(processInstance.getId());
        nodeInstances = ((WorkflowProcessInstanceImpl) processInstance)
                .getNodeInstances();
        assertEquals(1, nodeInstances.size());
        nodeInstance = nodeInstances.iterator().next();
        assertTrue(nodeInstance instanceof ForEachNodeInstance);

        if (isPersistence()) {
            // when persistence is used there is slightly different behaviour of ContextNodeInstance
            // it's already tested by SimplePersistenceBPMNProcessTest.testMultiInstanceLoopCharacteristicsProcessWithORGateway
            nodeInstancesChild = ((ForEachNodeInstance) nodeInstance)
                    .getNodeInstances();
            assertEquals(1, nodeInstancesChild.size());

            Iterator<NodeInstance> childIterator = nodeInstancesChild
                    .iterator();

            assertTrue(childIterator.next() instanceof CompositeContextNodeInstance);

            ksession.getWorkItemManager().completeWorkItem(
                    workItems.get(2).getId(), null);
            ksession.getWorkItemManager().completeWorkItem(
                    workItems.get(3).getId(), null);

            assertProcessInstanceFinished(processInstance, ksession);
        } else {
            nodeInstancesChild = ((ForEachNodeInstance) nodeInstance)
                    .getNodeInstances();
            assertEquals(2, nodeInstancesChild.size());

            Iterator<NodeInstance> childIterator = nodeInstancesChild
                    .iterator();

            assertTrue(childIterator.next() instanceof CompositeContextNodeInstance);
            assertTrue(childIterator.next() instanceof ForEachJoinNodeInstance);

            ksession.getWorkItemManager().completeWorkItem(
                    workItems.get(2).getId(), null);
            ksession.getWorkItemManager().completeWorkItem(
                    workItems.get(3).getId(), null);

            assertProcessInstanceFinished(processInstance, ksession);
        }

    }
}
