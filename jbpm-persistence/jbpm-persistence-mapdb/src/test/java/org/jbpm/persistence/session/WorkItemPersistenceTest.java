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
import static org.junit.Assert.assertTrue;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.drools.compiler.Person;
import org.drools.core.WorkItemHandlerNotFoundException;
import org.drools.core.impl.KnowledgeBaseImpl;
import org.drools.core.process.core.ParameterDefinition;
import org.drools.core.process.core.Work;
import org.drools.core.process.core.datatype.impl.type.IntegerDataType;
import org.drools.core.process.core.datatype.impl.type.ObjectDataType;
import org.drools.core.process.core.datatype.impl.type.StringDataType;
import org.drools.core.process.core.impl.ParameterDefinitionImpl;
import org.drools.core.process.core.impl.WorkImpl;
import org.drools.core.runtime.process.ProcessRuntimeFactory;
import org.drools.persistence.mapdb.MapDBEnvironmentName;
import org.jbpm.persistence.PersistentProcessInstance;
import org.jbpm.persistence.mapdb.MapDBProcessInstance;
import org.jbpm.persistence.mapdb.PersistentProcessInstanceSerializer;
import org.jbpm.persistence.mapdb.ProcessInstanceKeySerializer;
import org.jbpm.persistence.mapdb.ProcessKey;
import org.jbpm.persistence.mapdb.util.MapDBProcessPersistenceUtil;
import org.jbpm.process.core.context.variable.Variable;
import org.jbpm.process.instance.ProcessRuntimeFactoryServiceImpl;
import org.jbpm.process.instance.impl.demo.DoNothingWorkItemHandler;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.test.util.AbstractBaseTest;
import org.jbpm.workflow.core.Node;
import org.jbpm.workflow.core.impl.ConnectionImpl;
import org.jbpm.workflow.core.node.EndNode;
import org.jbpm.workflow.core.node.HumanTaskNode;
import org.jbpm.workflow.core.node.StartNode;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.internal.KnowledgeBaseFactory;
import org.kie.internal.io.ResourceFactory;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkItemPersistenceTest extends AbstractBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(WorkItemPersistenceTest.class);
    
    private HashMap<String, Object> context;
    private DB db;
    
    static {
        ProcessRuntimeFactory.setProcessRuntimeFactoryService(new ProcessRuntimeFactoryServiceImpl());
    }
    
    @Before
    public void setUp() throws Exception {
        context = MapDBProcessPersistenceUtil.setupMapDB();
        db = (DB) context.get(MapDBEnvironmentName.DB_OBJECT);
    }
    
    @After
    public void tearDown() throws Exception {
    	MapDBProcessPersistenceUtil.cleanUp(context); 
    }
   
    protected KieSession createSession(KieBase kbase) {
    	return KieServices.Factory.get().getStoreServices().newKieSession(
    			kbase, null, MapDBProcessPersistenceUtil.createEnvironment(context) );
    }

    @Test
    @Ignore
    public void testCancelNonRegisteredWorkItemHandler() {
        String processId = "org.drools.actions";
        String workName = "Unnexistent Task";
        RuleFlowProcess process = getWorkItemProcess( processId, workName );
        KieBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        ((KnowledgeBaseImpl) kbase).addProcess( process );
        KieSession ksession = createSession(kbase);

        ksession.getWorkItemManager().registerWorkItemHandler( workName, new DoNothingWorkItemHandler() );

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put( "UserName", "John Doe" );
        parameters.put( "Person", new Person( "John Doe" ) );

        ProcessInstance processInstance = ksession.startProcess( "org.drools.actions", parameters );
        long processInstanceId = processInstance.getId();
        Assert.assertEquals( ProcessInstance.STATE_ACTIVE, processInstance.getState() );
        ksession.getWorkItemManager().registerWorkItemHandler( workName, null );

        try {
            ksession.abortProcessInstance( processInstanceId );
            Assert.fail( "should fail if WorkItemHandler for" + workName + "is not registered" );
        } catch ( WorkItemHandlerNotFoundException wihnfe ) {

        }

        Assert.assertEquals( ProcessInstance.STATE_ABORTED, processInstance.getState() );
    }

    private RuleFlowProcess getWorkItemProcess(String processId, String workName) {
        RuleFlowProcess process = new RuleFlowProcess();
        process.setId( processId );

        List<Variable> variables = new ArrayList<Variable>();
        Variable variable = new Variable();
        variable.setName( "UserName" );
        variable.setType( new StringDataType() );
        variables.add( variable );
        
        variable = new Variable();
        variable.setName( "MyObject" );
        variable.setType( new ObjectDataType() );
        variables.add( variable );
        variable = new Variable();
        variable.setName( "Number" );
        variable.setType( new IntegerDataType() );
        variables.add( variable );
        process.getVariableScope().setVariables( variables );

        StartNode startNode = new StartNode();
        startNode.setName( "Start" );
        startNode.setId( 1 );

        HumanTaskNode workItemNode = new HumanTaskNode();
        workItemNode.setName( "workItemNode" );
        workItemNode.setId( 2 );
        workItemNode.addInMapping( "Attachment", "MyObject" );
        workItemNode.addOutMapping( "Result", "MyObject" );
        workItemNode.addOutMapping( "Result.length()", "Number" );
        
        Work work = new WorkImpl();
        work.setName( workName );
        
        Set<ParameterDefinition> parameterDefinitions = new HashSet<ParameterDefinition>();
        ParameterDefinition parameterDefinition = new ParameterDefinitionImpl( "ActorId", new StringDataType() );
        parameterDefinitions.add( parameterDefinition );
        parameterDefinition = new ParameterDefinitionImpl( "Content", new StringDataType() );
        parameterDefinitions.add( parameterDefinition );
        parameterDefinition = new ParameterDefinitionImpl( "Comment", new StringDataType() );
        parameterDefinitions.add( parameterDefinition );
        work.setParameterDefinitions( parameterDefinitions );
        
        work.setParameter( "ActorId", "#{UserName}" );
        work.setParameter( "Content", "#{Person.name}" );
        workItemNode.setWork( work );

        EndNode endNode = new EndNode();
        endNode.setName( "End" );
        endNode.setId( 3 );

        connect( startNode, workItemNode );
        connect( workItemNode, endNode );

        process.addNode( startNode );
        process.addNode( workItemNode );
        process.addNode( endNode );

        return process;
    }

    private void connect(Node sourceNode,
                         Node targetNode) {
        new ConnectionImpl( sourceNode,
                             Node.CONNECTION_DEFAULT_TYPE,
                             targetNode,
                             Node.CONNECTION_DEFAULT_TYPE );
    }

    @Test
    public void testHumanTask() {
        List<PersistentProcessInstance> procInstInfoList = retrieveProcessInstances(db);
        int numProcInstInfos = procInstInfoList.size();
        
        Reader source = new StringReader(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<process xmlns=\"http://drools.org/drools-5.0/process\"\n" +
            "         xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
            "         xs:schemaLocation=\"http://drools.org/drools-5.0/process drools-processes-5.0.xsd\"\n" +
            "         type=\"RuleFlow\" name=\"flow\" id=\"org.drools.humantask\" package-name=\"org.drools\" version=\"1\" >\n" +
            "\n" +
            "  <header>\n" +
            "  </header>\n" +
            "\n" +
            "  <nodes>\n" +
            "    <start id=\"1\" name=\"Start\" />\n" +
            "    <humanTask id=\"2\" name=\"HumanTask\" >\n" +
            "      <work name=\"Human Task\" >\n" +
            "        <parameter name=\"ActorId\" >\n" +
            "          <type name=\"org.drools.core.process.core.datatype.impl.type.StringDataType\" />\n" +
            "          <value>John Doe</value>\n" +
            "        </parameter>\n" +
            "        <parameter name=\"TaskName\" >\n" +
            "          <type name=\"org.drools.core.process.core.datatype.impl.type.StringDataType\" />\n" +
            "          <value>Do something</value>\n" +
            "        </parameter>\n" +
            "        <parameter name=\"Priority\" >\n" +
            "          <type name=\"org.drools.core.process.core.datatype.impl.type.StringDataType\" />\n" +
            "        </parameter>\n" +
            "        <parameter name=\"Comment\" >\n" +
            "          <type name=\"org.drools.core.process.core.datatype.impl.type.StringDataType\" />\n" +
            "        </parameter>\n" +
            "      </work>\n" +
            "    </humanTask>\n" +
            "    <end id=\"3\" name=\"End\" />\n" +
            "  </nodes>\n" +
            "\n" +
            "  <connections>\n" +
            "    <connection from=\"1\" to=\"2\" />\n" +
            "    <connection from=\"2\" to=\"3\" />\n" +
            "  </connections>\n" +
            "\n" +
            "</process>");

        KieFileSystem kfs = KieServices.Factory.get().newKieFileSystem();
        kfs.write("src/main/resources/p1.bpmn", ResourceFactory.newReaderResource(source));
        KieBuilder kbuilder = KieServices.Factory.get().newKieBuilder(kfs);
        kbuilder.buildAll();
        KieBase kbase = KieServices.Factory.get().newKieContainer(kbuilder.getKieModule().getReleaseId()).getKieBase();
        KieSession ksession = createSession(kbase);
        final List<WorkItem> workItems = new ArrayList<WorkItem>();
        DoNothingWorkItemHandler handler = new DoNothingWorkItemHandler() {

            @Override
            public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
                super.executeWorkItem(workItem, manager);
                workItems.add(workItem);
            }
            
        };
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", handler);
        
        ProcessInstance processInstance = ksession.startProcess("org.drools.humantask");
        
        assertEquals(ProcessInstance.STATE_ACTIVE, processInstance.getState());
        
        int state = processInstance.getState();
        switch(state) { 
        case ProcessInstance.STATE_ABORTED:
            logger.debug("STATE_ABORTED");
            break;
        case ProcessInstance.STATE_ACTIVE:
            logger.debug("STATE_ACTIVE");
            break;
        case ProcessInstance.STATE_COMPLETED:
            logger.debug("STATE_COMPLETED");
            break;
        case ProcessInstance.STATE_PENDING:
            logger.debug("STATE_PENDING");
            break;
        case ProcessInstance.STATE_SUSPENDED:
            logger.debug("STATE_SUSPENDED");
            break;
        default: 
            logger.debug("Unknown state: {}", state );
        }
       
        procInstInfoList = retrieveProcessInstances(db);
        assertTrue( (procInstInfoList.size() - numProcInstInfos) == 1);
        
        PersistentProcessInstance processInstanceInfoMadeInThisTest = procInstInfoList.get(numProcInstInfos);
        assertNotNull("ByteArray of ProcessInstanceInfo from this test is not filled and null!", 
                processInstanceInfoMadeInThisTest.getProcessInstanceByteArray());
        assertTrue("ByteArray of ProcessInstanceInfo from this test is not filled and empty!", 
                processInstanceInfoMadeInThisTest.getProcessInstanceByteArray().length > 0);
        assertEquals(1, workItems.size());
        ksession.getWorkItemManager().completeWorkItem(workItems.get(0).getId(), null);
        
        ProcessInstance pi = ksession.getProcessInstance(processInstance.getId());
        assertNull(pi);
    }
    
    @SuppressWarnings("unchecked")
    public static ArrayList<PersistentProcessInstance> retrieveProcessInstances(DB db) { 
        ArrayList<PersistentProcessInstance> procInstInfoList = new ArrayList<PersistentProcessInstance>();
        BTreeMap<ProcessKey, PersistentProcessInstance> map = db.treeMap(new MapDBProcessInstance().getMapKey(), 
        		new ProcessInstanceKeySerializer(), new PersistentProcessInstanceSerializer()).createOrOpen();
        Collection<PersistentProcessInstance> mdList = map.values();
        for( PersistentProcessInstance resultObject : mdList ) { 
            procInstInfoList.add(resultObject);
            logger.trace("> {}", resultObject);
        }
        return procInstInfoList;
    }
    
}
