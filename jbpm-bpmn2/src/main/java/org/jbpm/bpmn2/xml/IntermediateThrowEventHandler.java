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

package org.jbpm.bpmn2.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.drools.core.xml.ExtensibleXmlParser;
import org.jbpm.bpmn2.core.Escalation;
import org.jbpm.bpmn2.core.IntermediateLink;
import org.jbpm.bpmn2.core.Message;
import org.jbpm.bpmn2.handler.SendMessageAction;
import org.jbpm.bpmn2.handler.SendSignalAction;
import org.jbpm.bpmn2.xml.elements.ThrowEventReader;
import org.jbpm.compiler.xml.ProcessBuildData;
import org.jbpm.process.core.context.variable.Variable;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.workflow.core.Node;
import org.jbpm.workflow.core.NodeContainer;
import org.jbpm.workflow.core.impl.DroolsConsequenceAction;
import org.jbpm.workflow.core.impl.JavaDroolsAction;
import org.jbpm.workflow.core.node.ActionNode;
import org.jbpm.workflow.core.node.CompositeNode;
import org.jbpm.workflow.core.node.DataAssociation;
import org.jbpm.workflow.core.node.ThrowLinkNode;
import org.kie.api.definition.process.NodeType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class IntermediateThrowEventHandler extends AbstractNodeHandler {

    
	public static final String LINK_NAME = "linkName";
	public static final String LINK_SOURCE = "source";
	public static final String LINK_TARGET = "target";

    private ThrowEventReader throwEventReader = new ThrowEventReader();

	protected Node createNode(Attributes attrs) {
        return new ActionNode(NodeType.THROW_EVENT);
	}

	public Class<Node> generateNodeFor() {
		return Node.class;
	}

	public Object end(final String uri, final String localName,
			final ExtensibleXmlParser parser) throws SAXException {
		final Element element = parser.endElementBuilder();
		ActionNode node = (ActionNode) parser.getCurrent();

		// determine type of event definition, so the correct type of node
		// can be generated 
        List<DataAssociation> dataAssociations = throwEventReader.read(element);
        String varName = null;
        if(!dataAssociations.isEmpty()) {
            varName = dataAssociations.get(0).getTarget();
        }
        dataAssociations.forEach(dataAssociation -> node.addInDataAssociation(dataAssociation));
        node.setMetaData("MappingVariable", varName);

		org.w3c.dom.Node xmlNode = element.getFirstChild();
		while (xmlNode != null) {
			String nodeName = xmlNode.getNodeName();
			if ("signalEventDefinition".equals(nodeName)) {
				// reuse already created ActionNode
				handleSignalNode(node, element, uri, localName, parser);
				break;
			} else if ("messageEventDefinition".equals(nodeName)) {
				// reuse already created ActionNode
				handleMessageNode(node, element, uri, localName, parser);
				break;
			} else if ("escalationEventDefinition".equals(nodeName)) {
				// reuse already created ActionNode
				handleEscalationNode(node, element, uri, localName, parser);
				break;
			} else if ("compensateEventDefinition".equals(nodeName)) {
				// reuse already created ActionNode
				handleThrowCompensationEventNode(node, element, uri, localName, parser);
				break;
			} else if ("linkEventDefinition".equals(nodeName)) {
				ThrowLinkNode linkNode = new ThrowLinkNode();
				linkNode.setId(node.getId());
				handleLinkNode(element, linkNode, xmlNode, parser);
				NodeContainer nodeContainer = (NodeContainer) parser.getParent();
				nodeContainer.addNode(linkNode);
				((ProcessBuildData) parser.getData()).addNode(node);
				// we break the while and stop the execution of this method.
				return linkNode;
			}
			xmlNode = xmlNode.getNextSibling();
		}
		// none event definition
		if (node.getAction() == null) {
			node.setAction(new DroolsConsequenceAction("mvel", ""));
			node.setMetaData("NodeType", "IntermediateThrowEvent-None");
		}
		NodeContainer nodeContainer = (NodeContainer) parser.getParent();
		nodeContainer.addNode(node);
		return node;
	}

	protected void handleLinkNode(Element element, Node node,
			org.w3c.dom.Node xmlLinkNode, ExtensibleXmlParser parser) {

		node.setName(element.getAttribute("name"));

		NamedNodeMap linkAttr = xmlLinkNode.getAttributes();
		String name = linkAttr.getNamedItem("name").getNodeValue();


		String id = element.getAttribute("id");
		node.setMetaData("UniqueId", id);
		node.setMetaData(LINK_NAME, name);

		org.w3c.dom.Node xmlNode = xmlLinkNode.getFirstChild();

		NodeContainer nodeContainer = (NodeContainer) parser.getParent();

		IntermediateLink aLink = new IntermediateLink();
		aLink.setName(name);
		aLink.setUniqueId(id);
		while (null != xmlNode) {
			String nodeName = xmlNode.getNodeName();

			if (LINK_TARGET.equals(nodeName)) {
				String target = xmlNode.getTextContent();
				node.setMetaData(LINK_TARGET, target);
			}

			if (LINK_SOURCE.equals(nodeName)) {
				String source = xmlNode.getTextContent();
				ArrayList<String> sources = (ArrayList<String>) node
						.getMetaData().get(LINK_SOURCE);

				// if there is no list, create one
				if (null == sources) {
					sources = new ArrayList<String>();
				}

				// to connect nodes.
				aLink.addSource(source);

				// to do the xml dump
				sources.add(source);
				node.setMetaData(LINK_SOURCE, sources);
			}
			xmlNode = xmlNode.getNextSibling();
		}
		aLink.configureThrow();

		if (nodeContainer instanceof RuleFlowProcess) {
			RuleFlowProcess process = (RuleFlowProcess) nodeContainer;
			List<IntermediateLink> links = (List<IntermediateLink>) process
					.getMetaData().get(ProcessHandler.LINKS);
			if (null == links) {
				links = new ArrayList<IntermediateLink>();
			}
			links.add(aLink);
			process.setMetaData(ProcessHandler.LINKS, links);
		} else if (nodeContainer instanceof CompositeNode) {
			CompositeNode subprocess = (CompositeNode) nodeContainer;
			List<IntermediateLink> links = (List<IntermediateLink>) subprocess
					.getMetaData().get(ProcessHandler.LINKS);
			if (null == links) {
				links = new ArrayList<IntermediateLink>();
			}
			links.add(aLink);
			subprocess.setMetaData(ProcessHandler.LINKS, links);
		}

	}

	public void handleSignalNode(final Node node, final Element element,
			final String uri, final String localName,
			final ExtensibleXmlParser parser) throws SAXException {
		ActionNode actionNode = (ActionNode) node;
		org.w3c.dom.Node xmlNode = element.getFirstChild();
		while (xmlNode != null) {
			String nodeName = xmlNode.getNodeName();
			if ("signalEventDefinition".equals(nodeName)) {
                String signalName = checkSignalAndConvertToRealSignalNam(parser, ((Element) xmlNode).getAttribute("signalRef"), s -> s.addOutgoingNode(node));
                String variable = (String) actionNode.getMetaData("MappingVariable");
                actionNode.setMetaData("EventType", "signal");
                actionNode.setMetaData("Ref", signalName);
                actionNode.setMetaData("Variable", variable);
                actionNode.setAction(new JavaDroolsAction(new SendSignalAction(actionNode, variable, signalName, dataInputs.containsValue("async"))));
			}
			xmlNode = xmlNode.getNextSibling();
		}
	}

    @SuppressWarnings("unchecked")
	public void handleMessageNode(final Node node, final Element element,
			final String uri, final String localName,
			final ExtensibleXmlParser parser) throws SAXException {
		ActionNode actionNode = (ActionNode) node;
		org.w3c.dom.Node xmlNode = element.getFirstChild();
		while (xmlNode != null) {
			String nodeName = xmlNode.getNodeName();
			if ("messageEventDefinition".equals(nodeName)) {
				String messageRef = ((Element) xmlNode).getAttribute("messageRef");
				Map<String, Message> messages = (Map<String, Message>) ((ProcessBuildData) parser.getData()).getMetaData("Messages");
				if (messages == null) {
					throw new IllegalArgumentException("No messages found");
				}
				Message message = messages.get(messageRef);
				if (message == null) {
					throw new IllegalArgumentException("Could not find message " + messageRef);
				}
                message.addOutgoingNode(node);
                String mappingVariable = (String) actionNode.getMetaData("MappingVariable");
                Variable v = (Variable) ((ProcessBuildData) parser.getData()).getMetaData("Variable");
                String varName = v != null ? (String) v.getMetaData(mappingVariable) : mappingVariable;
                actionNode.setMetaData("MessageType", message.getType());
                actionNode.setAction(new JavaDroolsAction(new SendMessageAction(varName, message)));
			}
			xmlNode = xmlNode.getNextSibling();
		}
	}

	@SuppressWarnings("unchecked")
	public void handleEscalationNode(final Node node, final Element element,
			final String uri, final String localName,
			final ExtensibleXmlParser parser) throws SAXException {
		ActionNode actionNode = (ActionNode) node;
		org.w3c.dom.Node xmlNode = element.getFirstChild();
		while (xmlNode != null) {
			String nodeName = xmlNode.getNodeName();
			if ("escalationEventDefinition".equals(nodeName)) {
				String escalationRef = ((Element) xmlNode)
						.getAttribute("escalationRef");
				if (escalationRef != null && escalationRef.trim().length() > 0) {
					Map<String, Escalation> escalations = (Map<String, Escalation>) ((ProcessBuildData) parser
							.getData()).getMetaData(ProcessHandler.ESCALATIONS);
					if (escalations == null) {
						throw new IllegalArgumentException(
								"No escalations found");
					}
					Escalation escalation = escalations.get(escalationRef);
					if (escalation == null) {
						throw new IllegalArgumentException(
								"Could not find escalation " + escalationRef);
					}
					String faultName = escalation.getEscalationCode();
					String variable = (String) actionNode.getMetaData("MappingVariable");
					actionNode
							.setAction(new DroolsConsequenceAction(
									"java",
									"org.jbpm.process.instance.context.exception.ExceptionScopeInstance scopeInstance = (org.jbpm.process.instance.context.exception.ExceptionScopeInstance) ((org.jbpm.workflow.instance.NodeInstance) kcontext.getNodeInstance()).resolveContextInstance(org.jbpm.process.core.context.exception.ExceptionScope.EXCEPTION_SCOPE, \""
											+ faultName
											+ "\");"
											+ EOL
											+ "if (scopeInstance != null) {"
											+ EOL
											+ " Object tVariable = "+ (variable == null ? "null" : variable)+";"
											+ "org.jbpm.workflow.core.node.Transformation transformation = (org.jbpm.workflow.core.node.Transformation)kcontext.getNodeInstance().getNode().getMetaData().get(\"Transformation\");"
											+ "if (transformation != null) {"
											+ "  tVariable = new org.jbpm.process.core.event.EventTransformerImpl(transformation)"
											+ "  .transformEvent("+(variable == null ? "null" : variable)+");"
											+ "}"
											+ "  scopeInstance.handleException(\""
											+ faultName
											+ "\", tVariable);"
											+ EOL
											+ "} else {"
											+ EOL
											+ "    ((org.jbpm.process.instance.ProcessInstance) kcontext.getProcessInstance()).setState(org.jbpm.process.instance.ProcessInstance.STATE_ABORTED);"
											+ EOL + "}"));
				} else {
				    throw new IllegalArgumentException("General escalation is not yet supported");
				}
			}
			xmlNode = xmlNode.getNextSibling();
		}
	}



	public void writeNode(Node node, StringBuilder xmlDump, int metaDataType) {
		throw new IllegalArgumentException(
				"Writing out should be handled by action node handler");
	}

}
