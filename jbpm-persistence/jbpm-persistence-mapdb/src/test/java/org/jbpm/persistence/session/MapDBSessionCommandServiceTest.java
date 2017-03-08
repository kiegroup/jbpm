/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.jbpm.persistence.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.transaction.UserTransaction;

import org.drools.compiler.builder.impl.KnowledgeBuilderImpl;
import org.drools.core.SessionConfiguration;
import org.drools.core.TimerJobFactoryType;
import org.drools.core.command.runtime.process.CompleteWorkItemCommand;
import org.drools.core.command.runtime.process.GetProcessInstanceCommand;
import org.drools.core.command.runtime.process.RegisterWorkItemHandlerCommand;
import org.drools.core.command.runtime.process.StartProcessCommand;
import org.drools.core.definitions.InternalKnowledgePackage;
import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.core.process.core.Work;
import org.drools.core.process.core.impl.WorkImpl;
import org.drools.persistence.mapdb.MapDBJDKTimerService;
import org.drools.persistence.mapdb.MapDBSessionCommandService;
import org.drools.persistence.processinstance.mapdb.MapDBWorkItemManagerFactory;
import org.jbpm.compiler.ProcessBuilderImpl;
import org.jbpm.persistence.mapdb.MapDBProcessInstanceManagerFactory;
import org.jbpm.persistence.mapdb.MapDBSignalManagerFactory;
import org.jbpm.persistence.mapdb.util.MapDBProcessPersistenceUtil;
import org.jbpm.process.core.timer.Timer;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.ruleflow.instance.RuleFlowProcessInstance;
import org.jbpm.test.util.AbstractBaseTest;
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
import org.junit.After;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.conf.TimerJobFactoryOption;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItem;
import org.kie.internal.KnowledgeBaseFactory;
import org.kie.internal.definition.KnowledgePackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapDBSessionCommandServiceTest extends AbstractBaseTest {
    
    private static final Logger logger = LoggerFactory.getLogger(MapDBSessionCommandServiceTest.class);

	private HashMap<String, Object> context;
	private Environment env;
    
    public void setUp() {
        context = MapDBProcessPersistenceUtil.setupMapDB();
        env = MapDBProcessPersistenceUtil.createEnvironment(context);
    }

    @After
    public void tearDown() {
    	MapDBProcessPersistenceUtil.cleanUp(context);
    }

    @Test
    public void testPersistenceWorkItems() throws Exception {
        setUp();
        
        KieBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        Collection<InternalKnowledgePackage> kpkgs = getProcessWorkItems();
        ((InternalKnowledgeBase) kbase).addPackages( kpkgs );

        Properties properties = new Properties();
        properties.setProperty( "drools.commandService",
        		MapDBSessionCommandService.class.getName() );
        properties.setProperty( "drools.processInstanceManagerFactory",
        		MapDBProcessInstanceManagerFactory.class.getName() );
        properties.setProperty( "drools.workItemManagerFactory",
        		MapDBWorkItemManagerFactory.class.getName() );
        properties.setProperty( "drools.processSignalManagerFactory",
        		MapDBSignalManagerFactory.class.getName() );
        properties.setProperty( "drools.timerService",
        		MapDBJDKTimerService.class.getName() );
        SessionConfiguration config = SessionConfiguration.newInstance( properties );

        TestWorkItemHandler handler = TestWorkItemHandler.getInstance();
        MapDBSessionCommandService service = new MapDBSessionCommandService( kbase,
                                                                               config,
                                                                               env );
        RegisterWorkItemHandlerCommand regCommand = new RegisterWorkItemHandlerCommand();
        regCommand.setWorkItemName( "MyWork" );
        regCommand.setHandler( handler );
        service.execute( regCommand );
        Long sessionId = service.getSessionId();

        StartProcessCommand startProcessCommand = new StartProcessCommand();
        startProcessCommand.setProcessId( "org.drools.test.TestProcess" );
        ProcessInstance processInstance = service.execute( startProcessCommand );
        logger.info( "Started process instance {}", processInstance.getId() );

        WorkItem workItem = handler.getWorkItem();
        assertNotNull( workItem );
        service.dispose();

        service = new MapDBSessionCommandService( sessionId,
                                                   kbase,
                                                   config,
                                                   env );
        service.execute( regCommand );
        GetProcessInstanceCommand getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId( processInstance.getId() );
        processInstance = service.execute( getProcessInstanceCommand );
        assertNotNull( processInstance );
        service.dispose();

        service = new MapDBSessionCommandService( sessionId,
                                                   kbase,
                                                   config,
                                                   env );
        service.execute( regCommand );
        CompleteWorkItemCommand completeWorkItemCommand = new CompleteWorkItemCommand();
        completeWorkItemCommand.setWorkItemId( workItem.getId() );
        service.execute( completeWorkItemCommand );

        workItem = handler.getWorkItem();
        assertNotNull( workItem );
        service.dispose();

        service = new MapDBSessionCommandService( sessionId,
                                                   kbase,
                                                   config,
                                                   env );
        service.execute( regCommand );
        getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId( processInstance.getId() );
        processInstance = service.execute( getProcessInstanceCommand );
        assertNotNull( processInstance );
        service.dispose();

        service = new MapDBSessionCommandService( sessionId,
                                                   kbase,
                                                   config,
                                                   env );
        service.execute( regCommand );
        completeWorkItemCommand = new CompleteWorkItemCommand();
        completeWorkItemCommand.setWorkItemId( workItem.getId() );
        service.execute( completeWorkItemCommand );

        workItem = handler.getWorkItem();
        assertNotNull( workItem );
        service.dispose();

        service = new MapDBSessionCommandService( sessionId,
                                                   kbase,
                                                   config,
                                                   env );
        service.execute( regCommand );
        getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId( processInstance.getId() );
        processInstance = service.execute( getProcessInstanceCommand );
        assertNotNull( processInstance );
        service.dispose();

        service = new MapDBSessionCommandService( sessionId,
                                                   kbase,
                                                   config,
                                                   env );
        service.execute( regCommand );
        completeWorkItemCommand = new CompleteWorkItemCommand();
        completeWorkItemCommand.setWorkItemId( workItem.getId() );
        service.execute( completeWorkItemCommand );

        workItem = handler.getWorkItem();
        assertNull( workItem );
        service.dispose();

        service = new MapDBSessionCommandService( sessionId,
                                                   kbase,
                                                   config,
                                                   env );
        service.execute( regCommand );
        getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId( processInstance.getId() );
        processInstance = service.execute( getProcessInstanceCommand );
        assertNull( processInstance );
        service.dispose();
    }
    
    @Test
    
    public void testPersistenceWorkItemsUserTransaction() throws Exception {
        setUp();
        
        KieBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        Collection<InternalKnowledgePackage> kpkgs = getProcessWorkItems();
        ((InternalKnowledgeBase) kbase).addPackages( kpkgs );

        Properties properties = new Properties();
        properties.setProperty( "drools.commandService",
        		MapDBSessionCommandService.class.getName() );
        properties.setProperty( "drools.processInstanceManagerFactory",
        		MapDBProcessInstanceManagerFactory.class.getName() );
        properties.setProperty( "drools.workItemManagerFactory",
        		MapDBWorkItemManagerFactory.class.getName() );
        properties.setProperty( "drools.processSignalManagerFactory",
        		MapDBSignalManagerFactory.class.getName() );
        properties.setProperty( "drools.timerService",
        		MapDBJDKTimerService.class.getName() );
        SessionConfiguration config = SessionConfiguration.newInstance( properties );

        TestWorkItemHandler handler = TestWorkItemHandler.getInstance();
        MapDBSessionCommandService service = new MapDBSessionCommandService( kbase,
                                                                               config,
                                                                               env );
        service.getKieSession().getWorkItemManager().registerWorkItemHandler("MyWork", handler);
        Long sessionId = service.getSessionId();

        UserTransaction ut = (UserTransaction) new InitialContext().lookup( "java:comp/UserTransaction" );
        ut.begin();
        StartProcessCommand startProcessCommand = new StartProcessCommand();
        startProcessCommand.setProcessId( "org.drools.test.TestProcess" );
        ProcessInstance processInstance = service.execute( startProcessCommand );
        logger.info( "Started process instance {}", processInstance.getId() );
        ut.commit();

        WorkItem workItem = handler.getWorkItem();
        assertNotNull( workItem );
        service.dispose();

        service = new MapDBSessionCommandService( sessionId,
                                                   kbase,
                                                   config,
                                                   env );
        service.getKieSession().getWorkItemManager().registerWorkItemHandler("MyWork", handler);
        ut.begin();
        GetProcessInstanceCommand getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId( processInstance.getId() );
        processInstance = service.execute( getProcessInstanceCommand );
        assertNotNull( processInstance );
        ut.commit();
        service.dispose();

        service = new MapDBSessionCommandService( sessionId,
                                                   kbase,
                                                   config,
                                                   env );
        RegisterWorkItemHandlerCommand regCommand = new RegisterWorkItemHandlerCommand();
        regCommand.setWorkItemName( "MyWork" );
        regCommand.setHandler( handler );
        service.execute( regCommand );
        ut.begin();
        CompleteWorkItemCommand completeWorkItemCommand = new CompleteWorkItemCommand();
        completeWorkItemCommand.setWorkItemId( workItem.getId() );
        service.execute( completeWorkItemCommand );
        ut.commit();

        workItem = handler.getWorkItem();
        assertNotNull( workItem );
        service.dispose();

        service = new MapDBSessionCommandService( sessionId,
                                                   kbase,
                                                   config,
                                                   env );
        service.execute( regCommand );
        ut.begin();
        getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId( processInstance.getId() );
        processInstance = service.execute( getProcessInstanceCommand );
        ut.commit();
        assertNotNull( processInstance );
        service.dispose();

        service = new MapDBSessionCommandService( sessionId,
                                                   kbase,
                                                   config,
                                                   env );
        service.execute( regCommand );
        ut.begin();
        completeWorkItemCommand = new CompleteWorkItemCommand();
        completeWorkItemCommand.setWorkItemId( workItem.getId() );
        service.execute( completeWorkItemCommand );
        ut.commit();

        workItem = handler.getWorkItem();
        assertNotNull( workItem );
        service.dispose();

        service = new MapDBSessionCommandService( sessionId,
                                                   kbase,
                                                   config,
                                                   env );
        service.execute( regCommand );
        ut.begin();
        getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId( processInstance.getId() );
        processInstance = service.execute( getProcessInstanceCommand );
        ut.commit();
        assertNotNull( processInstance );
        service.dispose();

        service = new MapDBSessionCommandService( sessionId,
                                                   kbase,
                                                   config,
                                                   env );
        service.execute( regCommand );
        ut.begin();
        completeWorkItemCommand = new CompleteWorkItemCommand();
        completeWorkItemCommand.setWorkItemId( workItem.getId() );
        service.execute( completeWorkItemCommand );
        ut.commit();

        workItem = handler.getWorkItem();
        assertNull( workItem );
        service.dispose();

        service = new MapDBSessionCommandService( sessionId,
                                                   kbase,
                                                   config,
                                                   env );
        service.execute( regCommand );
        ut.begin();
        getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId( processInstance.getId() );
        processInstance = service.execute( getProcessInstanceCommand );
        ut.commit();
        assertNull( processInstance );
        service.dispose();
    }

	private Collection<InternalKnowledgePackage> getProcessWorkItems() {
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

        KnowledgeBuilderImpl packageBuilder = new KnowledgeBuilderImpl();
        ProcessBuilderImpl processBuilder = new ProcessBuilderImpl( packageBuilder );
        processBuilder.buildProcess( process,
                                     null );
        List<InternalKnowledgePackage> list = new ArrayList<>();
        for (KnowledgePackage kpkg : packageBuilder.getKnowledgePackages()) {
        	list.add( (InternalKnowledgePackage) kpkg );
        }
        return list;
    }

    @Test
    public void testPersistenceSubProcess() {
        setUp();
        
        Properties properties = new Properties();
        properties.setProperty( "drools.commandService",
        		MapDBSessionCommandService.class.getName() );
        properties.setProperty( "drools.processInstanceManagerFactory",
        		MapDBProcessInstanceManagerFactory.class.getName() );
        properties.setProperty( "drools.workItemManagerFactory",
        		MapDBWorkItemManagerFactory.class.getName() );
        properties.setProperty( "drools.processSignalManagerFactory",
        		MapDBSignalManagerFactory.class.getName() );
        properties.setProperty( "drools.timerService",
        		MapDBJDKTimerService.class.getName() );
        SessionConfiguration config = SessionConfiguration.newInstance( properties );
        config.setOption(TimerJobFactoryOption.get("mapdb"));

        KieBase ruleBase = KnowledgeBaseFactory.newKnowledgeBase();
        InternalKnowledgePackage pkg = getProcessSubProcess();
        ((InternalKnowledgeBase) ruleBase).addPackages( Arrays.asList(pkg) );

        MapDBSessionCommandService service = new MapDBSessionCommandService( ruleBase,
                                                                               config,
                                                                               env );
        RegisterWorkItemHandlerCommand regCommand = new RegisterWorkItemHandlerCommand();
        regCommand.setHandler(TestWorkItemHandler.getInstance());
        regCommand.setWorkItemName("MyWork");
        service.execute(regCommand);
        Long sessionId = service.getSessionId();
        StartProcessCommand startProcessCommand = new StartProcessCommand();
        startProcessCommand.setProcessId( "org.drools.test.TestProcess" );
        RuleFlowProcessInstance processInstance = (RuleFlowProcessInstance) service.execute( startProcessCommand );
        logger.info( "Started process instance {}", processInstance.getId() );
        long processInstanceId = processInstance.getId();

        TestWorkItemHandler handler = TestWorkItemHandler.getInstance();
        WorkItem workItem = handler.getWorkItem();
        assertNotNull( workItem );
        service.dispose();

        service = new MapDBSessionCommandService( sessionId,
        		                                   ruleBase,
                                                   config,
                                                   env );
        service.execute(regCommand);
        GetProcessInstanceCommand getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId( processInstanceId );
        processInstance = (RuleFlowProcessInstance) service.execute( getProcessInstanceCommand );
        assertNotNull( processInstance );

        Collection<NodeInstance> nodeInstances = processInstance.getNodeInstances();
        assertEquals( 1, nodeInstances.size() );
        SubProcessNodeInstance subProcessNodeInstance = (SubProcessNodeInstance) nodeInstances.iterator().next();
        long subProcessInstanceId = subProcessNodeInstance.getProcessInstanceId();
        getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId( subProcessInstanceId );
        RuleFlowProcessInstance subProcessInstance = (RuleFlowProcessInstance) service.execute( getProcessInstanceCommand );
        assertNotNull( subProcessInstance );
        service.dispose();

        service = new MapDBSessionCommandService( sessionId,
                                                   ruleBase,
                                                   config,
                                                   env );
        service.execute(regCommand);
        CompleteWorkItemCommand completeWorkItemCommand = new CompleteWorkItemCommand();
        completeWorkItemCommand.setWorkItemId( workItem.getId() );
        service.execute( completeWorkItemCommand );
        service.dispose();

        service = new MapDBSessionCommandService( sessionId,
                                                   ruleBase,
                                                   config,
                                                   env );
        service.execute(regCommand);
        getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId( subProcessInstanceId );
        subProcessInstance = (RuleFlowProcessInstance) service.execute( getProcessInstanceCommand );
        assertNull( subProcessInstance );

        getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId( processInstanceId );
        processInstance = (RuleFlowProcessInstance) service.execute( getProcessInstanceCommand );
        assertNull( processInstance );
        service.dispose();
    }

	private InternalKnowledgePackage getProcessSubProcess() {
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

        KnowledgeBuilderImpl packageBuilder = new KnowledgeBuilderImpl();
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

    @Test
    public void testPersistenceTimer() throws Exception {
        setUp();
        
        Properties properties = new Properties();
        properties.setProperty( "drools.commandService",
        		MapDBSessionCommandService.class.getName() );
        properties.setProperty( "drools.processInstanceManagerFactory",
        		MapDBProcessInstanceManagerFactory.class.getName() );
        properties.setProperty( "drools.workItemManagerFactory",
        		MapDBWorkItemManagerFactory.class.getName() );
        properties.setProperty( "drools.processSignalManagerFactory",
        		MapDBSignalManagerFactory.class.getName() );
        
        SessionConfiguration config = SessionConfiguration.newInstance( properties );
        config.setOption( TimerJobFactoryOption.get(TimerJobFactoryType.MAPDB.getId()) );

        KieBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        Collection<KnowledgePackage> kpkgs = getProcessTimer();
        ((InternalKnowledgeBase) kbase).addKnowledgePackages(kpkgs);;

        MapDBSessionCommandService service = new MapDBSessionCommandService( kbase,
                                                                               config,
                                                                               env );
        Long sessionId = service.getSessionId();
        StartProcessCommand startProcessCommand = new StartProcessCommand();
        startProcessCommand.setProcessId( "org.drools.test.TestProcess" );
        ProcessInstance processInstance = service.execute( startProcessCommand );
        logger.info( "Started process instance {}", processInstance.getId() );
        
        
        Thread.sleep( 500 );
        service.dispose();

        service = new MapDBSessionCommandService( sessionId,
                                                   kbase,
                                                   config,
                                                   env );
        GetProcessInstanceCommand getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId( processInstance.getId() );
        processInstance = service.execute( getProcessInstanceCommand );
        assertNotNull( processInstance );
        service.dispose();

        service = new MapDBSessionCommandService( sessionId,
                                                   kbase,
                                                   config,
                                                   env );
        Thread.sleep( 5000 );
        getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId( processInstance.getId() );
        processInstance = service.execute( getProcessInstanceCommand );
        assertNull( processInstance );
    }

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

        KnowledgeBuilderImpl packageBuilder = new KnowledgeBuilderImpl();
        ProcessBuilderImpl processBuilder = new ProcessBuilderImpl( packageBuilder );
        processBuilder.buildProcess( process,
                                     null );
        List<KnowledgePackage> list = new ArrayList<>();
        list.add( packageBuilder.getPackage() );
        return list;
    }

    @Test
    public void testPersistenceTimer2() throws Exception {
        setUp();
        
        Properties properties = new Properties();
        properties.setProperty( "drools.commandService",
        		MapDBSessionCommandService.class.getName() );
        properties.setProperty( "drools.processInstanceManagerFactory",
        		MapDBProcessInstanceManagerFactory.class.getName() );
        properties.setProperty( "drools.workItemManagerFactory",
        		MapDBWorkItemManagerFactory.class.getName() );
        properties.setProperty( "drools.processSignalManagerFactory",
        		MapDBSignalManagerFactory.class.getName() );
        properties.setProperty( "drools.timerService", 
        		MapDBJDKTimerService.class.getName() );

        SessionConfiguration config = SessionConfiguration.newInstance( properties );
        config.setOption( TimerJobFactoryOption.get("mapdb") );
        
        KieBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        Collection<KnowledgePackage> kpkgs = getProcessTimer2();
        ((InternalKnowledgeBase) kbase).addKnowledgePackages( kpkgs );

        MapDBSessionCommandService service = new MapDBSessionCommandService( kbase,
                                                                               config,
                                                                               env );
        Long sessionId = service.getSessionId();
        StartProcessCommand startProcessCommand = new StartProcessCommand();
        startProcessCommand.setProcessId( "org.drools.test.TestProcess" );
        ProcessInstance processInstance = service.execute( startProcessCommand );
        logger.info( "Started process instance {}", processInstance.getId() );
        Thread.sleep( 2000 );

        service = new MapDBSessionCommandService( sessionId,
                                                   kbase,
                                                   config,
                                                   env );
        GetProcessInstanceCommand getProcessInstanceCommand = new GetProcessInstanceCommand();
        getProcessInstanceCommand.setProcessInstanceId( processInstance.getId() );
        processInstance = service.execute( getProcessInstanceCommand );
        assertNull( processInstance );
    }

	private Collection<KnowledgePackage> getProcessTimer2() {
        RuleFlowProcess process = new RuleFlowProcess();
        process.setId( "org.drools.test.TestProcess" );
        process.setName( "TestProcess" );
        process.setPackageName( "org.drools.test" );
        StartNode start = new StartNode();
        start.setId( 1 );
        start.setName( "Start" );
        process.addNode( start );
        ActionNode actionNode0 = new ActionNode();
        actionNode0.setId( 4 );
        actionNode0.setName("Action prev");
        DroolsConsequenceAction action0 = new DroolsConsequenceAction();
        action0.setDialect("java");;
        action0.setConsequence("System.out.println(\"Started process!!\");");
        actionNode0.setAction(action0);
        process.addNode( actionNode0 );
        new ConnectionImpl(start, Node.CONNECTION_DEFAULT_TYPE, 
        		actionNode0, Node.CONNECTION_DEFAULT_TYPE);
        TimerNode timerNode = new TimerNode();
        timerNode.setId( 2 );
        timerNode.setName( "Timer" );
        Timer timer = new Timer();
        timer.setDelay( "0" );
        timerNode.setTimer( timer );
        process.addNode( timerNode );
        new ConnectionImpl( actionNode0, Node.CONNECTION_DEFAULT_TYPE,
        		timerNode, Node.CONNECTION_DEFAULT_TYPE );
        ActionNode actionNode = new ActionNode();
        actionNode.setId( 3 );
        actionNode.setName( "Action" );
        DroolsConsequenceAction action = new DroolsConsequenceAction();
        action.setDialect( "java" );
        action.setConsequence( "try { Thread.sleep(1000); } catch (Throwable t) {} System.out.println(\"Executed action\");" );
        actionNode.setAction( action );
        process.addNode( actionNode );
        new ConnectionImpl( timerNode, Node.CONNECTION_DEFAULT_TYPE, 
        		actionNode, Node.CONNECTION_DEFAULT_TYPE );
        EndNode end = new EndNode();
        end.setId( 6 );
        end.setName( "End" );
        process.addNode( end );
        new ConnectionImpl( actionNode, Node.CONNECTION_DEFAULT_TYPE,
        		end, Node.CONNECTION_DEFAULT_TYPE );

        KnowledgeBuilderImpl packageBuilder = new KnowledgeBuilderImpl();
        ProcessBuilderImpl processBuilder = new ProcessBuilderImpl( packageBuilder );
        processBuilder.buildProcess( process, null );
        return packageBuilder.getKnowledgePackages();
    }

}
