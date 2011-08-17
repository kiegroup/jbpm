package org.jbpm.persistence.session;

import static org.jbpm.persistence.util.PersistenceUtil.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.transaction.UserTransaction;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.SessionConfiguration;
import org.drools.command.runtime.process.CompleteWorkItemCommand;
import org.drools.command.runtime.process.GetProcessInstanceCommand;
import org.drools.command.runtime.process.StartProcessCommand;
import org.drools.compiler.PackageBuilder;
import org.drools.definition.KnowledgePackage;
import org.drools.definitions.impl.KnowledgePackageImp;
import org.drools.persistence.SingleSessionCommandService;
import org.drools.persistence.jpa.JpaJDKTimerService;
import org.drools.persistence.jpa.processinstance.JPAWorkItemManagerFactory;
import org.drools.process.core.Work;
import org.drools.process.core.impl.WorkImpl;
import org.drools.rule.Package;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.drools.runtime.process.NodeInstance;
import org.drools.runtime.process.ProcessInstance;
import org.drools.runtime.process.WorkItem;
import org.jbpm.JbpmTestCase;
import org.jbpm.compiler.ProcessBuilderImpl;
import org.jbpm.persistence.processinstance.JPAProcessInstanceManagerFactory;
import org.jbpm.persistence.processinstance.JPASignalManagerFactory;
import org.jbpm.process.core.timer.Timer;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.ruleflow.instance.RuleFlowProcessInstance;
import org.jbpm.workflow.core.Node;
import org.jbpm.workflow.core.impl.ConnectionImpl;
import org.jbpm.workflow.core.impl.DroolsConsequenceAction;
import org.jbpm.workflow.core.node.ActionNode;
import org.jbpm.workflow.core.node.EndNode;
import org.jbpm.workflow.core.node.StartNode;
import org.jbpm.workflow.core.node.SubProcessNode;
import org.jbpm.workflow.core.node.TimerNode;
import org.jbpm.workflow.core.node.WorkItemNode;
import org.jbpm.workflow.instance.node.SubProcessNodeInstance;

import bitronix.tm.TransactionManagerServices;
import bitronix.tm.resource.jdbc.PoolingDataSource;

public class SingleSessionCommandServiceTest extends JbpmTestCase {

	private PoolingDataSource ds1;
	private EntityManagerFactory emf;
	private SessionConfiguration config;
	private Environment env;
   
    private static Logger logger = Logger.getLogger(SingleSessionCommandServiceTest.class);
    
    static {
		DOMConfigurator.configure(SingleSessionCommandServiceTest.class.getResource("/log4j.xml"));
    }
    
    protected void setUp() {
        ds1 = setupPoolingDataSource();
        
        ds1.init();
        emf = Persistence.createEntityManagerFactory( PERSISTENCE_UNIT_NAME );
        
        config = createSessionConfiguration();
        env = createEnvironment();
    }

    protected void tearDown() {
        emf.close();
        ds1.close();
    }

    /**
     * Create a default session configuration containing the following properties: <ul>
     * <li>drools.commandService</li>
     * <li>drools.processInstanceManagerFactory</li>
     * <li>drools.workItemManagerFactory</li>
     * <li>drools.processSignalManagerFactory<li>
     * </ul>
     * @return SessionConfiguration
     */
    private static SessionConfiguration createSessionConfiguration() { 
        Properties properties = new Properties();
        properties.setProperty( "drools.commandService",
                                SingleSessionCommandService.class.getName() );
        properties.setProperty( "drools.processInstanceManagerFactory",
                                JPAProcessInstanceManagerFactory.class.getName() );
        properties.setProperty( "drools.workItemManagerFactory",
                                JPAWorkItemManagerFactory.class.getName() );
        properties.setProperty( "drools.processSignalManagerFactory",
                                JPASignalManagerFactory.class.getName() );
        properties.setProperty( "drools.timerService",
                                JpaJDKTimerService.class.getName() );
        SessionConfiguration config = new SessionConfiguration( properties );

        return config;
    }
   
    /**
     * Create an environment for use during process execution 
     *  with the appropriate EntityManagerFactory and TransactionManager values.
     * @return Environment
     */
    private Environment createEnvironment() { 
        Environment env = KnowledgeBaseFactory.newEnvironment();
        env.set( EnvironmentName.ENTITY_MANAGER_FACTORY, emf );
        env.set( EnvironmentName.TRANSACTION_MANAGER,
                 TransactionManagerServices.getTransactionManager() );
        return env;
    }
   
    /**
     * Start the test process (which has a processId of "org.drools.test.TestProcess" )
     * @param service A SingleSessionCommandService to execute the StartProcessCommand
     * @return the processInstance of the started process.
     */
    private static ProcessInstance startTestProcess(SingleSessionCommandService service) { 
        StartProcessCommand startProcessCommand = new StartProcessCommand();
        startProcessCommand.setProcessId( "org.drools.test.TestProcess" );
        ProcessInstance processInstance = service.execute( startProcessCommand );
        logger.info( "Started process instance " + processInstance.getId() );
        return processInstance;
    }
    
    /**
     * Retrieve a ProcessInstance using the GetProcessInstanceCommand 
     * @param processInstanceId the id the process to retrieve
     * @param service a SingleSessionCommandService object to execute the GetProcessInstanceCommand 
     * @return The requested ProcessInstance
     */
    private static ProcessInstance getProcessInstance( long processInstanceId, SingleSessionCommandService service) { 
        GetProcessInstanceCommand getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId( processInstanceId );
        return service.execute( getProcessInstanceCommand );
    }
  
    /**
     * Methods to test/make sure that a ProcessInstance is null
     */
    
    private static void testNullProcessInstance( final ProcessInstance processInstance, SingleSessionCommandService service) { 
        ProcessInstance nullProcessInstance = getProcessInstance(processInstance.getId(), service);
        assertNotNull( nullProcessInstance );
    }
    
    private SingleSessionCommandService testNullProcessInstance( final ProcessInstance processInstance, final int sessionId, final KnowledgeBase kbase) { 
        SingleSessionCommandService service 
            = new SingleSessionCommandService( sessionId, kbase, config, env );
        
        ProcessInstance nullProcessInstance = getProcessInstance(processInstance.getId(), service);
        assertNotNull( nullProcessInstance );
        return service;
    }
    
    private void testNullProcessInstanceAndDisposeService( final ProcessInstance processInstance, int sessionId, KnowledgeBase kbase) { 
        SingleSessionCommandService service = testNullProcessInstance(processInstance, sessionId, kbase);
        service.dispose();
    }
   
    
    private SingleSessionCommandService getServiceAndExecuteCompleteWorkItem( WorkItem workItem, int sessionId, KnowledgeBase kbase ) { 
        SingleSessionCommandService service = new SingleSessionCommandService( sessionId, kbase, config, env );
        executeCompleteWorkItem(workItem, service);
        return service;
    }
    
    /**
     * Methods to execute a workItem
     */
    
    private void executeCompleteWorkItem( WorkItem workItem, SingleSessionCommandService service ) { 
        CompleteWorkItemCommand completeWorkItemCommand = new CompleteWorkItemCommand();
        completeWorkItemCommand.setWorkItemId( workItem.getId() );
        service.execute( completeWorkItemCommand );
    }
   
    /**
     * The tests!
     */
    
    
    public void testPersistenceWorkItems() throws Exception {
        // setup: add the specific knowledge base that we want
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        Collection<KnowledgePackage> kpkgs = getProcessWorkItems();
        kbase.addKnowledgePackages( kpkgs );

        SingleSessionCommandService service = new SingleSessionCommandService( kbase, config, env );
        int sessionId = service.getSessionId();

        // start the test process
        ProcessInstance processInstance = startTestProcess(service);

        // test that the workItem we retrieve is NOT null
        TestWorkItemHandler handler = TestWorkItemHandler.getInstance();
        WorkItem workItem = handler.getWorkItem();
        assertNotNull( workItem );
        service.dispose();

        // test that the process instance is null
        testNullProcessInstanceAndDisposeService(processInstance, sessionId, kbase);
 
        // execute the workItem and test that the workItem is NOT null afterwards
        service = getServiceAndExecuteCompleteWorkItem(workItem, sessionId, kbase);
        workItem = handler.getWorkItem();
        assertNotNull( workItem );
        service.dispose();

        // test that the process instance is null
        testNullProcessInstanceAndDisposeService(processInstance, sessionId, kbase);

        // execute the workItem again and test that the workItem is NOT null afterwards
        service = getServiceAndExecuteCompleteWorkItem(workItem, sessionId, kbase);
        workItem = handler.getWorkItem();
        assertNotNull( workItem );
        service.dispose();

        // test that the process instance is null
        testNullProcessInstanceAndDisposeService(processInstance, sessionId, kbase);

        // execute the workItem again and test that the workItem is null afterwards
        service = getServiceAndExecuteCompleteWorkItem(workItem, sessionId, kbase);
        workItem = handler.getWorkItem();
        assertNull( workItem );
        service.dispose();

        // Test that the process instance is null

        service = new SingleSessionCommandService( sessionId, kbase, config, env );
        processInstance = getProcessInstance(processInstance.getId(), service);
        assertNull( processInstance );
    }

    public void testPersistenceWorkItemsUserTransaction() throws Exception {
        // setup: add the specific knowledge base that we want
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        Collection<KnowledgePackage> kpkgs = getProcessWorkItems();
        kbase.addKnowledgePackages( kpkgs );

        SingleSessionCommandService service = new SingleSessionCommandService( kbase, config, env );
        int sessionId = service.getSessionId();
        
        // start the test process
        UserTransaction ut = (UserTransaction) new InitialContext().lookup( "java:comp/UserTransaction" );
        ut.begin();
        ProcessInstance processInstance = startTestProcess(service);
        ut.commit();

        // make sure that the workItem is NOT null
        TestWorkItemHandler handler = TestWorkItemHandler.getInstance();
        WorkItem workItem = handler.getWorkItem();
        assertNotNull( workItem );
        service.dispose();

        // test that the process instance is null
        service = new SingleSessionCommandService( sessionId, kbase, config, env );
        ut.begin();
        testNullProcessInstance(processInstance, service);
        ut.commit();
        service.dispose();

        // execute the workItem again and test that the workItem is null afterwards
        service = new SingleSessionCommandService( sessionId, kbase, config, env );
        ut.begin();
        executeCompleteWorkItem(workItem, service);
        ut.commit();

        workItem = handler.getWorkItem();
        assertNotNull( workItem );
        service.dispose();

        // test that the process instance is null
        service = new SingleSessionCommandService( sessionId, kbase, config, env );
        ut.begin();
        processInstance = getProcessInstance(processInstance.getId(), service);
        ut.commit();
        assertNotNull( processInstance );
        service.dispose();

        // execute the workItem again and test that the workItem is null afterwards
        service = new SingleSessionCommandService( sessionId, kbase, config, env );
        ut.begin();
        executeCompleteWorkItem(workItem, service);
        ut.commit();

        workItem = handler.getWorkItem();
        assertNotNull( workItem );
        service.dispose();

        // test that the process instance is null
        service = new SingleSessionCommandService( sessionId, kbase, config, env );
        ut.begin();
        processInstance = getProcessInstance(processInstance.getId(), service);
        ut.commit();
        assertNotNull( processInstance );
        service.dispose();

        // execute the workItem again and test that the workItem is null afterwards
        service = new SingleSessionCommandService( sessionId, kbase, config, env );
        ut.begin();
        executeCompleteWorkItem(workItem, service);
        ut.commit();

        workItem = handler.getWorkItem();
        assertNull( workItem );
        service.dispose();

        // test that the process instance is null
        service = new SingleSessionCommandService( sessionId, kbase, config, env );
        ut.begin();
        processInstance = getProcessInstance(processInstance.getId(), service);
        ut.commit();
        assertNull( processInstance );
        service.dispose();
    }

    @SuppressWarnings("unused")
	private Collection<KnowledgePackage> getProcessWorkItems() {
        RuleFlowProcess process = new RuleFlowProcess();
        process.setId( "org.drools.test.TestProcess" );
        process.setName( "TestProcess" );
        process.setPackageName( "org.drools.test" );
        StartNode start = new StartNode();
        start.setId( 1 );
        start.setName( "Start" );
        process.addNode( start );
        ActionNode actionNode = new ActionNode();
        actionNode.setId( 2 );
        actionNode.setName( "Action" );
        DroolsConsequenceAction action = new DroolsConsequenceAction();
        action.setDialect( "java" );
        action.setConsequence( "System.out.println(\"Executed action\");" );
        actionNode.setAction( action );
        process.addNode( actionNode );
        new ConnectionImpl( start,
                            Node.CONNECTION_DEFAULT_TYPE,
                            actionNode,
                            Node.CONNECTION_DEFAULT_TYPE );
        WorkItemNode workItemNode = new WorkItemNode();
        workItemNode.setId( 3 );
        workItemNode.setName( "WorkItem1" );
        Work work = new WorkImpl();
        work.setName( "MyWork" );
        workItemNode.setWork( work );
        process.addNode( workItemNode );
        new ConnectionImpl( actionNode,
                            Node.CONNECTION_DEFAULT_TYPE,
                            workItemNode,
                            Node.CONNECTION_DEFAULT_TYPE );
        WorkItemNode workItemNode2 = new WorkItemNode();
        workItemNode2.setId( 4 );
        workItemNode2.setName( "WorkItem2" );
        work = new WorkImpl();
        work.setName( "MyWork" );
        workItemNode2.setWork( work );
        process.addNode( workItemNode2 );
        new ConnectionImpl( workItemNode,
                            Node.CONNECTION_DEFAULT_TYPE,
                            workItemNode2,
                            Node.CONNECTION_DEFAULT_TYPE );
        WorkItemNode workItemNode3 = new WorkItemNode();
        workItemNode3.setId( 5 );
        workItemNode3.setName( "WorkItem3" );
        work = new WorkImpl();
        work.setName( "MyWork" );
        workItemNode3.setWork( work );
        process.addNode( workItemNode3 );
        new ConnectionImpl( workItemNode2,
                            Node.CONNECTION_DEFAULT_TYPE,
                            workItemNode3,
                            Node.CONNECTION_DEFAULT_TYPE );
        EndNode end = new EndNode();
        end.setId( 6 );
        end.setName( "End" );
        process.addNode( end );
        new ConnectionImpl( workItemNode3,
                            Node.CONNECTION_DEFAULT_TYPE,
                            end,
                            Node.CONNECTION_DEFAULT_TYPE );

        PackageBuilder packageBuilder = new PackageBuilder();
        ProcessBuilderImpl processBuilder = new ProcessBuilderImpl( packageBuilder );
        processBuilder.buildProcess( process,
                                     null );
        List<KnowledgePackage> list = new ArrayList<KnowledgePackage>();
        list.add( new KnowledgePackageImp( packageBuilder.getPackage() ) );
        return list;
    }

    public void testPersistenceSubProcess() {

        // setup: add the specific knowledge base that we want
        RuleBase ruleBase = RuleBaseFactory.newRuleBase();
        Package pkg = getProcessSubProcess();
        ruleBase.addPackage( pkg );

        SingleSessionCommandService service = new SingleSessionCommandService( ruleBase, config, env );
        int sessionId = service.getSessionId();
        
        // start the test process
        RuleFlowProcessInstance processInstance = (RuleFlowProcessInstance) startTestProcess(service);

        // make sure that the workItem is NOT null
        TestWorkItemHandler handler = TestWorkItemHandler.getInstance();
        WorkItem workItem = handler.getWorkItem();
        assertNotNull( workItem );
        service.dispose();

        // test that the process instance is null
        service = new SingleSessionCommandService( sessionId, ruleBase, config, env );
        testNullProcessInstance(processInstance, service);

        Collection<NodeInstance> nodeInstances = processInstance.getNodeInstances();
        assertEquals( 1, nodeInstances.size() );
        
        // test that the subProcessInstance is null
        SubProcessNodeInstance subProcessNodeInstance = (SubProcessNodeInstance) nodeInstances.iterator().next();
        RuleFlowProcessInstance subProcessInstance = (RuleFlowProcessInstance) getProcessInstance(subProcessNodeInstance.getProcessInstanceId(), service);
        assertNotNull( subProcessInstance );
        service.dispose();

        // execute the workItem
        service = new SingleSessionCommandService( sessionId, ruleBase, config, env );
        executeCompleteWorkItem(workItem, service);
        service.dispose();

        // test that the subProcessInstance is null
        service = new SingleSessionCommandService( sessionId, ruleBase, config, env );
        subProcessInstance = (RuleFlowProcessInstance) getProcessInstance(subProcessInstance.getId(), service);
        assertNull( subProcessInstance );

        // test that the process instance is null
        processInstance = (RuleFlowProcessInstance) getProcessInstance(processInstance.getId(), service);
        assertNull( processInstance );
        service.dispose();
    }

    @SuppressWarnings("unused")
	private Package getProcessSubProcess() {
        RuleFlowProcess process = new RuleFlowProcess();
        process.setId( "org.drools.test.TestProcess" );
        process.setName( "TestProcess" );
        process.setPackageName( "org.drools.test" );
        StartNode start = new StartNode();
        start.setId( 1 );
        start.setName( "Start" );
        process.addNode( start );
        ActionNode actionNode = new ActionNode();
        actionNode.setId( 2 );
        actionNode.setName( "Action" );
        DroolsConsequenceAction action = new DroolsConsequenceAction();
        action.setDialect( "java" );
        action.setConsequence( "System.out.println(\"Executed action\");" );
        actionNode.setAction( action );
        process.addNode( actionNode );
        new ConnectionImpl( start,
                            Node.CONNECTION_DEFAULT_TYPE,
                            actionNode,
                            Node.CONNECTION_DEFAULT_TYPE );
        SubProcessNode subProcessNode = new SubProcessNode();
        subProcessNode.setId( 3 );
        subProcessNode.setName( "SubProcess" );
        subProcessNode.setProcessId( "org.drools.test.SubProcess" );
        process.addNode( subProcessNode );
        new ConnectionImpl( actionNode,
                            Node.CONNECTION_DEFAULT_TYPE,
                            subProcessNode,
                            Node.CONNECTION_DEFAULT_TYPE );
        EndNode end = new EndNode();
        end.setId( 4 );
        end.setName( "End" );
        process.addNode( end );
        new ConnectionImpl( subProcessNode,
                            Node.CONNECTION_DEFAULT_TYPE,
                            end,
                            Node.CONNECTION_DEFAULT_TYPE );

        PackageBuilder packageBuilder = new PackageBuilder();
        ProcessBuilderImpl processBuilder = new ProcessBuilderImpl( packageBuilder );
        processBuilder.buildProcess( process,
                                     null );

        process = new RuleFlowProcess();
        process.setId( "org.drools.test.SubProcess" );
        process.setName( "SubProcess" );
        process.setPackageName( "org.drools.test" );
        start = new StartNode();
        start.setId( 1 );
        start.setName( "Start" );
        process.addNode( start );
        actionNode = new ActionNode();
        actionNode.setId( 2 );
        actionNode.setName( "Action" );
        action = new DroolsConsequenceAction();
        action.setDialect( "java" );
        action.setConsequence( "System.out.println(\"Executed action\");" );
        actionNode.setAction( action );
        process.addNode( actionNode );
        new ConnectionImpl( start,
                            Node.CONNECTION_DEFAULT_TYPE,
                            actionNode,
                            Node.CONNECTION_DEFAULT_TYPE );
        WorkItemNode workItemNode = new WorkItemNode();
        workItemNode.setId( 3 );
        workItemNode.setName( "WorkItem1" );
        Work work = new WorkImpl();
        work.setName( "MyWork" );
        workItemNode.setWork( work );
        process.addNode( workItemNode );
        new ConnectionImpl( actionNode,
                            Node.CONNECTION_DEFAULT_TYPE,
                            workItemNode,
                            Node.CONNECTION_DEFAULT_TYPE );
        end = new EndNode();
        end.setId( 6 );
        end.setName( "End" );
        process.addNode( end );
        new ConnectionImpl( workItemNode,
                            Node.CONNECTION_DEFAULT_TYPE,
                            end,
                            Node.CONNECTION_DEFAULT_TYPE );

        processBuilder.buildProcess( process,
                                     null );
        return packageBuilder.getPackage();
    }

    public void testPersistenceTimer() throws Exception {
        // setup: add the specific knowledge base that we want
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        Collection<KnowledgePackage> kpkgs = getProcessTimer();
        kbase.addKnowledgePackages( kpkgs );

        SingleSessionCommandService service = new SingleSessionCommandService( kbase, config, env );
        int sessionId = service.getSessionId();
        
        // start the test process
        ProcessInstance processInstance = startTestProcess(service);
        service.dispose();

        // test that the process instance is null
        testNullProcessInstanceAndDisposeService(processInstance, sessionId, kbase);
       
        // wait for the timer to complete
        service = new SingleSessionCommandService( sessionId, kbase, config, env );
        
        Thread.sleep( 3000 );

        // test that the process instance is null
        processInstance = getProcessInstance(processInstance.getId(), service);
        assertNull( processInstance );
        service.dispose();
    }

    @SuppressWarnings("unused")
	private List<KnowledgePackage> getProcessTimer() {
        RuleFlowProcess process = new RuleFlowProcess();
        process.setId( "org.drools.test.TestProcess" );
        process.setName( "TestProcess" );
        process.setPackageName( "org.drools.test" );
        StartNode start = new StartNode();
        start.setId( 1 );
        start.setName( "Start" );
        process.addNode( start );
        TimerNode timerNode = new TimerNode();
        timerNode.setId( 2 );
        timerNode.setName( "Timer" );
        Timer timer = new Timer();
        timer.setDelay( "2000" );
        timerNode.setTimer( timer );
        process.addNode( timerNode );
        new ConnectionImpl( start,
                            Node.CONNECTION_DEFAULT_TYPE,
                            timerNode,
                            Node.CONNECTION_DEFAULT_TYPE );
        ActionNode actionNode = new ActionNode();
        actionNode.setId( 3 );
        actionNode.setName( "Action" );
        DroolsConsequenceAction action = new DroolsConsequenceAction();
        action.setDialect( "java" );
        action.setConsequence( "System.out.println(\"Executed action\");" );
        actionNode.setAction( action );
        process.addNode( actionNode );
        new ConnectionImpl( timerNode,
                            Node.CONNECTION_DEFAULT_TYPE,
                            actionNode,
                            Node.CONNECTION_DEFAULT_TYPE );
        EndNode end = new EndNode();
        end.setId( 6 );
        end.setName( "End" );
        process.addNode( end );
        new ConnectionImpl( actionNode,
                            Node.CONNECTION_DEFAULT_TYPE,
                            end,
                            Node.CONNECTION_DEFAULT_TYPE );

        PackageBuilder packageBuilder = new PackageBuilder();
        ProcessBuilderImpl processBuilder = new ProcessBuilderImpl( packageBuilder );
        processBuilder.buildProcess( process,
                                     null );
        List<KnowledgePackage> list = new ArrayList<KnowledgePackage>();
        list.add( new KnowledgePackageImp( packageBuilder.getPackage() ) );
        return list;
    }

    public void testPersistenceTimer2() throws Exception {
        // setup: add the specific knowledge base that we want
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        Collection<KnowledgePackage> kpkgs = getProcessTimer2();
        kbase.addKnowledgePackages( kpkgs );

        SingleSessionCommandService service = new SingleSessionCommandService( kbase, config, env );
        int sessionId = service.getSessionId();
        
        // start the test process 
        ProcessInstance processInstance = startTestProcess(service);

        // Wait for the timer to finish
        Thread.sleep( 2000 );

        // test that the process instance is null
        service = new SingleSessionCommandService( sessionId, kbase, config, env );
        processInstance = getProcessInstance(processInstance.getId(), service);
        assertNull( processInstance );
    }

    @SuppressWarnings("unused")
	private List<KnowledgePackage> getProcessTimer2() {
        RuleFlowProcess process = new RuleFlowProcess();
        process.setId( "org.drools.test.TestProcess" );
        process.setName( "TestProcess" );
        process.setPackageName( "org.drools.test" );
        StartNode start = new StartNode();
        start.setId( 1 );
        start.setName( "Start" );
        process.addNode( start );
        TimerNode timerNode = new TimerNode();
        timerNode.setId( 2 );
        timerNode.setName( "Timer" );
        Timer timer = new Timer();
        timer.setDelay( "0" );
        timerNode.setTimer( timer );
        process.addNode( timerNode );
        new ConnectionImpl( start,
                            Node.CONNECTION_DEFAULT_TYPE,
                            timerNode,
                            Node.CONNECTION_DEFAULT_TYPE );
        ActionNode actionNode = new ActionNode();
        actionNode.setId( 3 );
        actionNode.setName( "Action" );
        DroolsConsequenceAction action = new DroolsConsequenceAction();
        action.setDialect( "java" );
        action.setConsequence( "try { Thread.sleep(1000); } catch (Throwable t) {} System.out.println(\"Executed action\");" );
        actionNode.setAction( action );
        process.addNode( actionNode );
        new ConnectionImpl( timerNode,
                            Node.CONNECTION_DEFAULT_TYPE,
                            actionNode,
                            Node.CONNECTION_DEFAULT_TYPE );
        EndNode end = new EndNode();
        end.setId( 6 );
        end.setName( "End" );
        process.addNode( end );
        new ConnectionImpl( actionNode,
                            Node.CONNECTION_DEFAULT_TYPE,
                            end,
                            Node.CONNECTION_DEFAULT_TYPE );

        PackageBuilder packageBuilder = new PackageBuilder();
        ProcessBuilderImpl processBuilder = new ProcessBuilderImpl( packageBuilder );
        processBuilder.buildProcess( process,
                                     null );
        List<KnowledgePackage> list = new ArrayList<KnowledgePackage>();
        list.add( new KnowledgePackageImp( packageBuilder.getPackage() ) );
        return list;
    }

}
