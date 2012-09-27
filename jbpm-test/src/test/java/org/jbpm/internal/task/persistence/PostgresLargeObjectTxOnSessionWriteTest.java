package org.jbpm.internal.task.persistence;

import static junit.framework.Assert.fail;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.drools.*;
import org.drools.builder.*;
import org.drools.event.process.*;
import org.drools.io.ResourceFactory;
import org.drools.process.instance.WorkItemManager;
import org.drools.runtime.StatefulKnowledgeSession;
import org.jbpm.task.Group;
import org.jbpm.task.User;
import org.jbpm.task.service.*;
import org.jbpm.task.service.hornetq.*;
import org.jbpm.task.service.responsehandlers.BlockingGetTaskResponseHandler;
import org.jbpm.task.service.responsehandlers.BlockingTaskOperationResponseHandler;
import org.junit.*;
import org.junit.runner.RunWith;

import bitronix.tm.resource.jdbc.PoolingDataSource;

/**
 * Test for BZ 852044: 
 * - Cannot view human task list on EAP 6.0 with Hibernate 4 and PostgreSQL 
 * due to: org.postgresql.util.PSQLException: Large Objects may not be used in auto-commit mode.
 *
 */
public class PostgresLargeObjectTxOnSessionWriteTest {

    private static PoolingDataSource dataSource;
    private static Thread serverThread;
    private static EntityManagerFactory emf;
    
    private final static int hornetQServerPort = 5446;
    private final static String userId = "jsvitak";
    private final static int waitTime = 1000;
    
    private final static String BPMN2_FILE_NAME = "internal/largeObjectPostgresTaskTx.bpmn2";
    
    @BeforeClass
    public static void beforeClass() throws Exception {
        dataSource = setupDataSource();

        // Use persistence.xml configuration
        emf = Persistence.createEntityManagerFactory("org.jbpm.task");

        TaskService taskService = new TaskService(emf, SystemEventListenerFactory.getSystemEventListener());

        HashMap<String, User> users = new HashMap<String, User>();
        users.put("admin", new User("Administrator"));
        users.put("jiri", new User(userId));
        taskService.addUsersAndGroups(users, new HashMap<String, Group>());

        TaskServer server = new HornetQTaskServer(taskService, hornetQServerPort);
        serverThread = new Thread(server);
        serverThread.start();
        
        System.out.println("Waiting for the HornetQTask Server to come up");
        while (!server.isRunning()) {
            System.out.print(".");
            Thread.sleep(50);
        }
    }

    @AfterClass
    public static void afterClass() {
        try { 
            serverThread.stop(); 
            emf.close();
            dataSource.close();
        } catch(Exception e) { 
            // swallow!
        }
    }

    @Test
//    @RequiresDialect( value=PostgreSQL82Dialect.class )
    public void inJvmTest() {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newClassPathResource(BPMN2_FILE_NAME), ResourceType.BPMN2);
        if (kbuilder.hasErrors()) {
            System.out.println(kbuilder.getErrors());
            fail(kbuilder.getErrors().toString());
        }

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());

        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();

        TaskClient taskClient = new TaskClient(new HornetQTaskClientConnector("HornetQConnector" + UUID.randomUUID(),
                new HornetQTaskClientHandler(SystemEventListenerFactory.getSystemEventListener())));
        taskClient.connect("127.0.0.1", hornetQServerPort);

        CommandBasedHornetQWSHumanTaskHandler handler = new CommandBasedHornetQWSHumanTaskHandler(ksession);
        handler.setClient(taskClient);
        handler.connect();

        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", handler);

        CountDownLatch latch = new CountDownLatch(1);

        CompleteProcessEventListener processEventListener = new CompleteProcessEventListener(latch);
        ksession.addEventListener(processEventListener);
        ksession.startProcess("test.testproc");

        // Handle task
        long workItemId = ((WorkItemManager) ksession.getWorkItemManager()).getWorkItems().iterator().next().getId();
        BlockingGetTaskResponseHandler getTaskHandler = new BlockingGetTaskResponseHandler();
        taskClient.getTaskByWorkItemId(workItemId, getTaskHandler);
        getTaskHandler.waitTillDone(waitTime);
        long taskId = getTaskHandler.getTask().getId();
        
        // Go straight from Ready to Inprogress
        BlockingTaskOperationResponseHandler taskOperationResponseHandler = new BlockingTaskOperationResponseHandler();
        taskClient.start( taskId, userId, taskOperationResponseHandler );
        taskOperationResponseHandler.waitTillDone(waitTime);
        
        // Complete
        taskOperationResponseHandler = new BlockingTaskOperationResponseHandler();
        taskClient.complete( taskId, userId, null, taskOperationResponseHandler ); 
        taskOperationResponseHandler.waitTillDone(waitTime);
        
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            taskClient.disconnect();
            handler.dispose();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static PoolingDataSource setupDataSource() {
        // create data source
        PoolingDataSource pds = new PoolingDataSource();
        pds.setUniqueName("jdbc/taskDS");
        pds.setClassName("bitronix.tm.resource.jdbc.lrc.LrcXADataSource");
        pds.setMaxPoolSize(5);
        pds.setAllowLocalTransactions(true);
        pds.getDriverProperties().put("user", "task");
        pds.getDriverProperties().put("password", "task");
        pds.getDriverProperties().put("url", "jdbc:postgresql://localhost:5432/task");
        pds.getDriverProperties().put("driverClassName", "org.postgresql.Driver");
        pds.init();
        return pds;
    }
    
    public class CompleteProcessEventListener implements ProcessEventListener {
        
        private CountDownLatch latch;
        
        public CompleteProcessEventListener(CountDownLatch latch) {
            this.latch = latch;
        }    
        
        public void beforeProcessStarted(ProcessStartedEvent event) {
        }

        public void afterProcessStarted(ProcessStartedEvent event) {
            
        }

        public void beforeProcessCompleted(ProcessCompletedEvent event) {
            latch.countDown();        
        }

        public void afterProcessCompleted(ProcessCompletedEvent event) {             

        }

        public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {

        }

        public void afterNodeTriggered(ProcessNodeTriggeredEvent event) {
      
        }

        public void beforeNodeLeft(ProcessNodeLeftEvent event) {

        }

        public void afterNodeLeft(ProcessNodeLeftEvent event) {

        }

        public void beforeVariableChanged(ProcessVariableChangedEvent event) {
            
        }

        public void afterVariableChanged(ProcessVariableChangedEvent event) {
            
        }
    }
}
