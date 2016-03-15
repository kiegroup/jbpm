package org.jbpm.bpmn2;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jbpm.bpmn2.objects.MyError;
import org.jbpm.bpmn2.objects.TestWorkItemHandler;
import org.jbpm.bpmn2.test.Broken;
import org.jbpm.test.util.TestBpmn2ProcessEventListener;
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
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Parameterized.class)
public class StacklessOneOffDevelopmentTest extends JbpmBpmn2TestCase {

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
        params.add( new Object [] {
                true, // stackless
                true, // persistence
                true, //  broken
                "STACKLESS BROKEN PERSISTENCE"
        });
        return params;
    };

    private Logger logger = LoggerFactory.getLogger(StacklessOneOffDevelopmentTest.class);
    private TestBpmn2ProcessEventListener procEventListener;
    private final boolean broken;

    public StacklessOneOffDevelopmentTest(boolean stackless, boolean persistence, boolean broken, String name) {
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
        ProcessInstance processInstance = ksession.startProcess("BPMN2-ErrorBoundaryEventOnTask");

        assertProcessInstanceActive(processInstance);
        assertNodeTriggered(processInstance.getId(), "start", "split", "User Task", "User task error attached",
                "Script Task", "error2", "error1");
    }

}