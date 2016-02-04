package org.jbpm.bpmn2;

import static org.jbpm.bpmn2.StandaloneBPMNProcessTest.runTestErrorSignallingExceptionServiceTask;
import static org.jbpm.bpmn2.StandaloneBPMNProcessTest.runTestSignallingExceptionServiceTask;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.bpmn2.handler.ReceiveTaskHandler;
import org.jbpm.bpmn2.handler.SendTaskHandler;
import org.jbpm.bpmn2.handler.ServiceTaskHandler;
import org.jbpm.bpmn2.objects.MyError;
import org.jbpm.bpmn2.objects.Person;
import org.jbpm.bpmn2.objects.TestWorkItemHandler;
import org.jbpm.bpmn2.test.Broken;
import org.jbpm.process.instance.impl.demo.DoNothingWorkItemHandler;
import org.jbpm.process.instance.impl.demo.SystemOutWorkItemHandler;
import org.jbpm.ruleflow.instance.RuleFlowProcessInstance;
import org.jbpm.test.util.CountDownProcessEventListener;
import org.jbpm.test.util.TestProcessEventListener;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.model.Statement;
import org.kie.api.KieBase;
import org.kie.api.event.process.DefaultProcessEventListener;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.process.ProcessNodeLeftEvent;
import org.kie.api.event.process.ProcessNodeTriggeredEvent;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Parameterized.class)
public class StacklessDevelopmentTest extends JbpmBpmn2TestCase {

    @Parameters(name="{2}")
    public static Collection<Object[]> persistence() {
        List<Object[]> params = new ArrayList<Object[]>();
        params.add( new Object [] {
                false, // recursive
                false, //  everything
                "RECURSIVE"
        });
        params.add( new Object [] {
                true, // stackless
                false, //  everything
                "STACKLESS FIXED"
        });
        params.add( new Object [] {
                true, // stackless
                true, //  broken
                "STACKLESS BROKEN"
        });
        return params;
    };

    private Logger logger = LoggerFactory.getLogger(StacklessDevelopmentTest.class);
    private TestProcessEventListener procEventListener;
    private final boolean broken;

    public StacklessDevelopmentTest(boolean stackless, boolean broken, String name) {
        super(false, false, stackless);
        this.broken = broken;
    }

    @Rule
    public TestRule watcher = new TestRule() {

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
        procEventListener = new TestProcessEventListener();
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
    public void testErrorBoundaryEventOnEntry() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-BoundaryErrorEventCatchingOnEntryException.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ksession.addEventListener(procEventListener);
        TestWorkItemHandler handler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",handler);

        ProcessInstance processInstance = ksession.startProcess("BoundaryErrorEventOnEntry");
        assertProcessInstanceActive(processInstance.getId(), ksession);
        assertEquals(1, handler.getWorkItems().size());
    }

    @Test
    public void testCatchErrorBoundaryEventOnTask() throws Exception {
        KieBase kbase = createKnowledgeBase("BPMN2-ErrorBoundaryEventOnTask.bpmn2");
        ksession = createKnowledgeSession(kbase);
        ksession.addEventListener(procEventListener);
        procEventListener.useNodeInstanceUniqueId();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", new TestWorkItemHandler(){

            @Override
            public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
                if (workItem.getParameter("ActorId").equals("mary")) {
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
                "Script Task", "error1", "error2");

    }

    @Test
    @Broken
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
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                workItemHandler);
        ProcessInstance processInstance = ksession
                .startProcess("BPMN2-EventSubprocessError");
        assertProcessInstanceActive(processInstance);
        ksession = restoreSession(ksession, true);
        ksession.addEventListener(listener);

        WorkItem workItem = workItemHandler.getWorkItem();
        assertNotNull(workItem);
        ksession.getWorkItemManager().completeWorkItem(workItem.getId(), null);
        assertProcessInstanceFinished(processInstance, ksession);
        assertNodeTriggered(processInstance.getId(), "start", "User Task 1",
                "end", "Sub Process 1", "start-sub", "Script Task 1", "end-sub");
        assertEquals(1, executednodes.size());

    }

}