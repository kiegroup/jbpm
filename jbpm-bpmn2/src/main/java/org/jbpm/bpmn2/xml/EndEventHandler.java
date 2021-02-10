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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.drools.core.xml.ExtensibleXmlParser;
import org.jbpm.bpmn2.core.Error;
import org.jbpm.bpmn2.core.Escalation;
import org.jbpm.bpmn2.core.Message;
import org.jbpm.bpmn2.handler.SendMessageAction;
import org.jbpm.bpmn2.handler.SendSignalAction;
import org.jbpm.bpmn2.xml.elements.ThrowEventReader;
import org.jbpm.compiler.xml.ProcessBuildData;
import org.jbpm.workflow.core.Node;
import org.jbpm.workflow.core.NodeContainer;
import org.jbpm.workflow.core.impl.JavaDroolsAction;
import org.jbpm.workflow.core.node.DataAssociation;
import org.jbpm.workflow.core.node.EndNode;
import org.jbpm.workflow.core.node.FaultNode;
import org.jbpm.workflow.core.node.ThrowNode;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class EndEventHandler extends AbstractNodeHandler {

    private ThrowEventReader throwEventReader = new ThrowEventReader();

    protected Node createNode(Attributes attrs) {
        EndNode node = new EndNode();
        node.setTerminate(false);
        return node;
    }

    public Class<EndNode> generateNodeFor() {
        return EndNode.class;
    }

    public Object end(final String uri, final String localName,
            final ExtensibleXmlParser parser) throws SAXException {
        final Element element = parser.endElementBuilder();
        Node node = (Node) parser.getCurrent();
        // determine type of event definition, so the correct type of node
        // can be generated
        List<DataAssociation> dataAssocations = throwEventReader.read(element);
        String varName = null;
        if(!dataAssocations.isEmpty()) {
            varName = dataAssocations.get(0).getTarget();
        }

        super.handleNode(node, element, uri, localName, parser);
        org.w3c.dom.Node xmlNode = element.getFirstChild();
        while (xmlNode != null) {
            String nodeName = xmlNode.getNodeName();
            if ("terminateEventDefinition".equals(nodeName)) {
                // reuse already created EndNode
                ThrowNode throwNode = (ThrowNode) node;
                dataAssocations.forEach(dataAssociation -> throwNode.addInDataAssociation(dataAssociation));
                node.setMetaData("MappingVariable", varName);
                handleTerminateNode(node, element, uri, localName, parser);
                break;
            } else if ("signalEventDefinition".equals(nodeName)) {
                ThrowNode throwNode = (ThrowNode) node;
                dataAssocations.forEach(dataAssociation -> throwNode.addInDataAssociation(dataAssociation));
                node.setMetaData("MappingVariable", varName);
                handleSignalNode(node, element, uri, localName, parser);
            } else if ("messageEventDefinition".equals(nodeName)) {
                ThrowNode throwNode = (ThrowNode) node;
                dataAssocations.forEach(dataAssociation -> throwNode.addInDataAssociation(dataAssociation));
                node.setMetaData("MappingVariable", varName);
                handleMessageNode(node, element, uri, localName, parser);
            } else if ("errorEventDefinition".equals(nodeName)) {
                // create new faultNode
                FaultNode faultNode = new FaultNode();
                faultNode.setId(node.getId());
                faultNode.setName(node.getName());
                faultNode.setTerminateParent(true);
                faultNode.setMetaData("UniqueId", node.getMetaData().get("UniqueId"));
                node = faultNode;
                
                ThrowNode throwNode = (ThrowNode) node;
                dataAssocations.forEach(dataAssociation -> throwNode.addInDataAssociation(dataAssociation));
                node.setMetaData("MappingVariable", varName);

                super.handleNode(node, element, uri, localName, parser);
                handleErrorNode(node, element, uri, localName, parser);
                break;
            } else if ("escalationEventDefinition".equals(nodeName)) {
                // create new faultNode
                FaultNode faultNode = new FaultNode();
                faultNode.setId(node.getId());
                faultNode.setName(node.getName());
                faultNode.setMetaData("UniqueId", node.getMetaData().get("UniqueId"));
                node = faultNode;

                ThrowNode throwNode = (ThrowNode) node;
                dataAssocations.forEach(dataAssociation -> throwNode.addInDataAssociation(dataAssociation));
                node.setMetaData("MappingVariable", varName);

                super.handleNode(node, element, uri, localName, parser);
                handleEscalationNode(node, element, uri, localName, parser);
                break;
            } else if ("compensateEventDefinition".equals(nodeName)) {
                // reuse already created ActionNode
                handleThrowCompensationEventNode(node, element, uri, localName, parser);
                break;
            }
            xmlNode = xmlNode.getNextSibling();
        }

        NodeContainer nodeContainer = (NodeContainer) parser.getParent();
        nodeContainer.addNode(node);
        ((ProcessBuildData) parser.getData()).addNode(node);
        return node;
    }

    public void handleTerminateNode(final Node node, final Element element, final String uri,
            final String localName, final ExtensibleXmlParser parser) throws SAXException {
        ((EndNode) node).setTerminate(true);

        EndNode endNode = (EndNode) node;
        org.w3c.dom.Node xmlNode = element.getFirstChild();
        while (xmlNode != null) {
            String nodeName = xmlNode.getNodeName();
            if ("terminateEventDefinition".equals(nodeName)) {

                String scope = ((Element) xmlNode).getAttribute("scope");
                if ("process".equalsIgnoreCase(scope)) {
                    endNode.setScope(EndNode.PROCESS_SCOPE);
                } else {
                    endNode.setScope(EndNode.CONTAINER_SCOPE);
                }
            }
            xmlNode = xmlNode.getNextSibling();
        }
    }

    public void handleSignalNode(final Node node, final Element element, final String uri,
            final String localName, final ExtensibleXmlParser parser) throws SAXException {
        EndNode endNode = (EndNode) node;
        org.w3c.dom.Node xmlNode = element.getFirstChild();
        while (xmlNode != null) {
            String nodeName = xmlNode.getNodeName();
            if ("signalEventDefinition".equals(nodeName)) {
                String signalName = ((Element) xmlNode).getAttribute("signalRef");
                String variable = (String) endNode.getMetaData("MappingVariable");
                signalName = checkSignalAndConvertToRealSignalNam(parser, signalName, s -> s.addOutgoingNode(node));
                endNode.setMetaData("EventType", "signal");
                endNode.setMetaData("Ref", signalName);
                endNode.setMetaData("Variable", variable);
                endNode.setActions(EndNode.EVENT_NODE_ENTER, Collections.singletonList(new JavaDroolsAction(
                        new SendSignalAction(endNode, variable, signalName, dataInputs.containsValue("async")))));
            }
            xmlNode = xmlNode.getNextSibling();
        }
    }

    @SuppressWarnings("unchecked")
    public void handleMessageNode(final Node node, final Element element, final String uri,
            final String localName, final ExtensibleXmlParser parser) throws SAXException {
        EndNode endNode = (EndNode) node;
        org.w3c.dom.Node xmlNode = element.getFirstChild();
        while (xmlNode != null) {
            String nodeName = xmlNode.getNodeName();
            if ("messageEventDefinition".equals(nodeName)) {
                String messageRef = ((Element) xmlNode).getAttribute("messageRef");
                Map<String, Message> messages = (Map<String, Message>)
                    ((ProcessBuildData) parser.getData()).getMetaData("Messages");
                if (messages == null) {
                    throw new IllegalArgumentException("No messages found");
                }
                Message message = messages.get(messageRef);
                if (message == null) {
                    throw new IllegalArgumentException("Could not find message " + messageRef);
                }
                message.addOutgoingNode(node);
                String varName = (String) node.getMetaData().get("MappingVariable");

                endNode.setMetaData("MessageType", message.getType());
                endNode.setActions(EndNode.EVENT_NODE_ENTER, Collections.singletonList(new JavaDroolsAction(new SendMessageAction(varName, message))));
            }
            xmlNode = xmlNode.getNextSibling();
        }
    }



    @SuppressWarnings("unchecked")
	public void handleErrorNode(final Node node, final Element element, final String uri,
            final String localName, final ExtensibleXmlParser parser) throws SAXException {
        FaultNode faultNode = (FaultNode) node;
        org.w3c.dom.Node xmlNode = element.getFirstChild();
        while (xmlNode != null) {
            String nodeName = xmlNode.getNodeName();
            if ("errorEventDefinition".equals(nodeName)) {
                String errorRef = ((Element) xmlNode).getAttribute("errorRef");
                if (errorRef != null && errorRef.trim().length() > 0) {
                    List<Error> errors = (List<Error>) ((ProcessBuildData) parser.getData()).getMetaData("Errors");
		            if (errors == null) {
		                throw new IllegalArgumentException("No errors found");
		            }
		            Error error = null;
		            for( Error listError: errors ) {
		                if( errorRef.equals(listError.getId()) ) {
		                    error = listError;
		                    break;
		                }
		            }
		            if (error == null) {
		                throw new IllegalArgumentException("Could not find error " + errorRef);
		            }
		            faultNode.setFaultName(error.getErrorCode());
	                faultNode.setTerminateParent(true);
                }
            }
            xmlNode = xmlNode.getNextSibling();
        }
    }

    @SuppressWarnings("unchecked")
	public void handleEscalationNode(final Node node, final Element element, final String uri,
            final String localName, final ExtensibleXmlParser parser) throws SAXException {
        FaultNode faultNode = (FaultNode) node;
        org.w3c.dom.Node xmlNode = element.getFirstChild();
        while (xmlNode != null) {
            String nodeName = xmlNode.getNodeName();
            if ("escalationEventDefinition".equals(nodeName)) {
                String escalationRef = ((Element) xmlNode).getAttribute("escalationRef");
                if (escalationRef != null && escalationRef.trim().length() > 0) {
                    Map<String, Escalation> escalations = (Map<String, Escalation>)
		                ((ProcessBuildData) parser.getData()).getMetaData(ProcessHandler.ESCALATIONS);
		            if (escalations == null) {
		                throw new IllegalArgumentException("No escalations found");
		            }
		            Escalation escalation = escalations.get(escalationRef);
		            if (escalation == null) {
		                throw new IllegalArgumentException("Could not find escalation " + escalationRef);
		            }
		            faultNode.setFaultName(escalation.getEscalationCode());
                } else {
                    // BPMN2 spec, p. 83: end event's with <escalationEventDefintions>
                    // are _required_ to reference a specific escalation(-code).
                    throw new IllegalArgumentException("End events throwing an escalation must throw *specific* escalations (and not general ones).");
                }
            }
            xmlNode = xmlNode.getNextSibling();
        }
    }

    protected void readFaultDataInputAssociation(org.w3c.dom.Node xmlNode, FaultNode faultNode) {
        // sourceRef
        org.w3c.dom.Node subNode = xmlNode.getFirstChild();
        String faultVariable = subNode.getTextContent();
        faultNode.setFaultVariable(faultVariable);
    }

    public void writeNode(Node node, StringBuilder xmlDump, int metaDataType) {
        throw new IllegalArgumentException("Writing out should be handled by specific handlers");
    }

}
