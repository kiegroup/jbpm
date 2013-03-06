package org.jbpm.persistence;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.drools.common.InternalKnowledgeRuntime;
import org.drools.marshalling.impl.MarshallingConfigurationImpl;
import org.drools.marshalling.impl.ProtobufMarshaller;
import org.jbpm.process.instance.InternalProcessRuntime;
import org.jbpm.process.instance.timer.TimerInstance;
import org.jbpm.process.instance.timer.TimerManager;
import org.jbpm.process.workitem.wsht.LocalHTWorkItemHandler;
import org.jbpm.task.Group;
import org.jbpm.task.User;
import org.jbpm.task.identity.UserGroupCallbackManager;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.task.service.TaskService;
import org.jbpm.task.service.local.LocalTaskService;
import org.jbpm.task.utils.OnErrorAction;
import org.jbpm.test.JbpmTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.KieBase;
import org.kie.KnowledgeBaseFactory;
import org.kie.SystemEventListenerFactory;
import org.kie.runtime.Environment;
import org.kie.runtime.EnvironmentName;
import org.kie.runtime.StatefulKnowledgeSession;
import org.kie.runtime.process.ProcessInstance;

import bitronix.tm.TransactionManagerServices;
import bitronix.tm.resource.jdbc.PoolingDataSource;

public class SerializedTimerRollbackTest extends JbpmTestCase {

    private PoolingDataSource ds;
    private EntityManagerFactory emf;

    public SerializedTimerRollbackTest() {
        super(true);
    }

    @Before
    public void setup() {
        ds = new PoolingDataSource();
        ds.setUniqueName("jdbc/testDS1");
        ds.setClassName("org.h2.jdbcx.JdbcDataSource");
        ds.setMaxPoolSize(3);
        ds.setAllowLocalTransactions(true);
        ds.getDriverProperties().put("user", "sa");
        ds.getDriverProperties().put("password", "sasa");
        ds.getDriverProperties().put("URL", "jdbc:h2:mem:mydb");
        ds.init();
        UserGroupCallbackManager.getInstance().setCallback(null);

        setPoolingDataSource(ds);

        emf = Persistence
                .createEntityManagerFactory("org.jbpm.persistence.jpa");
        try {
            UserTransaction ut = InitialContext
                    .doLookup("java:comp/UserTransaction");
            ut.begin();
            EntityManager em = emf.createEntityManager()
                    .getEntityManagerFactory().createEntityManager();
            em.createQuery("delete from SessionInfo").executeUpdate();
            em.close();
            ut.commit();
        } catch (Exception e) {

        }

        setEntityManagerFactory(emf);

    }

    @After
    public void tearDown() {
        emf.close();
        ds.close();
    }

    @Test
    public void testSerizliableTestsWithExternalRollback() {
        try {

            Environment env = KnowledgeBaseFactory.newEnvironment();

            TransactionManager tm = TransactionManagerServices
                    .getTransactionManager();
            env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);
            env.set(EnvironmentName.TRANSACTION_MANAGER,
                    TransactionManagerServices.getTransactionManager());
            TaskService taskService = new org.jbpm.task.service.TaskService(
                    emf, SystemEventListenerFactory.getSystemEventListener());
            Map<String, User> users = new HashMap<String, User>();
            users.put("Administrator", new User("Administrator"));
            users.put("john", new User("john"));
            Map<String, Group> groups = new HashMap<String, Group>();
            taskService.addUsersAndGroups(users, groups);
            org.jbpm.task.TaskService humanTaskClient = new LocalTaskService(
                    taskService);
            ;

            System.out.println("Task service created");

            KieBase kbase = createKnowledgeBase("HumanTaskWithBoundaryTimer.bpmn");
            StatefulKnowledgeSession sesion = createKnowledgeSession(kbase, env);
            System.out.println("Created knowledge session");

            LocalHTWorkItemHandler localHTWorkItemHandler = new LocalHTWorkItemHandler(
                    humanTaskClient, sesion, OnErrorAction.RETHROW);
            localHTWorkItemHandler.connect();
            sesion.getWorkItemManager().registerWorkItemHandler("Human Task",
                    localHTWorkItemHandler);
            System.out.println("Attached human task work item handler");
            List<Long> committedProcessInstanceIds = new ArrayList<Long>();
            for (int i = 0; i < 10; i++) {
                tm.begin();
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("test", "john");
                System.out.println("Creating process instance: " + i);
                ProcessInstance pi = sesion.startProcess("PROCESS_1", params);
                if (i % 2 == 0) {
                    committedProcessInstanceIds.add(pi.getId());
                    tm.commit();
                } else {
                    tm.rollback();
                }
            }

            Connection c = ds.getConnection();
            Statement st = c.createStatement();
            ResultSet rs = st
                    .executeQuery("select rulesbytearray from sessioninfo");
            rs.next();
            Blob b = rs.getBlob("rulesbytearray");
            assertNotNull(b);

            ProtobufMarshaller marshaller = new ProtobufMarshaller(kbase,
                    new MarshallingConfigurationImpl());
            StatefulKnowledgeSession session = marshaller.unmarshall(b
                    .getBinaryStream());
            assertNotNull(session);

            TimerManager timerManager = ((InternalProcessRuntime) ((InternalKnowledgeRuntime) session)
                    .getProcessRuntime()).getTimerManager();
            assertNotNull(timerManager);

            Collection<TimerInstance> timers = timerManager.getTimers();
            assertNotNull(timers);
            assertEquals(5, timers.size());

            for (TimerInstance timerInstance : timers) {
                assertTrue(committedProcessInstanceIds.contains(timerInstance
                        .getProcessInstanceId()));
                sesion.abortProcessInstance(timerInstance
                        .getProcessInstanceId());
            }
            LocalTaskService lts = new LocalTaskService(taskService);
            List<TaskSummary> tasks = lts.getTasksAssignedAsPotentialOwner(
                    "john", "en-UK");
            lts.dispose();
            assertEquals(0, tasks.size());
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception thrown");
        }

    }

    @Test
    public void testSerizliableTestsWithEngineRollback() {
        try {

            Environment env = KnowledgeBaseFactory.newEnvironment();
            env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);
            env.set(EnvironmentName.TRANSACTION_MANAGER,
                    TransactionManagerServices.getTransactionManager());
            TaskService taskService = new org.jbpm.task.service.TaskService(
                    emf, SystemEventListenerFactory.getSystemEventListener());
            Map<String, User> users = new HashMap<String, User>();
            users.put("Administrator", new User("Administrator"));
            users.put("john", new User("john"));
            Map<String, Group> groups = new HashMap<String, Group>();
            taskService.addUsersAndGroups(users, groups);
            org.jbpm.task.TaskService humanTaskClient = new LocalTaskService(
                    taskService);
            ;

            System.out.println("Task service created");

            KieBase kbase = createKnowledgeBase("HumanTaskWithBoundaryTimer.bpmn");
            StatefulKnowledgeSession sesion = createKnowledgeSession(kbase, env);
            System.out.println("Created knowledge session");

            LocalHTWorkItemHandler localHTWorkItemHandler = new LocalHTWorkItemHandler(
                    humanTaskClient, sesion, OnErrorAction.RETHROW);
            localHTWorkItemHandler.connect();
            sesion.getWorkItemManager().registerWorkItemHandler("Human Task",
                    localHTWorkItemHandler);
            System.out.println("Attached human task work item handler");
            List<Long> committedProcessInstanceIds = new ArrayList<Long>();
            for (int i = 0; i < 10; i++) {
                if (i % 2 == 0) {
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("test", "john");
                    System.out.println("Creating process instance: " + i);
                    ProcessInstance pi = sesion.startProcess("PROCESS_1",
                            params);

                    committedProcessInstanceIds.add(pi.getId());

                } else {
                    try {
                        Map<String, Object> params = new HashMap<String, Object>();
                        params.put("test", "test");
                        System.out.println("Creating process instance: " + i);
                        ProcessInstance pi = sesion.startProcess("PROCESS_1",
                                params);
                    } catch (Exception e) {
                        System.out.println("Process rolled back");
                    }
                }
            }

            Connection c = ds.getConnection();
            Statement st = c.createStatement();
            ResultSet rs = st
                    .executeQuery("select rulesbytearray from sessioninfo");
            rs.next();
            Blob b = rs.getBlob("rulesbytearray");
            assertNotNull(b);

            ProtobufMarshaller marshaller = new ProtobufMarshaller(kbase,
                    new MarshallingConfigurationImpl());
            StatefulKnowledgeSession session = marshaller.unmarshall(b
                    .getBinaryStream());
            assertNotNull(session);

            TimerManager timerManager = ((InternalProcessRuntime) ((InternalKnowledgeRuntime) session)
                    .getProcessRuntime()).getTimerManager();
            assertNotNull(timerManager);

            Collection<TimerInstance> timers = timerManager.getTimers();
            assertNotNull(timers);
            assertEquals(5, timers.size());

            for (TimerInstance timerInstance : timers) {
                assertTrue(committedProcessInstanceIds.contains(timerInstance
                        .getProcessInstanceId()));
                sesion.abortProcessInstance(timerInstance
                        .getProcessInstanceId());
            }
            LocalTaskService lts = new LocalTaskService(taskService);
            List<TaskSummary> tasks = lts.getTasksAssignedAsPotentialOwner(
                    "john", "en-UK");
            lts.dispose();
            assertEquals(0, tasks.size());
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception thrown");
        }
    }

}