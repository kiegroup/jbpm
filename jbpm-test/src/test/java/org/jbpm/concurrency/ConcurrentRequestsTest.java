package org.jbpm.concurrency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.Persistence;
import javax.transaction.UserTransaction;

import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemHandler;
import org.drools.runtime.process.WorkItemManager;
import org.hibernate.cfg.Environment;
import org.jbpm.concurrency.util.RunThreadCommands;
import org.jbpm.process.instance.impl.demo.DoNothingWorkItemHandler;
import org.jbpm.test.JbpmJUnitTestCase;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bitronix.tm.resource.jdbc.PoolingDataSource;

public class ConcurrentRequestsTest extends JbpmJUnitTestCase {
    private Logger logger = LoggerFactory
            .getLogger(ConcurrentRequestsTest.class);
    final Map<String, Object> results = new HashMap<String, Object>();

    public ConcurrentRequestsTest() {
        super(false);
        setPersistence(true);
    }

    @Test
    public void testConcurrency() throws Exception {
        mySetupPoolingDataSource("1");

        Properties p = new Properties();
        p.setProperty(Environment.DATASOURCE, "jdbc/1");
        UserTransaction ut = (UserTransaction) new InitialContext()
                .lookup("java:comp/UserTransaction");
        ut.begin();
        emf = Persistence.createEntityManagerFactory(
                "org.jbpm.persistence.jpa", p);
        final TestWorkItemHandler twh1 = new TestWorkItemHandler();
        final StatefulKnowledgeSession ksession = createKnowledgeSession("concurrency_humantask.bpmn");
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                twh1);
        ut.commit();
        mySetupPoolingDataSource("2");
        p = new Properties();
        p.setProperty(Environment.DATASOURCE, "jdbc/2");
        ut = (UserTransaction) new InitialContext()
                .lookup("java:comp/UserTransaction");
        ut.begin();
        emf = Persistence.createEntityManagerFactory(
                "org.jbpm.persistence.jpa", p);

        final TestWorkItemHandler twh2 = new TestWorkItemHandler();
        final StatefulKnowledgeSession ksession2 = createKnowledgeSession("concurrency_humantask.bpmn");
        ksession2.getWorkItemManager().registerWorkItemHandler("Human Task",
                twh2);
        ut.commit();
        mySetupPoolingDataSource("3");
        ut = (UserTransaction) new InitialContext()
                .lookup("java:comp/UserTransaction");
        ut.begin();
        p = new Properties();
        p.setProperty(Environment.DATASOURCE, "jdbc/3");
        emf = Persistence.createEntityManagerFactory(
                "org.jbpm.persistence.jpa", p);

        final TestWorkItemHandler twh3 = new TestWorkItemHandler();
        final StatefulKnowledgeSession ksession3 = createKnowledgeSession("concurrency_humantask.bpmn");
        ksession3.getWorkItemManager().registerWorkItemHandler("Human Task",
                twh3);
        ut.commit();

        RunThreadCommands commands = new RunThreadCommands();

        final List<ProcessInstance> pis = new ArrayList<ProcessInstance>();
        for (int i = 0; i < 10; i++) {
            final int ii = i;
            commands.addCommand(new RunThreadCommands.Command() {
                public void doWork() throws Exception {
                    logger.info("start proces " + ii + " for session 1");
                    runProcess(ksession, twh1);
                }
            });
            commands.addCommand(new RunThreadCommands.Command() {

                public void doWork() throws Exception {
                    logger.info("start proces " + ii + " for session 1-b");
                    runProcess(ksession, twh1);
                }
            });
            commands.addCommand(new RunThreadCommands.Command() {

                public void doWork() throws Exception {
                    logger.info("start proces " + ii + " for session 2");
                    runProcess(ksession2, twh2);
                }
            });
            commands.addCommand(new RunThreadCommands.Command() {

                public void doWork() throws Exception {
                    logger.info("start proces " + ii + " for session 2-b");
                    runProcess(ksession2, twh2);
                }
            });
            commands.addCommand(new RunThreadCommands.Command() {
                public void doWork() throws Exception {
                    logger.info("start proces " + ii + " for session 3");
                    runProcess(ksession3, twh3);
                }
            });
            commands.addCommand(new RunThreadCommands.Command() {
                public void doWork() throws Exception {
                    logger.info("start proces " + ii + " for session 3-b");
                    runProcess(ksession3, twh3);
                }
            });
        }

        commands.runCommands();
        for (ProcessInstance pi : pis) {
            assertNotNull(ksession.getProcessInstance(pi.getId()));
        }
        // this fails because the JPAProcessInstanceManager's process map is
        // cleared after every transaction
        // assertEquals(20, ksession.getProcessInstances().size());
    }

    public PoolingDataSource mySetupPoolingDataSource(String id) {
        PoolingDataSource pds = new PoolingDataSource();
        pds.setUniqueName("jdbc/" + id);

        pds.setMaxPoolSize(300);
        pds.setMinPoolSize(1);

        pds.setMaxIdleTime(60);

        pds.setClassName("org.h2.jdbcx.JdbcDataSource");
        pds.getDriverProperties().put("URL",
                "jdbc:h2:mem:jbpm-db-" + id + ";MVCC=TRUE;LOCK_TIMEOUT=35000");
        pds.setAllowLocalTransactions(false);
        pds.init();
        return pds;
    }

    private void runProcess(StatefulKnowledgeSession ksession,
            TestWorkItemHandler twh) throws Exception {

        UserTransaction ut = (UserTransaction) new InitialContext()
                .lookup("java:comp/UserTransaction");

        try {
            ut.begin();
            ProcessInstance processInstance = ksession
                    .startProcess("concurrency");
            Thread.sleep(100);
            ut.commit();
        } catch (Exception e) {
            ut.rollback();
            throw e;
        }
        Thread.sleep(3100);
        ksession.getWorkItemManager().completeWorkItem(
                twh.getWorkItem().getId(), results);

        Thread.sleep(3100);
        ksession.getWorkItemManager().completeWorkItem(
                twh.getWorkItem().getId(), results);

    }

    public static class TestWorkItemHandler implements WorkItemHandler {

        private List<WorkItem> workItems =Collections.synchronizedList(new ArrayList<WorkItem>());

        public synchronized void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
            workItems.add(workItem);
        }

        public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
        }

        public synchronized WorkItem getWorkItem() {
            if (workItems.size() == 0) {
                return null;
            }
            return workItems.remove(0);
        }

        public List<WorkItem> getWorkItems() {
            List<WorkItem> result = new ArrayList<WorkItem>(workItems);
            workItems.clear();
            return result;
        }

    }
}
