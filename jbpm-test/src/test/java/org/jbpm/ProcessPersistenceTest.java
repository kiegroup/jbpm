package org.jbpm;

import javax.naming.InitialContext;
import javax.transaction.UserTransaction;

import org.jbpm.process.instance.impl.demo.DoNothingWorkItemHandler;
import org.jbpm.test.JbpmTestCase;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.runtime.StatefulKnowledgeSession;
import org.kie.runtime.process.ProcessInstance;

/**
 * This is a sample file to test a process.
 */
public class ProcessPersistenceTest extends JbpmTestCase {

    public ProcessPersistenceTest() {
        super(true);
    }

    @BeforeClass
    public static void setup() throws Exception {
        setUpDataSource();
    }

    @Test
    public void testProcess() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("hello.bpmn");
        ProcessInstance processInstance = ksession
                .startProcess("com.sample.bpmn.hello");
        // check whether the process instance has completed successfully
        assertProcessInstanceCompleted(processInstance);
        assertNodeTriggered(processInstance.getId(), "StartProcess", "Hello",
                "EndProcess");
    }

    @Test
    public void testTransactions() throws Exception {
        StatefulKnowledgeSession ksession = createKnowledgeSession("humantask.bpmn");
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                new DoNothingWorkItemHandler());

        UserTransaction ut = (UserTransaction) new InitialContext()
                .lookup("java:comp/UserTransaction");
        ut.begin();
        ProcessInstance processInstance = ksession
                .startProcess("com.sample.bpmn.hello");
        ut.rollback();

        assertNull(ksession.getProcessInstance(processInstance.getId()));
    }

}