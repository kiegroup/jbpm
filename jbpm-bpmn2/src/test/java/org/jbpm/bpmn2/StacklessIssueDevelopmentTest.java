package org.jbpm.bpmn2;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.jbpm.bpmn2.objects.Person;
import org.jbpm.bpmn2.objects.TestWorkItemHandler;
import org.jbpm.bpmn2.test.Broken;
import org.jbpm.bpmn2.test.Fixed;
import org.jbpm.process.instance.impl.demo.DoNothingWorkItemHandler;
import org.jbpm.test.util.TestBpmn2ProcessEventListener;
import org.jbpm.workflow.instance.node.DynamicNodeInstance;
import org.jbpm.workflow.instance.node.DynamicUtils;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.model.Statement;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Parameterized.class)
public class StacklessIssueDevelopmentTest extends JbpmBpmn2TestCase {

    private static final boolean persistence = false;

    @Parameters(name="{3}")
    public static Collection<Object[]> persistence() {
        List<Object[]> params = new ArrayList<Object[]>();
        params.add( new Object [] {
                false, // recursive
                false, // persistence
                false, // fixed
                "RECURSIVE"
        });
        if( persistence ) {
            params.add( new Object [] {
                    false, // recursive
                    true,  // persistence
                    false, // fixed
                    "RECURSIVE W/ PERSISTENCE"
            });
        }
        params.add( new Object [] {
                true, // stackless
                true, // persistence
                false, // fixed
                "STACKLESS FIXED"
        });
        params.add( new Object [] {
                true, // stackless
                false, // persistence
                true, //  broken
                "STACKLESS BROKEN"
        });
        if( persistence ) {
            params.add( new Object [] {
                    true, // stackless
                    true, // persistence
                    true, //  broken
                    "STACKLESS BROKEN PERSISTENCE"
            });
        }
        return params;
    };

    private Logger logger = LoggerFactory.getLogger(StacklessIssueDevelopmentTest.class);
    private TestBpmn2ProcessEventListener procEventListener;
    private final boolean broken;

    public StacklessIssueDevelopmentTest(boolean stackless, boolean persistence, boolean broken, String name) {
        super(persistence, false, stackless);
        this.broken = broken;
    }

    @Rule
    public TestRule brokenWatcher = new TestRule() {

        @Override
        public Statement apply( final Statement base, Description description ) {
            final boolean [] testBroken = { false };
            try {
                String methodName = description.getMethodName();
                int i = methodName.indexOf("[");
                if (i > 0) {
                    String type = methodName.substring(i);
                    type = type.substring(1, type.indexOf(']'));
                    System.out.println( "> " + type );

                    methodName = methodName.substring(0, i);
                }
                Method method = description.getTestClass().getMethod(methodName);
                testBroken[0] = (method.getAnnotation(Broken.class) != null);
            } catch (Exception ex) {
                // ignore
            }

            return new Statement() {

                @Override
                public void evaluate() throws Throwable {
                    if( stacklessExecution ) {
                        if( broken ) {
                            Assume.assumeTrue( testBroken[0] );
                        } else {
                            Assume.assumeFalse( testBroken[0] );
                        }
                    }
                    base.evaluate();
                }
            };
        }
    };

    @BeforeClass
    public static void setupPersistence() throws Exception {
        setUpDataSource();
    }

    @Before
    public void setup() throws Exception {

        procEventListener = new TestBpmn2ProcessEventListener();
    }

    private KieSession ksession;

    @After
    public void dispose() {
        if (ksession != null) {
            ksession.dispose();
            ksession = null;
        }
    }

    @Override
    protected StatefulKnowledgeSession createKnowledgeSession(KieBase kbase) throws Exception {
        StatefulKnowledgeSession ksession = super.createKnowledgeSession(kbase);
        ksession.addEventListener(procEventListener);
        return ksession;
    }

    /**
     * [ ] Ad-Hoc
     * [ ] Compensation
     */

    // Tests ---------------------------------------------------------------------------------------------------------------------

    @Test
    @Fixed
    public void testAdHocProcess() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-AdHocProcess.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ProcessInstance processInstance = ksession.startProcess("AdHocProcess");
        assertTrue(processInstance.getState() == ProcessInstance.STATE_ACTIVE);
        ksession = restoreSession(ksession, true);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                new DoNothingWorkItemHandler());
        logger.debug("Triggering node");
        ksession.signalEvent("Task1", null, processInstance.getId());
        assertProcessInstanceActive(processInstance);
        ksession.signalEvent("User1", null, processInstance.getId());
        assertProcessInstanceActive(processInstance);
        ksession.insert(new Person());
        ksession.signalEvent("Task3", null, processInstance.getId());
        assertProcessInstanceFinished(processInstance, ksession);
    }


    @Test
    @Broken
    public void testAdHocProcessDynamicSubProcess() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-AdHocProcess.bpmn2",
                "BPMN2-MinimalProcess.bpmn2");
        ksession = createKnowledgeSession(kbase);

        ProcessInstance processInstance = ksession.startProcess("AdHocProcess");
        assertTrue(processInstance.getState() == ProcessInstance.STATE_ACTIVE);

        ksession = restoreSession(ksession, true);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", new DoNothingWorkItemHandler());
        logger.debug("Triggering node");

        ksession.signalEvent("Task1", null, processInstance.getId());
        assertProcessInstanceActive(processInstance);
        TestWorkItemHandler workItemHandler2 = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("OtherTask", workItemHandler2);

        DynamicUtils.addDynamicSubProcess(processInstance, ksession, "Minimal", new HashMap<String, Object>());

        ksession = restoreSession(ksession, true);
        ksession.signalEvent("User1", null, processInstance.getId());
        assertProcessInstanceActive(processInstance);
        ksession.insert(new Person());
        ksession.signalEvent("Task3", null, processInstance.getId());
        assertProcessInstanceFinished(processInstance, ksession);
    }


    @Test
    @Fixed
    public void testAdHocProcessDynamicTask() throws Exception {

        KieBase kbase = createKnowledgeBase("BPMN2-AdHocProcess.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ProcessInstance processInstance = ksession.startProcess("AdHocProcess");
        assertTrue(processInstance.getState() == ProcessInstance.STATE_ACTIVE);
        ksession = restoreSession(ksession, true);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                new DoNothingWorkItemHandler());
        logger.debug("Triggering node");
        ksession.signalEvent("Task1", null, processInstance.getId());
        assertProcessInstanceActive(processInstance);
        TestWorkItemHandler workItemHandler2 = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("OtherTask",
                workItemHandler2);
        DynamicUtils.addDynamicWorkItem(processInstance, ksession, "OtherTask",
                new HashMap<String, Object>());
        WorkItem workItem = workItemHandler2.getWorkItem();
        assertNotNull(workItem);
        ksession = restoreSession(ksession, true);
        ksession.getWorkItemManager().completeWorkItem(workItem.getId(), null);
        ksession.signalEvent("User1", null, processInstance.getId());
        assertProcessInstanceActive(processInstance);
        ksession.insert(new Person());
        ksession.signalEvent("Task3", null, processInstance.getId());
        assertProcessInstanceFinished(processInstance, ksession);

    }

    @Test
    @Fixed
    public void testAdHocSubProcessAutoCompleteDynamicTask() throws Exception {

        KieBase kbase = createKnowledgeBaseWithoutDumper(
                "BPMN2-AdHocSubProcessAutoComplete.bpmn2",
                "BPMN2-AdHocSubProcess.drl");
        ksession = createKnowledgeSession(kbase);
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);
        TestWorkItemHandler workItemHandler2 = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("OtherTask",
                workItemHandler2);
        ProcessInstance processInstance = ksession
                .startProcess("AdHocSubProcess");
        assertTrue(processInstance.getState() == ProcessInstance.STATE_ACTIVE);
        DynamicNodeInstance dynamicContext = (DynamicNodeInstance) ((WorkflowProcessInstance) processInstance)
                .getNodeInstances().iterator().next();
        DynamicUtils.addDynamicWorkItem(dynamicContext, ksession, "OtherTask",
                new HashMap<String, Object>());
        WorkItem workItem = workItemHandler.getWorkItem();
        assertNull(workItem);
        ksession = restoreSession(ksession, true);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);
        ksession.fireAllRules();
        workItem = workItemHandler.getWorkItem();
        assertNotNull(workItem);
        ksession = restoreSession(ksession, true);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);
        ksession.getWorkItemManager().completeWorkItem(workItem.getId(), null);
        assertProcessInstanceActive(processInstance);
        workItem = workItemHandler2.getWorkItem();
        ksession.getWorkItemManager().completeWorkItem(workItem.getId(), null);
        assertProcessInstanceFinished(processInstance, ksession);
        ksession.dispose();
    }

    @Test
    @Broken
    public void testAdHocSubProcessAutoCompleteDynamicSubProcess() throws Exception {

        KieBase kbase = createKnowledgeBaseWithoutDumper(
                "BPMN2-AdHocSubProcessAutoComplete.bpmn2",
                "BPMN2-AdHocSubProcess.drl", "BPMN2-MinimalProcess.bpmn2");
        ksession = createKnowledgeSession(kbase);
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);
        TestWorkItemHandler workItemHandler2 = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("OtherTask",
                workItemHandler2);
        ProcessInstance processInstance = ksession
                .startProcess("AdHocSubProcess");
        assertTrue(processInstance.getState() == ProcessInstance.STATE_ACTIVE);
        ksession.fireAllRules();
        DynamicNodeInstance dynamicContext = (DynamicNodeInstance) ((WorkflowProcessInstance) processInstance)
                .getNodeInstances().iterator().next();
        DynamicUtils.addDynamicSubProcess(dynamicContext, ksession, "Minimal",
                new HashMap<String, Object>());
        ksession = restoreSession(ksession, true);
        WorkItem workItem = workItemHandler.getWorkItem();
        assertNotNull(workItem);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);
        ksession.getWorkItemManager().completeWorkItem(workItem.getId(), null);
        // assertProcessInstanceActive(processInstance.getId(), ksession);
        // workItem = workItemHandler2.getWorkItem();
        // ksession.getWorkItemManager().completeWorkItem(workItem.getId(), null);
        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    @Fixed
    public void testAdHocSubProcessAutoCompleteDynamicSubProcess2() throws Exception {
        procEventListener.useNodeInstanceUniqueId();

        KieBase kbase = createKnowledgeBaseWithoutDumper(
                "BPMN2-AdHocSubProcessAutoComplete.bpmn2", // AdHocSubprocess
                "BPMN2-AdHocSubProcess.drl",
                "BPMN2-ServiceProcess.bpmn2");  // ServiceProcess
        ksession = createKnowledgeSession(kbase);

        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);
        TestWorkItemHandler workItemHandler2 = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Service Task", workItemHandler2);

        ProcessInstance processInstance = ksession.startProcess("AdHocSubProcess");
        assertTrue(processInstance.getState() == ProcessInstance.STATE_ACTIVE);

        ksession.fireAllRules();

        DynamicNodeInstance dynamicContext = (DynamicNodeInstance) ((WorkflowProcessInstance) processInstance).getNodeInstances().iterator().next();
        DynamicUtils.addDynamicSubProcess(dynamicContext, ksession, "ServiceProcess", new HashMap<String, Object>());

        ksession = restoreSession(ksession, true);
        WorkItem workItem = workItemHandler.getWorkItem();
        assertNotNull(workItem);

        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);
        ksession.getWorkItemManager().completeWorkItem(workItem.getId(), null);
        assertProcessInstanceActive(processInstance);

        workItem = workItemHandler2.getWorkItem();
        ksession.getWorkItemManager().completeWorkItem(workItem.getId(), null);
        assertProcessInstanceFinished(processInstance, ksession);
    }

}