package org.jbpm.concurrency;

import java.util.Properties;

import javax.naming.InitialContext;
import javax.persistence.Persistence;
import javax.transaction.UserTransaction;

import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
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

    public ConcurrentRequestsTest() {
        super(false);
        setPersistence(true);
    }

    @Test
    public void testXXXy() {
        PoolingDataSource ds = mySetupPoolingDataSource("1");

        Properties p = new Properties();
        p.setProperty(Environment.DATASOURCE, "jdbc/1");
        emf = Persistence.createEntityManagerFactory(
                "org.jbpm.persistence.jpa", p);

        final StatefulKnowledgeSession ksession = createKnowledgeSession("humantask.bpmn");
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task",
                new DoNothingWorkItemHandler());

        ds = mySetupPoolingDataSource("2");

        p = new Properties();
        p.setProperty(Environment.DATASOURCE, "jdbc/2");
        emf = Persistence.createEntityManagerFactory(
                "org.jbpm.persistence.jpa", p);

        final StatefulKnowledgeSession ksession2 = createKnowledgeSession("humantask.bpmn");
        ksession2.getWorkItemManager().registerWorkItemHandler("Human Task",
                new DoNothingWorkItemHandler());

        ds = mySetupPoolingDataSource("3");

        p = new Properties();
        p.setProperty(Environment.DATASOURCE, "jdbc/3");
        emf = Persistence.createEntityManagerFactory(
                "org.jbpm.persistence.jpa", p);

        final StatefulKnowledgeSession ksession3 = createKnowledgeSession("humantask.bpmn");
        ksession3.getWorkItemManager().registerWorkItemHandler("Human Task",
                new DoNothingWorkItemHandler());

        RunThreadCommands commands = new RunThreadCommands();

        for (int i = 0; i < 5; i++) {
            final int ii = i;
            commands.addCommand(new RunThreadCommands.Command() {
                public void doWork() throws Exception {
                    UserTransaction ut = (UserTransaction) new InitialContext()
                            .lookup("java:comp/UserTransaction");
                    ut.begin();
                    try {
                        Thread.sleep(2000);
                        ProcessInstance processInstance = ksession
                                .startProcess("com.sample.bpmn.hello");
                        Thread.sleep(2000);
                        ut.commit();
                    } catch (Exception e) {
                        ut.rollback();
                        throw e;
                    }

                }
            });
            commands.addCommand(new RunThreadCommands.Command() {
                public void doWork() throws Exception {
                    UserTransaction ut = (UserTransaction) new InitialContext()
                            .lookup("java:comp/UserTransaction");
                    ut.begin();
                    try {
                        logger.info("start proces " + ii + " for session 1-b");
                        ProcessInstance processInstance = ksession
                                .startProcess("com.sample.bpmn.hello");
                        Thread.sleep(2000);
                        ut.commit();
                    } catch (Exception e) {
                        ut.rollback();
                        throw e;
                    }

                }
            });
            commands.addCommand(new RunThreadCommands.Command() {

                public void doWork() throws Exception {
                    UserTransaction ut = (UserTransaction) new InitialContext()
                            .lookup("java:comp/UserTransaction");
                    ut.begin();
                    try {
                        logger.info("start proces " + ii + " for session 2");
                        ProcessInstance processInstance = ksession2
                                .startProcess("com.sample.bpmn.hello");
                        Thread.sleep(2000);
                        ut.commit();
                    } catch (Exception e) {
                        ut.rollback();
                        throw e;
                    }
                }
            });
            commands.addCommand(new RunThreadCommands.Command() {

                public void doWork() throws Exception {
                    UserTransaction ut = (UserTransaction) new InitialContext()
                            .lookup("java:comp/UserTransaction");
                    ut.begin();
                    logger.info("start proces " + ii + " for session 2-b");
                    try {
                        ProcessInstance processInstance = ksession2
                                .startProcess("com.sample.bpmn.hello");
                        Thread.sleep(2000);
                        ut.commit();
                    } catch (Exception e) {
                        ut.rollback();
                        throw e;
                    }
                }
            });
            commands.addCommand(new RunThreadCommands.Command() {
                public void doWork() throws Exception {
                    UserTransaction ut = (UserTransaction) new InitialContext()
                            .lookup("java:comp/UserTransaction");
                    ut.begin();
                    try {
                        logger.info("start proces " + ii + " for session 3");

                        ProcessInstance processInstance = ksession3
                                .startProcess("com.sample.bpmn.hello");
                        Thread.sleep(2000);
                        ut.commit();
                    } catch (Exception e) {
                        ut.rollback();
                        throw e;
                    }
                }
            });
            commands.addCommand(new RunThreadCommands.Command() {
                public void doWork() throws Exception {
                    UserTransaction ut = (UserTransaction) new InitialContext()
                            .lookup("java:comp/UserTransaction");
                    ut.begin();
                    try {
                        logger.info("start proces " + ii + " for session 3-b");
                        ProcessInstance processInstance = ksession3
                                .startProcess("com.sample.bpmn.hello");
                        Thread.sleep(2000);
                        ut.commit();
                    } catch (Exception e) {
                        ut.rollback();
                        throw e;
                    }
                }
            });

        }
        commands.runCommands();
    }

    public PoolingDataSource mySetupPoolingDataSource(String id) {
        PoolingDataSource pds = new PoolingDataSource();
        pds.setUniqueName("jdbc/" + id);
        
        pds.setMaxPoolSize(150);
        pds.setMinPoolSize(1);

        pds.setMaxIdleTime(60);

        pds.setClassName("org.h2.jdbcx.JdbcDataSource");
        pds.getDriverProperties().put(
                "URL",
                "jdbc:h2:jbpm-db-" + id+";MVCC=TRUE;LOCK_TIMEOUT=10000");
        pds.setAllowLocalTransactions(true);
        pds.init();
        return pds;
    }
}
