package org.jbpm.process;

import static org.jbpm.process.test.NodeCreator.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.drools.core.process.core.Work;
import org.drools.core.process.core.datatype.impl.type.ObjectDataType;
import org.drools.core.process.core.impl.WorkImpl;
import org.jbpm.process.core.context.variable.Variable;
import org.jbpm.process.instance.impl.Action;
import org.jbpm.process.test.NodeCreator;
import org.jbpm.process.test.Person;
import org.jbpm.process.test.TestProcessEventListener;
import org.jbpm.process.test.TestWorkItemHandler;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.test.util.AbstractBaseTest;
import org.jbpm.workflow.core.DroolsAction;
import org.jbpm.workflow.core.Node;
import org.jbpm.workflow.core.impl.ConnectionImpl;
import org.jbpm.workflow.core.impl.DroolsConsequenceAction;
import org.jbpm.workflow.core.node.ActionNode;
import org.jbpm.workflow.core.node.CatchLinkNode;
import org.jbpm.workflow.core.node.DynamicNode;
import org.jbpm.workflow.core.node.EndNode;
import org.jbpm.workflow.core.node.HumanTaskNode;
import org.jbpm.workflow.core.node.Join;
import org.jbpm.workflow.core.node.Split;
import org.jbpm.workflow.core.node.StartNode;
import org.jbpm.workflow.core.node.ThrowLinkNode;
import org.jbpm.workflow.core.node.WorkItemNode;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessContext;
import org.kie.api.runtime.process.ProcessInstance;
import org.slf4j.LoggerFactory;

/**
 * - [x] ActionNodeInstance
 * - [x]StartNodeInstance
 * - [x] EndNodeInstance
 *
 * - [x] SplitInstance
 * - [x] JoinInstance
 *
 * - [x] ThrowLinkNodeInstance
 * - [x] CatchLinkNodeInstance
 *
 * - FaultNodeInstance
 *
 * - ExtendedNodeInstanceImpl
 *
 *   - EventNodeInstance
 *     - AsyncEventNodeInstance
 *     - BoundaryEventNodeInstance
 *
 *   - StateBasedNodeInstance
 *     - CompositeNodeInstance
 *       - CompositeNodeStartInstance
 *       - CompositeNodeEndInstance
 *       - CompositeContextNodeInstance
 *
 *         - DynamicNodeInstance
 *         - EventSubProcessNodeInstance
 *
 *         - ForEachNodeInstance
 *           - ForEachJoinNodeInstance
 *           - ForEachSplitNodeInstance
 *         - StateNodeInstance
 *
 *       - MilestoneNodeInstance
 *       - RuleSetNodeInstance
 *       - SubProcessNodeInstance
 *       - TimerNodeInstance
 *
 *       - WorkItemNodeInstance
 *         - HumanTaskNodeInstance
 */

@RunWith(Parameterized.class)
public class SimpleFlowTest extends AbstractBaseTest  {

    public void addLogger() {
        logger = LoggerFactory.getLogger(this.getClass());
    }

    @Parameters(name="{0}")
    public static Collection<Object[]> useStack() {
        Object[][] execModelType = new Object[][] {
                { OLD_RECURSIVE_STACK },
                { QUEUE_BASED_EXECUTION }
                };
        return Arrays.asList(execModelType);
    };

    public SimpleFlowTest(String execModel) {
        this.queueBasedExecution = QUEUE_BASED_EXECUTION.equals(execModel);
    }

    @Rule
    public TestName testName = new TestName();

    @Before
    public void printTestName() {
        // DBG
       System.out.println( " " + testName.getMethodName() );
    }

    @Test
    public void testStartActionEnd() {
        String processId = "org.jbpm.core.process.simple";

        RuleFlowProcess process = new RuleFlowProcess();
        process.setId(processId);
        process.setName("Start-Action-End Process");

        List<Variable> variables = new ArrayList<Variable>();
        Variable variable = new Variable();
        variable.setName("var");
        ObjectDataType personDataType = new ObjectDataType();
        personDataType.setClassName(Person.class.getSimpleName());
        variable.setType(personDataType);
        variables.add(variable);
        process.getVariableScope().setVariables(variables);

        StartNode startNode = new StartNode();
        startNode.setName("Start");
        startNode.setId(1);
        process.addNode(startNode);

        final List<String> myList = new ArrayList<String>();
        ActionNode actionNode = new ActionNode();
        actionNode.setName("Print");
        DroolsAction action = new DroolsConsequenceAction("java", null);
        action.setMetaData("Action", new Action() {
            public void execute(ProcessContext context) throws Exception {
                Object eventObj = context.getVariable("var");
                Person person = (Person) eventObj;
                myList.add(person.getName());
            }
        });
        actionNode.setAction(action);
        actionNode.setId(4);
        process.addNode(actionNode);
        new ConnectionImpl(
            startNode, Node.CONNECTION_DEFAULT_TYPE,
            actionNode, Node.CONNECTION_DEFAULT_TYPE
        );

        EndNode endNode = new EndNode();
        endNode.setName("EndNode");
        endNode.setId(6);
        process.addNode(endNode);
        new ConnectionImpl(
            actionNode, Node.CONNECTION_DEFAULT_TYPE,
            endNode, Node.CONNECTION_DEFAULT_TYPE
        );

        KieSession ksession = createKieSession(process);
        TestProcessEventListener procEventListener = new TestProcessEventListener();
        ksession.addEventListener(procEventListener);

        String name = UUID.randomUUID().toString();
        Person jack = new Person();
        jack.setName(name);
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("var", jack);

        ProcessInstance processInstance = ksession.startProcess(processId, parameters);
        assertEquals(1, myList.size());
        assertEquals( name, myList.get(0));
        assertEquals(ProcessInstance.STATE_COMPLETED, processInstance.getState());
    }

    /**
     * queueBased event order:
     *
     * With the split, we get
     * - before node trigger
     * - before node leave and after node leave per branch
     * - after node trigger
     *
     * With the join, we get:
     * - before node trigger and after node trigger per incoming branch
     * - before node leave and after leave when actually continuing
     */
    @Test
    public void testSplitJoin() {
        String processId = "org.jbpm.core.process.split";
        final List<Long> myList = new ArrayList<Long>();

        RuleFlowProcess process = new RuleFlowProcess();
        process.setId(processId);
        process.setName("Start-Split-Join-End Process");

        StartNode startNode = new StartNode();
        startNode.setName("Start");
        startNode.setId(1);
        process.addNode(startNode);

        Split splitNode = new Split(Split.TYPE_AND);
        splitNode.setName("Split-And");
        splitNode.setId(2);
        process.addNode(splitNode);
        new ConnectionImpl(
            startNode, Node.CONNECTION_DEFAULT_TYPE,
            splitNode, Node.CONNECTION_DEFAULT_TYPE
        );

        DroolsAction action = new DroolsConsequenceAction("java", null);
        action.setMetaData("Action", new Action() {
            public void execute(ProcessContext context) throws Exception {
                myList.add(context.getNodeInstance().getNode().getId());
            }
        });

        Join joinNode = new Join();
        joinNode.setType(Join.TYPE_AND);
        joinNode.setName("Join-And");
        joinNode.setId(5);
        process.addNode(joinNode);

        {
            ActionNode actionNode = new ActionNode();
            actionNode.setName("Split-And-1");
            actionNode.setAction(action);
            actionNode.setId(3);
            process.addNode(actionNode);
            new ConnectionImpl(
                    splitNode, Node.CONNECTION_DEFAULT_TYPE,
                    actionNode, Node.CONNECTION_DEFAULT_TYPE
                    );

            new ConnectionImpl(
                    actionNode, Node.CONNECTION_DEFAULT_TYPE,
                    joinNode, Node.CONNECTION_DEFAULT_TYPE
                    );
        }

        {
            ActionNode actionNode = new ActionNode();
            actionNode.setName("Split-And-2");
            actionNode.setAction(action);
            actionNode.setId(4);
            process.addNode(actionNode);
            new ConnectionImpl(
                    splitNode, Node.CONNECTION_DEFAULT_TYPE,
                    actionNode, Node.CONNECTION_DEFAULT_TYPE
                    );

            new ConnectionImpl(
                    actionNode, Node.CONNECTION_DEFAULT_TYPE,
                    joinNode, Node.CONNECTION_DEFAULT_TYPE
                    );
        }

        EndNode endNode = new EndNode();
        endNode.setName("EndNode");
        endNode.setId(6);
        process.addNode(endNode);
        new ConnectionImpl(
            joinNode, Node.CONNECTION_DEFAULT_TYPE,
            endNode, Node.CONNECTION_DEFAULT_TYPE
        );

        KieSession ksession = createKieSession(process);
        TestProcessEventListener procEventListener = new TestProcessEventListener();
        ksession.addEventListener(procEventListener);

        ProcessInstance processInstance = ksession.startProcess(processId);
        assertEquals(2, myList.size());
        assertTrue( myList.contains(3l) );
        assertTrue( myList.contains(4l) );
        assertEquals(ProcessInstance.STATE_COMPLETED, processInstance.getState());
    }

    @Test
    public void testSplitThreeEnds() {
        String processId = "org.jbpm.core.process.split.ends";
        final List<Long> myList = new ArrayList<Long>();

        RuleFlowProcess process = new RuleFlowProcess();
        process.setId(processId);
        process.setName("Start-Split-Multi-End Process");

        StartNode startNode = new StartNode();
        startNode.setName("Start");
        startNode.setId(1);
        process.addNode(startNode);

        Split splitNode = new Split(Split.TYPE_AND);
        splitNode.setName("Split-And");
        splitNode.setId(2);
        process.addNode(splitNode);
        new ConnectionImpl(
            startNode, Node.CONNECTION_DEFAULT_TYPE,
            splitNode, Node.CONNECTION_DEFAULT_TYPE
        );

        DroolsAction action = new DroolsConsequenceAction("java", null);
        action.setMetaData("Action", new Action() {
            public void execute(ProcessContext context) throws Exception {
                myList.add(context.getNodeInstance().getNode().getId());
            }
        });

        List<Long> actionNodeIds = new ArrayList<Long>(3);
        int n = 3;
        for( int i = 0; i < 3; ++i ) {
            ActionNode actionNode = new ActionNode();
            actionNode.setName("Action-" + (i+1));
            actionNode.setAction(action);
            actionNode.setId(n++);
            actionNodeIds.add(actionNode.getId());
            process.addNode(actionNode);
            new ConnectionImpl(
                    splitNode, Node.CONNECTION_DEFAULT_TYPE,
                    actionNode, Node.CONNECTION_DEFAULT_TYPE
                    );

            EndNode endNode = new EndNode();
            endNode.setName("EndNode-" + (i+1));
            endNode.setId(n++);
            boolean terminate = ( i == 1 );
            endNode.setTerminate(terminate);
            process.addNode(endNode);
            new ConnectionImpl(
                    actionNode, Node.CONNECTION_DEFAULT_TYPE,
                    endNode, Node.CONNECTION_DEFAULT_TYPE
                    );
        }

        KieSession ksession = createKieSession(process);
        TestProcessEventListener procEventListener = new TestProcessEventListener();
        procEventListener.useNodeName();
        ksession.addEventListener(procEventListener);

        ProcessInstance processInstance = ksession.startProcess(processId);
        assertEquals(ProcessInstance.STATE_COMPLETED, processInstance.getState());
        assertEquals(2, myList.size());
        actionNodeIds.remove(7l);
        for( long id : actionNodeIds ) {
            assertTrue( "Expected list var to contain action node id " + id, myList.contains(id) );
        }
    }

    @Test
    public void testLinkNode() throws Exception {
        String processId = "org.jbpm.core.process.link";
        RuleFlowProcess process = new RuleFlowProcess();
        process.setId(processId);
        process.setName("Start-Link-End Process");

        StartNode startNode = new NodeCreator<>(process, StartNode.class).createNode("start");
        ThrowLinkNode throwNode = new NodeCreator<>(process, ThrowLinkNode.class).createNode("throw");
        connect(startNode, throwNode);
        CatchLinkNode catchNode = new NodeCreator<>(process, CatchLinkNode.class).createNode("catch");
        connect(throwNode, catchNode);
        EndNode endNode = new NodeCreator<>(process, EndNode.class).createNode("end");
        connect(catchNode, endNode);

        KieSession ksession = createKieSession(process);
        TestProcessEventListener procEventListener = new TestProcessEventListener();
        procEventListener.useNodeName();
        ksession.addEventListener(procEventListener);

        ProcessInstance processInstance = ksession.startProcess(processId);
        assertEquals(ProcessInstance.STATE_COMPLETED, processInstance.getState());
    }

    @Test
    public void testDyanmicNode() throws Exception {
        String processId = "org.jbpm.core.process.adhoc";
        RuleFlowProcess process = new RuleFlowProcess();
        process.setId(processId);
        process.setName("Start-Adhoc-End Process");

        List<Long> myList = new ArrayList<>();
        DroolsAction action = new DroolsConsequenceAction("java", null);
        action.setMetaData("Action", new Action() {
            public void execute(ProcessContext context) throws Exception {
                myList.add(context.getNodeInstance().getNode().getId());
            }
        });


        StartNode startNode = new NodeCreator<>(process, StartNode.class).createNode("start");
        DynamicNode dynaNode = new NodeCreator<>(process, DynamicNode.class).createNode("dyna");
        dynaNode.setAutoComplete(true);
        connect(startNode, dynaNode);
        ActionNode actionNode = new NodeCreator<>(process, ActionNode.class).createNode("action");
        actionNode.setAction(action);
        connect(dynaNode, actionNode);
        EndNode endNode = new NodeCreator<>(process, EndNode.class).createNode("end");
        connect(actionNode, endNode);

        NodeCreator<ActionNode> actionNodeCreator = new NodeCreator<>(dynaNode, ActionNode.class);
        ActionNode singleActionNode = actionNodeCreator.createNode("single");
        singleActionNode.setAction(action);

        actionNode = actionNodeCreator.createNode("before-task");
        actionNode.setAction(action);
        WorkItemNode taskNode = new NodeCreator<>(dynaNode, WorkItemNode.class).createNode("dyna-work");
        connect(actionNode, taskNode);
        Work work = new WorkImpl();
        String workItemName = "work";
        work.setName( workItemName );
        taskNode.setWork(work);

        KieSession ksession = createKieSession(process);
        TestProcessEventListener procEventListener = new TestProcessEventListener();
        procEventListener.useNodeName();
        ksession.addEventListener(procEventListener);

        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler(workItemName, workItemHandler);
        ProcessInstance processInstance = ksession.startProcess(processId);
        assertEquals(ProcessInstance.STATE_ACTIVE, processInstance.getState());

        // trigger dynamic
        ksession.signalEvent("before-task", null, processInstance.getId());
        ksession.getWorkItemManager().completeWorkItem(workItemHandler.getWorkItems().removeLast().getId(), null);

        assertEquals(ProcessInstance.STATE_COMPLETED, processInstance.getState());
        assertEquals( 2, myList.size() );
    }

}
