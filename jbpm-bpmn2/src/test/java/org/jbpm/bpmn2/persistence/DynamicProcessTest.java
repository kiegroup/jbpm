/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.bpmn2.persistence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.drools.core.command.impl.CommandBasedStatefulKnowledgeSession;
import org.jbpm.bpmn2.JbpmBpmn2TestCase;
import org.jbpm.bpmn2.xml.XmlBPMNProcessDumper;
import org.jbpm.compiler.xml.XmlRuleFlowProcessDumper;
import org.jbpm.persistence.session.objects.TestWorkItemHandler;
import org.jbpm.process.instance.impl.ProcessInstanceImpl;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.ruleflow.core.RuleFlowProcessFactory;
import org.jbpm.workflow.core.Constraint;
import org.jbpm.workflow.core.DroolsAction;
import org.jbpm.workflow.core.impl.ConnectionImpl;
import org.jbpm.workflow.core.impl.DroolsConsequenceAction;
import org.jbpm.workflow.core.impl.NodeImpl;
import org.jbpm.workflow.core.node.HumanTaskNode;
import org.jbpm.workflow.core.node.Split;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.command.ExecutableCommand;
import org.kie.api.definition.process.Connection;
import org.kie.api.definition.process.Node;
import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.process.ProcessNodeLeftEvent;
import org.kie.api.event.process.ProcessNodeTriggeredEvent;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.event.process.ProcessVariableChangedEvent;
import org.kie.api.fluent.Dialect;
import org.kie.api.io.Resource;
import org.kie.api.runtime.Context;
import org.kie.api.runtime.KieSession;
import org.kie.internal.command.RegistryContext;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a sample file to launch a process.
 */
public class DynamicProcessTest extends JbpmBpmn2TestCase {

    private static final Logger logger = LoggerFactory.getLogger(DynamicProcessTest.class);
    
    @BeforeClass
    public static void setup() throws Exception {
        if (PERSISTENCE) {
            setUpDataSource();
        }
    }

    @Test
    public void testDynamicProcessUpdate() throws Exception {
        Resource resource = ResourceFactory.newClassPathResource("BPMN2-Dynamic-HT-Process.bpmn2");
        resource.setSourcePath("BPMN2-Dynamic-HT-Process.bpmn2"); // source path or target path must be set to be added into kbase
        KieBase kbase = createKnowledgeBaseFromResources(resource);
        RuleFlowProcess process = (RuleFlowProcess) kbase.getProcess("ht-script-process");
        StatefulKnowledgeSession ksession = createKnowledgeSession(kbase);
        TestWorkItemHandler testHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", testHandler);

        final ProcessInstanceImpl processInstance = (ProcessInstanceImpl) ksession.startProcess("ht-script-process", Collections.singletonMap("isCond", true));


        assertProcessInstanceActive(processInstance);
        ksession.getWorkItemManager().completeWorkItem(testHandler.getWorkItem().getId(), null);

        HumanTaskNode node = new HumanTaskNode();
        node.setName("Task Appended");
        node.setId(10);
        List<DroolsAction> actions = new ArrayList<DroolsAction>();
        actions.add(new DroolsConsequenceAction("java", "System.out.println(\"on Entry to the node the dynamic node added!!\");"));
        node.setActions("onEntry", actions);

        insertNodeBefore(process, "Script 1", node);

        ((CommandBasedStatefulKnowledgeSession) ksession).getRunner().execute(new ExecutableCommand<Void>() {
            public Void execute(Context context) {
                StatefulKnowledgeSession ks = (StatefulKnowledgeSession) ((RegistryContext) context).lookup( KieSession.class );
                ProcessInstanceImpl impl = ((ProcessInstanceImpl) ks.getProcessInstance(processInstance.getId()));
                impl.updateProcess(process);
                return null;
            }
        });


        assertProcessInstanceActive(processInstance);
        ksession.getWorkItemManager().completeWorkItem(testHandler.getWorkItem().getId(), null);

        assertProcessInstanceActive(processInstance);
        ksession.getWorkItemManager().completeWorkItem(testHandler.getWorkItem().getId(), null);

        assertProcessInstanceFinished(processInstance, ksession);
        
        ksession.dispose();

    }

    @Test
	public void testDynamicProcess() throws Exception {		
		RuleFlowProcessFactory factory = RuleFlowProcessFactory.createProcess("org.jbpm.HelloWorld");
		factory
			// Header
			.name("HelloWorldProcess")
			.version("1.0")
			.packageName("org.jbpm")
			.imports("org.jbpm.bpmn2.objects.Person")
			// Nodes
			.startNode(1).name("Start").done()
			.humanTaskNode(2).name("Task1").actorId("krisv").taskName("MyTask").done()
			.splitNode(3).name("Split").type(Split.TYPE_XOR)
				.constraint(4, "c1", "code", Dialect.JAVA, "return new Person().getName() == null;", 1)
				.constraint(5, "c2", "code", Dialect.JAVA, "return false;", 2)
				.done()
			.endNode(4).name("End1").done()
			.endNode(5).name("End2").done()
			// Connections
			.connection(1, 2)
			.connection(2, 3)
			.connection(3, 4)
			.connection(3, 5);
		final RuleFlowProcess process = factory.validate().getProcess();
		Resource resource = ResourceFactory
                .newByteArrayResource(XmlRuleFlowProcessDumper.INSTANCE.dump(
                        process).getBytes());
		resource.setSourcePath("/tmp/dynamicProcess.bpmn2"); // source path or target path must be set to be added into kbase
        KieBase kbase = createKnowledgeBaseFromResources(resource);
		StatefulKnowledgeSession ksession = createKnowledgeSession(kbase);
		TestWorkItemHandler testHandler = new TestWorkItemHandler();
		ksession.getWorkItemManager().registerWorkItemHandler("Human Task", testHandler);
		ksession.addEventListener(new ProcessEventListener() {
			public void beforeVariableChanged(ProcessVariableChangedEvent arg0) {
			}
			public void beforeProcessStarted(ProcessStartedEvent arg0) {
				logger.info("{}", arg0);
			}
			public void beforeProcessCompleted(ProcessCompletedEvent arg0) {
			    logger.info("{}", arg0);
			}
			public void beforeNodeTriggered(ProcessNodeTriggeredEvent arg0) {
			    logger.info("{}", arg0);
			}
			public void beforeNodeLeft(ProcessNodeLeftEvent arg0) {
			    logger.info("{}", arg0);
			}
			public void afterVariableChanged(ProcessVariableChangedEvent arg0) {
			}
			public void afterProcessStarted(ProcessStartedEvent arg0) {
			}
			public void afterProcessCompleted(ProcessCompletedEvent arg0) {
			}
			public void afterNodeTriggered(ProcessNodeTriggeredEvent arg0) {
			}
			public void afterNodeLeft(ProcessNodeLeftEvent arg0) {
			}
		});

		final ProcessInstanceImpl processInstance = (ProcessInstanceImpl)
			ksession.startProcess("org.jbpm.HelloWorld");
		
		HumanTaskNode node = new HumanTaskNode();
		node.setName("Task2");
		node.setId(6);
		List<DroolsAction> actions = new ArrayList<DroolsAction>();
		actions.add(new DroolsConsequenceAction("java", "System.out.println(\"OnEntry\");"));
		node.setActions("onEntry", actions);
		insertNodeInBetween(process, 2, 3, node);
		XmlBPMNProcessDumper.INSTANCE.dump(process);
		
		((CommandBasedStatefulKnowledgeSession) ksession).getRunner().execute(new ExecutableCommand<Void>() {
			public Void execute(Context context) {
				StatefulKnowledgeSession ksession = (StatefulKnowledgeSession) ((RegistryContext) context).lookup( KieSession.class );
				((ProcessInstanceImpl) ksession.getProcessInstance(processInstance.getId())).updateProcess(process);
				return null;
			}
		});

        assertProcessInstanceActive(processInstance);
		ksession.getWorkItemManager().completeWorkItem(testHandler.getWorkItem().getId(), null);
		
		assertProcessInstanceActive(processInstance);
		ksession.getWorkItemManager().completeWorkItem(testHandler.getWorkItem().getId(), null);

		assertProcessInstanceFinished(processInstance, ksession);
	    ksession.getWorkItemManager().completeWorkItem(testHandler.getWorkItem().getId(), null);



	    assertProcessInstanceFinished(processInstance, ksession);
		ksession.dispose();
	}

    private static void insertNodeInBetween(RuleFlowProcess process, long startNodeId, long endNodeId, NodeImpl node) {
        if (process == null) {
            throw new IllegalArgumentException("Process may not be null");
        }
        NodeImpl selectedNode = (NodeImpl) process.getNode(startNodeId);
        if (selectedNode == null) {
            throw new IllegalArgumentException("Node " + startNodeId + " not found in process " + process.getId());
        }
        for (Connection connection : selectedNode.getDefaultOutgoingConnections()) {
            if (connection.getTo().getId() != endNodeId) {
                continue;
            }
            Constraint constraint = null;
            if(selectedNode instanceof Split) {
                Split split = (Split) selectedNode;
                constraint = split.getConstraint(connection);
            }

            process.addNode(node);
            NodeImpl endNode = (NodeImpl) connection.getTo();
            ((ConnectionImpl) connection).terminate();
            Connection conn = new ConnectionImpl(selectedNode, NodeImpl.CONNECTION_DEFAULT_TYPE, node, NodeImpl.CONNECTION_DEFAULT_TYPE);
            new ConnectionImpl(node, NodeImpl.CONNECTION_DEFAULT_TYPE, endNode, NodeImpl.CONNECTION_DEFAULT_TYPE);
            if(selectedNode instanceof Split) {
                ((Split) selectedNode).setConstraint(conn, constraint);
            }
            return;
        }
        throw new IllegalArgumentException("Connection to node " + endNodeId + " not found in process " + process.getId());
    }   

    private static void insertNodeBefore(RuleFlowProcess process, String nodeName, NodeImpl node) {
        if (process == null) {
            throw new IllegalArgumentException("Process may not be null");
        }
        NodeImpl endNode = null;
        for (Node n : process.getNodes()) {
            if (nodeName.equals(n.getName())) {
                endNode = (NodeImpl)n;
            }
        }
        if (endNode == null) {
            throw new IllegalArgumentException("Node " + nodeName + " not found in process " + process.getId());
        }
        NodeImpl startNode = (NodeImpl)endNode.getDefaultIncomingConnections().get(0).getFrom();
        insertNodeInBetween(process, startNode.getId(), endNode.getId(), node);
    }
	
}
