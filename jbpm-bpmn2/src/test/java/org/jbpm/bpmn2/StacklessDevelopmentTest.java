package org.jbpm.bpmn2;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jbpm.bpmn2.objects.MyError;
import org.jbpm.bpmn2.objects.Person;
import org.jbpm.bpmn2.objects.TestWorkItemHandler;
import org.jbpm.bpmn2.test.Broken;
import org.jbpm.bpmn2.test.Fixed;
import org.jbpm.bpmn2.test.RequiresPersistence;
import org.jbpm.bpmn2.test.RequiresQueueBased;
import org.jbpm.process.core.context.exception.CompensationScope;
import org.jbpm.process.instance.impl.demo.DoNothingWorkItemHandler;
import org.jbpm.process.instance.impl.demo.SystemOutWorkItemHandler;
import org.jbpm.test.util.CountDownProcessEventListener;
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
import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.process.ProcessNodeLeftEvent;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Parameterized.class)
public class StacklessDevelopmentTest extends JbpmBpmn2TestCase {

    @Parameters(name="{3}")
    public static Collection<Object[]> persistence() {
        List<Object[]> params = new ArrayList<Object[]>();
        params.add( new Object [] {
                false, // recursive
                false, // persistence
                false, // fixed
                "RECURSIVE"
        });
        params.add( new Object [] {
                false, // recursive
                true,  // persistence
                false, // fixed
                "RECURSIVE W/ PERSISTENCE"
        });
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
        return params;
    };

    private Logger logger = LoggerFactory.getLogger(StacklessDevelopmentTest.class);
    private TestBpmn2ProcessEventListener procEventListener;
    private final boolean broken;

    public StacklessDevelopmentTest(boolean stackless, boolean persistence, boolean broken, String name) {
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


    /**
     * [ ] Last event stuff (below)
     * [ ] Ad-Hoc
     * [ ] Compensation
     */
    // Tests ---------------------------------------------------------------------------------------------------------------------

    @Test
    @RequiresQueueBased
    @Fixed
    public void brokenTestsAreAlsoHere() {
        Reflections reflections = new Reflections(
                ClasspathHelper.forPackage("org.jbpm"),
                new MethodAnnotationsScanner(),
                new FieldAnnotationsScanner(), new SubTypesScanner());

        Set<String> brokenTests = new HashSet<>();
        for( Method brokenTest : reflections.getMethodsAnnotatedWith(Broken.class) ) {
            System.out.println( brokenTest.getDeclaringClass().getSimpleName() + "." + brokenTest.getName() );
            brokenTests.add(brokenTest.getName());
        }

        Set<String> fixedTests = new HashSet<>();
        for( Method fixedTest : reflections.getMethodsAnnotatedWith(Fixed.class) ) {
            fixedTests.add(fixedTest.getName());
        }

        Set<String> stacklessTests = new HashSet<>();
        Set<String> stacklessFixed = new HashSet<>();
        for( Method test : this.getClass().getDeclaredMethods() ) {
            String testName = test.getName();
           if( test.getAnnotation(Test.class) != null ) {
               if( test.getAnnotation(Broken.class) != null ) {
                   stacklessTests.add(testName);
               } else if( test.getAnnotation(Fixed.class) != null ) {
                   stacklessFixed.add(testName);
                   assertTrue( this.getClass().getSimpleName() + "." + testName + " is fixed?", fixedTests.contains(testName) );
               } else {
                   fail( this.getClass().getSimpleName() + "." + testName + " should be broken OR fixed!" );
               }
           }
        }

        for( String brokenTest : brokenTests ) {
            assertTrue( this.getClass().getSimpleName() + " does not contain " + brokenTest + "!",
                      stacklessTests.contains(brokenTest) );
        }

        for( String fixedTest : fixedTests ) {
            assertFalse( this.getClass().getSimpleName() + "." + fixedTest + " should be set to fixed?",
                    stacklessTests.contains(fixedTest) );
        }
    }

    protected void _ESCALATIONS_AND_EVENTS() { }

    @Test
    @Fixed(times=1)
    public void testThrowEndSignalWithScope() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-EndThrowEventScope.bpmn2");
        ksession = createKnowledgeSession(kbase);

        TestWorkItemHandler handler = new TestWorkItemHandler();

        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", handler);
        Map<String, Object> params = new HashMap<String, Object>();

        ProcessInstance processInstance = ksession.startProcess("end-event-scope", params);
        ProcessInstance processInstance2 = ksession.startProcess("end-event-scope", params);

        assertProcessInstanceActive(processInstance);
        assertProcessInstanceActive(processInstance2);

        assertNodeActive(processInstance.getId(), ksession, "Complete work", "Wait");
        assertNodeActive(processInstance2.getId(), ksession, "Complete work", "Wait");

        List<WorkItem> items = handler.getWorkItems();

        WorkItem wi = items.get(0);

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("_output", "sending event");

        ksession.getWorkItemManager().completeWorkItem(wi.getId(), result);

        assertProcessInstanceCompleted(processInstance);
        assertProcessInstanceActive(processInstance2);
        assertNodeActive(processInstance2.getId(), ksession, "Complete work", "Wait");

        wi = items.get(1);
        ksession.getWorkItemManager().completeWorkItem(wi.getId(), result);
        assertProcessInstanceCompleted(processInstance2);
    }

    @Test
    @Fixed(times=1)
    public void testEscalationBoundaryEventAndIntermediate() throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("escalation/BPMN2-EscalationWithDataMapping.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ksession.addEventListener(procEventListener);
        Map<String, Object> sessionArgs = new HashMap<String, Object>();
        sessionArgs.put("Property_2", new java.lang.RuntimeException());
        ProcessInstance processInstance = ksession.startProcess("BPMN2BoundaryEscalationEventOnTask", sessionArgs);
        assertProcessInstanceCompleted(processInstance);
        assertProcessVarValue(processInstance, "Property_3", "java.lang.RuntimeException");
    }

    protected void _ERRORS() { }

    @Test
    @Fixed(times=1)
    public void testErrorBoundaryEvent() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-ErrorBoundaryEventInterrupting.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ksession.getWorkItemManager().registerWorkItemHandler("MyTask",
                new DoNothingWorkItemHandler());
        ProcessInstance processInstance = ksession
                .startProcess("ErrorBoundaryEvent");
        assertProcessInstanceFinished(processInstance, ksession);

    }

    @Test
    @Fixed(times=1)
    public void testCallActivityWithBoundaryErrorEventWithWaitState() throws Exception {
        KieBase kbase = createKnowledgeBase(
                "BPMN2-CallActivityProcessBoundaryError.bpmn2",
                "BPMN2-CallActivitySubProcessBoundaryError.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ksession.addEventListener(procEventListener);
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("task1", workItemHandler);
        ProcessInstance processInstance = ksession.startProcess("ParentProcess");

        System.out.println("| Getting work item 1");
        WorkItem workItem = workItemHandler.getWorkItem();
        assertNotNull("Null work item!", workItem);
        ksession.getWorkItemManager().completeWorkItem(workItem.getId(), null);

        System.out.println("| Getting work item 2");
        workItem = workItemHandler.getWorkItem();
        assertNotNull("Another null work item!",workItem);
        ksession.getWorkItemManager().completeWorkItem(workItem.getId(), null);

        assertProcessInstanceFinished(processInstance, ksession);
        assertNodeTriggered(processInstance.getId(), "StartProcess",
                "Call Activity 1", "Boundary event", "Task Parent", "End2");
        // then check child process executed nodes - is there better way to get child process id than simply increment?
        assertNodeTriggered(processInstance.getId() + 1, "StartProcess", "Task 1", "End");
    }

    @Test
    @Fixed(times=1)
    public void testCallActivityWithBoundaryErrorEvent() throws Exception {
        KieBase kbase = createKnowledgeBase(
                "BPMN2-CallActivityProcessBoundaryError.bpmn2",
                "BPMN2-CallActivitySubProcessBoundaryError.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ksession.addEventListener(procEventListener);
        ksession.getWorkItemManager().registerWorkItemHandler("task1",
                new SystemOutWorkItemHandler());
        ProcessInstance processInstance = ksession.startProcess("ParentProcess");

        assertProcessInstanceFinished(processInstance, ksession);
        assertNodeTriggered(processInstance.getId(), "StartProcess",
                "Call Activity 1", "Boundary event", "Task Parent", "End2");
        // then check child process executed nodes - is there better way to get child process id than simply increment?
        assertNodeTriggered(processInstance.getId() + 1, "StartProcess", "Task 1", "End");
    }

    @Test
    @Fixed(times=1)
    public void testErrorBoundaryEventOnEntry() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-BoundaryErrorEventCatchingOnEntryException.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ksession.addEventListener(procEventListener);
        TestWorkItemHandler handler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",handler);

        ProcessInstance processInstance = ksession
            .startProcess("BoundaryErrorEventOnEntry");
        assertProcessInstanceActive(processInstance.getId(), ksession);
        assertEquals(1, handler.getWorkItems().size());
    }

    @Test
    @Fixed(times=1)
    public void testErrorBoundaryEventOnExit() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-BoundaryErrorEventCatchingOnExitException.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ksession.addEventListener(new TestBpmn2ProcessEventListener());
        TestWorkItemHandler handler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",handler);

        ProcessInstance processInstance = ksession.startProcess("BoundaryErrorEventOnExit");
        assertProcessInstanceActive(processInstance.getId(), ksession);
        WorkItem workItem = handler.getWorkItem();
        ksession.getWorkItemManager().completeWorkItem(workItem.getId(), null);

        assertEquals(1, handler.getWorkItems().size());
    }

    @Test
    @Broken
    public void testCatchErrorBoundaryEventOnTask() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-ErrorBoundaryEventOnTask.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ksession.addEventListener(procEventListener);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", new TestWorkItemHandler() {

            @Override
            public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
                if (workItem.getParameter("ActorId").equals("mary")) { // User Task error attached
                    throw new MyError();
                }
            }

            @Override
            public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
                manager.abortWorkItem(workItem.getId());
            }


        });
        ProcessInstance processInstance = ksession
                .startProcess("BPMN2-ErrorBoundaryEventOnTask");

        assertProcessInstanceActive(processInstance);
        assertNodeTriggered(processInstance.getId(), "start", "split", "User Task", "User task error attached",
                "Script Task", "error2", "error1");

    }

    @Test(timeout=10000)
    @RequiresPersistence(false)
    @Fixed(times=1)
    public void testTimerBoundaryEventCycleISO() throws Exception {
        CountDownProcessEventListener countDownListener = new CountDownProcessEventListener("TimerEvent", 2);
        KieBase kbase = createKnowledgeBase("BPMN2-TimerBoundaryEventCycleISO.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ksession.addEventListener(countDownListener);
        ksession.getWorkItemManager().registerWorkItemHandler("MyTask", new DoNothingWorkItemHandler());
        ProcessInstance processInstance = ksession.startProcess("TimerBoundaryEvent");
        assertProcessInstanceActive(processInstance);
        countDownListener.waitTillCompleted();
        assertProcessInstanceActive(processInstance);
        ksession.abortProcessInstance(processInstance.getId());
    }

    protected void _SUB_PROCESSES() { }

    @Test
    @Fixed(times=1)
    public void testEventSubprocessTimer() throws Exception {
        CountDownProcessEventListener countDownListener = new CountDownProcessEventListener("Script Task 1", 1);

        KieBase kbase = createKnowledgeBase("BPMN2-EventSubprocessTimer.bpmn2");

        ksession = createKnowledgeSession(kbase);
        ksession.addEventListener(countDownListener);
        ksession.addEventListener(procEventListener);
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);
        ProcessInstance processInstance = ksession
                .startProcess("BPMN2-EventSubprocessTimer");
        assertProcessInstanceActive(processInstance);
        countDownListener.waitTillCompleted();

        WorkItem workItem = workItemHandler.getWorkItem();
        assertNotNull(workItem);
        ksession.getWorkItemManager().completeWorkItem(workItem.getId(), null);
        assertProcessInstanceFinished(processInstance, ksession);
        assertNodeTriggered(processInstance.getId(), "start", "User Task 1",
                "end", "Sub Process 1", "start-sub", "Script Task 1", "end-sub");

    }

    @Test
    @Fixed(times=1)
    public void testEventSubprocessError() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-EventSubprocessError.bpmn2");
        final List<Long> executednodes = new ArrayList<Long>();
        ProcessEventListener listener = new DefaultProcessEventListener() {

            @Override
            public void afterNodeLeft(ProcessNodeLeftEvent event) {
                if (event.getNodeInstance().getNodeName()
                        .equals("Script Task 1")) {
                    executednodes.add(event.getNodeInstance().getId());
                }
            }

        };
        ksession = createKnowledgeSession(kbase);
        ksession.addEventListener(procEventListener);
        ksession.addEventListener(listener);
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);
        ProcessInstance processInstance = ksession.startProcess("BPMN2-EventSubprocessError");
        assertProcessInstanceActive(processInstance);

        ksession = restoreSession(ksession, true);
        ksession.addEventListener(listener);
        ksession.addEventListener(procEventListener);

        WorkItem workItem = workItemHandler.getWorkItem();
        assertNotNull(workItem);
        ksession.getWorkItemManager().completeWorkItem(workItem.getId(), null);
        assertProcessInstanceFinished(processInstance, ksession);
        assertNodeTriggered(processInstance.getId(), "start", "User Task 1",
                "end", "Sub Process 1", "start-sub", "Script Task 1", "end-sub");
        assertEquals(1, executednodes.size());

    }

    @Test
    @Broken
    @RequiresQueueBased
    public void testEventSubprocessTaskError() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-EventSubprocessErrorWithTask.bpmn2");
        final List<Long> executednodes = new ArrayList<Long>();
        ProcessEventListener listener = new DefaultProcessEventListener() {

            @Override
            public void afterNodeLeft(ProcessNodeLeftEvent event) {
                if (event.getNodeInstance().getNodeName()
                        .equals("Script Task 1")) {
                    executednodes.add(event.getNodeInstance().getId());
                }
            }

        };
        ksession = createKnowledgeSession(kbase);
        ksession.addEventListener(procEventListener);
        ksession.addEventListener(listener);
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);
        ProcessInstance processInstance = ksession.startProcess("BPMN2-EventSubprocessErrorWithTask");
        assertProcessInstanceActive(processInstance);
        ksession = restoreSession(ksession, true);
        ksession.addEventListener(listener);

        WorkItem workItem = workItemHandler.getWorkItem();
        assertNotNull(workItem);
        ksession.getWorkItemManager().completeWorkItem(workItem.getId(), null);

        workItem = workItemHandler.getWorkItem();
        assertNotNull(workItem);
        ksession.getWorkItemManager().completeWorkItem(workItem.getId(), null);

        assertProcessInstanceFinished(processInstance, ksession);

    }

    @Test
    @Fixed(times=1)
    public void testErrorBetweenProcessesProcess() throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("subprocess/ErrorsBetweenProcess-Process.bpmn2",
                "subprocess/ErrorsBetweenProcess-SubProcess.bpmn2");
        ksession = createKnowledgeSession(kbase);

        Map<String, Object> variables = new HashMap<String, Object>();

        variables.put("tipoEvento", "error");
        variables.put("pasoVariable", 3);
        ProcessInstance processInstance = ksession.startProcess("Principal", variables);

        assertProcessInstanceCompleted(processInstance.getId(), ksession);
        assertProcessInstanceAborted(processInstance.getId()+1, ksession);

        assertProcessVarValue(processInstance, "event", "error desde Subproceso");
    }

    @Test
    @Fixed(times=1)
    public void testSubProcessTask() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-SubProcessUserTask.bpmn2");
        KieSession ksession = createKnowledgeSession(kbase);

        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);

        ProcessInstance processInstance = ksession.startProcess("SubProcess");
        assertProcessInstanceActive(processInstance);

        ksession = restoreSession(ksession, true);
        WorkItem workItem = workItemHandler.getWorkItem();
        ksession.getWorkItemManager().completeWorkItem(workItem.getId(), null);
    }

    protected void _AD_HOC() { }

    @Test
    @Broken
    public void testAdHocProcessDynamicSubProcess() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-AdHocProcess.bpmn2",
                "BPMN2-MinimalProcess.bpmn2");
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
        DynamicUtils.addDynamicSubProcess(processInstance, ksession, "Minimal",
                new HashMap<String, Object>());
        ksession = restoreSession(ksession, true);
        ksession.signalEvent("User1", null, processInstance.getId());
        assertProcessInstanceActive(processInstance);
        ksession.insert(new Person());
        ksession.signalEvent("Task3", null, processInstance.getId());
        assertProcessInstanceFinished(processInstance, ksession);
    }

    @Test
    @Broken
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
    @Broken
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
    public void testAdHocSubProcessAutoCompleteDynamicSubProcess()
            throws Exception {
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
    @Broken
    public void testAdHocSubProcessAutoCompleteDynamicSubProcess2()
            throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper(
                "BPMN2-AdHocSubProcessAutoComplete.bpmn2",
                "BPMN2-AdHocSubProcess.drl", "BPMN2-ServiceProcess.bpmn2");
        ksession = createKnowledgeSession(kbase);
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);
        TestWorkItemHandler workItemHandler2 = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Service Task",
                workItemHandler2);
        ProcessInstance processInstance = ksession
                .startProcess("AdHocSubProcess");
        assertTrue(processInstance.getState() == ProcessInstance.STATE_ACTIVE);
        ksession.fireAllRules();
        DynamicNodeInstance dynamicContext = (DynamicNodeInstance) ((WorkflowProcessInstance) processInstance)
                .getNodeInstances().iterator().next();
        DynamicUtils.addDynamicSubProcess(dynamicContext, ksession,
                "ServiceProcess", new HashMap<String, Object>());
        ksession = restoreSession(ksession, true);
        WorkItem workItem = workItemHandler.getWorkItem();
        assertNotNull(workItem);
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);
        ksession.getWorkItemManager().completeWorkItem(workItem.getId(), null);
        assertProcessInstanceActive(processInstance);
        workItem = workItemHandler2.getWorkItem();
        ksession.getWorkItemManager().completeWorkItem(workItem.getId(), null);
        assertProcessInstanceFinished(processInstance, ksession);
    }

    protected void  _MULTI_INSTANCE() { }

    @Test
    @Broken
    public void testCallActivityMI() throws Exception {
        KieBase kbase = createKnowledgeBaseWithoutDumper("BPMN2-CallActivityMI.bpmn2",
                "BPMN2-CallActivitySubProcess.bpmn2");
        ksession = createKnowledgeSession(kbase);
        final List<Long> subprocessStarted = new ArrayList<Long>();
        ksession.addEventListener(new DefaultProcessEventListener() {

            @Override
            public void beforeProcessStarted(ProcessStartedEvent event) {
                if (event.getProcessInstance().getProcessId().equals("SubProcess")) {
                    subprocessStarted.add(event.getProcessInstance().getId());
                }
            }

        });

        List<String> list = new ArrayList<String>();
        list.add("first");
        list.add("second");
        List<String> listOut = new ArrayList<String>();

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", "oldValue");
        params.put("list", list);
        params.put("listOut", listOut);

        ProcessInstance processInstance = ksession.startProcess("ParentProcess", params);
        assertProcessInstanceCompleted(processInstance);

        assertEquals(2, subprocessStarted.size());
        listOut = (List)((WorkflowProcessInstance) processInstance).getVariable("listOut");
        assertNotNull(listOut);
        assertEquals(2, listOut.size());

        assertEquals("new value", listOut.get(0));
        assertEquals("new value", listOut.get(1));
    }


    @Test
    @Broken
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
    @Broken
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
    @Broken
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

    protected void  _COMPENSATION() { }

    @Test
    @Broken
    public void compensationViaIntermediateThrowEventProcess() throws Exception {
        KieSession ksession = createKnowledgeSession("compensation/BPMN2-Compensation-IntermediateThrowEvent.bpmn2");
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", "0");
        ProcessInstance processInstance = ksession.startProcess("CompensateIntermediateThrowEvent", params);

        ksession.getWorkItemManager().completeWorkItem(workItemHandler.getWorkItem().getId(), null);

        // compensation activity (assoc. with script task) signaled *after* script task
        assertProcessInstanceCompleted(processInstance.getId(), ksession);
        assertProcessVarValue(processInstance, "x", "1" );
    }

    @Test
    @Broken
    public void compensationTwiceViaSignal() throws Exception {
        KieSession ksession = createKnowledgeSession("compensation/BPMN2-Compensation-IntermediateThrowEvent.bpmn2");
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", "0");
        String processId = "CompensateIntermediateThrowEvent";
        ProcessInstance processInstance = ksession.startProcess(processId, params);

        // twice
        ksession.signalEvent("Compensation", CompensationScope.IMPLICIT_COMPENSATION_PREFIX + processId, processInstance.getId());
        ksession.getWorkItemManager().completeWorkItem(workItemHandler.getWorkItem().getId(), null);

        // compensation activity (assoc. with script task) signaled *after* script task
        assertProcessInstanceCompleted(processInstance.getId(), ksession);
        assertProcessVarValue(processInstance, "x", "2");
    }

    @Test
    @Fixed(times=1)
    public void compensationViaEventSubProcess() throws Exception {
        KieSession ksession = createKnowledgeSession("compensation/BPMN2-Compensation-EventSubProcess.bpmn2");
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", "0");
        ProcessInstance processInstance = ksession.startProcess("CompensationEventSubProcess", params);

        assertProcessInstanceActive(processInstance.getId(), ksession);
        ksession.getWorkItemManager().completeWorkItem(workItemHandler.getWorkItem().getId(), null);

        assertProcessVarValue(processInstance, "x", "1");
    }

    @Test
    @Broken
    public void compensationOnlyAfterAssociatedActivityHasCompleted() throws Exception {
        KieSession ksession = createKnowledgeSession("compensation/BPMN2-Compensation-UserTaskBeforeAssociatedActivity.bpmn2");
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", "0");
        ProcessInstance processInstance = ksession.startProcess("CompensateIntermediateThrowEvent", params);

        // should NOT cause compensation since compensated activity has not yet completed (or started)!
        ksession.signalEvent("Compensation", "_3", processInstance.getId());

        // user task -> script task (associated with compensation) --> intermeidate throw compensation event
        ksession.getWorkItemManager().completeWorkItem(workItemHandler.getWorkItem().getId(), null);

        // compensation activity (assoc. with script task) signaled *after* to-compensate script task
        assertProcessInstanceCompleted(processInstance.getId(), ksession);
        assertProcessVarValue(processInstance, "x", "1");
    }

    @Test
    @Broken
    public void orderedCompensation() throws Exception {
        KieSession ksession = createKnowledgeSession("compensation/BPMN2-Compensation-ParallelOrderedCompensation-IntermediateThrowEvent.bpmn2");
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", "");
        ProcessInstance processInstance = ksession.startProcess("CompensateParallelOrdered", params);
        List<WorkItem> workItems = workItemHandler.getWorkItems();
        List<Long> workItemIds = new ArrayList<Long>();
        for( WorkItem workItem : workItems ) {
           if( "Thr".equals(workItem.getParameter("NodeName")) )  {
               workItemIds.add(workItem.getId());
           }
        }
        for( WorkItem workItem : workItems ) {
           if( "Two".equals(workItem.getParameter("NodeName")) )  {
               workItemIds.add(workItem.getId());
           }
        }
        for( WorkItem workItem : workItems ) {
           if( "One".equals(workItem.getParameter("NodeName")) )  {
               workItemIds.add(workItem.getId());
           }
        }
        for( Long id : workItemIds ) {
            ksession.getWorkItemManager().completeWorkItem(id, null);
        }

        // user task -> script task (associated with compensation) --> intermeidate throw compensation event
        String xVal = getProcessVarValue(processInstance, "x");
        // Compensation happens in the *REVERSE* order of completion
        // Ex: if the order is 3, 17, 282, then compensation should happen in the order of 282, 17, 3
        assertEquals("Compensation did not fire in the same order as the associated activities completed.", "_171:_131:_141:_151:", xVal );
    }

    @Test
    @Broken
    public void compensationInSubSubProcesses() throws Exception {
        KieSession ksession = createKnowledgeSession("compensation/BPMN2-Compensation-InSubSubProcess.bpmn2");
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", "0");
        ProcessInstance processInstance = ksession.startProcess("CompensateSubSubSub", params);

        ksession.signalEvent("Compensation", "_C-2", processInstance.getId());

        ksession.getWorkItemManager().completeWorkItem(workItemHandler.getWorkItem().getId(), null);
        ksession.getWorkItemManager().completeWorkItem(workItemHandler.getWorkItem().getId(), null);
        ksession.getWorkItemManager().completeWorkItem(workItemHandler.getWorkItem().getId(), null);

        // compensation activity (assoc. with script task) signaled *after* script task
        assertProcessInstanceCompleted(processInstance.getId(), ksession);
        assertProcessVarValue(processInstance, "x", "2");
    }

    @Test
    @Broken
    public void specificCompensationOfASubProcess() throws Exception {
        KieSession ksession = createKnowledgeSession("compensation/BPMN2-Compensation-ThrowSpecificForSubProcess.bpmn2");
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", 1);
        ProcessInstance processInstance = ksession.startProcess("CompensationSpecificSubProcess", params);

        // compensation activity (assoc. with script task) signaled *after* to-compensate script task
        assertProcessInstanceCompleted(processInstance.getId(), ksession);
        if( ! isPersistence() ) {
            assertProcessVarValue(processInstance, "x", null);
        } else {
            assertProcessVarValue(processInstance, "x", "");
        }
    }

    @Test
    @RequiresQueueBased
    @Broken
    public void compensationViaCancellation() throws Exception {
        KieSession ksession = createKnowledgeSession("compensation/BPMN2-Compensation-IntermediateThrowEvent.bpmn2");
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", "0");
        ProcessInstance processInstance = ksession.startProcess("CompensateIntermediateThrowEvent", params);

        ksession.signalEvent("Cancel", null, processInstance.getId());
        ksession.getWorkItemManager().completeWorkItem(workItemHandler.getWorkItem().getId(), null);

        // compensation activity (assoc. with script task) signaled *after* script task
        assertProcessInstanceCompleted(processInstance.getId(), ksession);
        assertProcessVarValue(processInstance, "x", "1");
    }

    @Test
    @Broken
    public void compensationInvokingSubProcess() throws Exception {
        KieSession ksession = createKnowledgeSession("compensation/BPMN2-UserTaskCompensation.bpmn2");
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", new SystemOutWorkItemHandler());

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("compensation", "True");
        ProcessInstance processInstance = ksession.startProcess("UserTaskCompensation", params);

        assertProcessInstanceCompleted(processInstance.getId(), ksession);
        assertProcessVarValue(processInstance, "compensation", "compensation");
    }

    @Test
    @RequiresQueueBased
    @Broken
    public void compensationViaUserTask() throws Exception {
        KieSession ksession = createKnowledgeSession("compensation/BPMN2-Compensation-UserTask.bpmn2");
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("x", "0");
        ProcessInstance processInstance = ksession.startProcess("CompensateIntermediateThrowEvent", params);

        // should NOT cause compensation since compensated activity has not yet completed (or started)!
        ksession.signalEvent("Compensation", "_3", processInstance.getId());

        assertProcessInstanceActive(processInstance.getId(), ksession);

        // user task -> script task (associated with compensation) --> intermediate throw compensation event
        ksession.getWorkItemManager().completeWorkItem(workItemHandler.getWorkItem().getId(), null);

        assertProcessInstanceActive(processInstance.getId(), ksession);

        // compensation task
        ksession.getWorkItemManager().completeWorkItem(workItemHandler.getWorkItem().getId(), null);

        assertProcessInstanceCompleted(processInstance.getId(), ksession);
    }
}