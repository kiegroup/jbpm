/**
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.bpmn2.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.compiler.xml.XmlDumper;
import org.drools.process.core.Work;
import org.drools.xml.ExtensibleXmlParser;
import org.jbpm.workflow.core.Node;
import org.jbpm.workflow.core.node.HumanTaskNode;
import org.jbpm.workflow.core.node.TaskDeadline;
import org.jbpm.workflow.core.node.TaskNotification;
import org.jbpm.workflow.core.node.TaskReassignment;
import org.jbpm.workflow.core.node.WorkItemNode;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class UserTaskHandler extends TaskHandler {
    
    protected Node createNode(Attributes attrs) {
        return new HumanTaskNode();
    }
    
    @SuppressWarnings("unchecked")
	public Class generateNodeFor() {
        return HumanTaskNode.class;
    }

    protected void handleNode(final Node node, final Element element, final String uri, 
            final String localName, final ExtensibleXmlParser parser) throws SAXException {
    	super.handleNode(node, element, uri, localName, parser);
    	HumanTaskNode humanTaskNode = (HumanTaskNode) node;
        Work work = humanTaskNode.getWork();
        work.setName("Human Task");
    	Map<String, String> dataInputs = new HashMap<String, String>();
    	Map<String, String> dataOutputs = new HashMap<String, String>();
    	List<String> owners = new ArrayList<String>();
    	org.w3c.dom.Node xmlNode = element.getFirstChild();
        while (xmlNode != null) {
        	String nodeName = xmlNode.getNodeName();
        	if ("ioSpecification".equals(nodeName)) {
        		readIoSpecification(xmlNode, dataInputs, dataOutputs);
//        	} else if ("dataInputAssociation".equals(nodeName)) {
 //       		readDataInputAssociation(xmlNode, humanTaskNode, dataInputs);
  //      	} else if ("dataOutputAssociation".equals(nodeName)) {
   //     		readDataOutputAssociation(xmlNode, humanTaskNode, dataOutputs);
        	} else if ("potentialOwner".equals(nodeName)) {
        		owners.add(readPotentialOwner(xmlNode, humanTaskNode));
        	} else if ("extensionElements".equals(nodeName)) {
        	    readExtensionElements(xmlNode, humanTaskNode);
        	}
    		xmlNode = xmlNode.getNextSibling();
        }
        if (owners.size() > 0) {
        	String owner = owners.get(0);
        	for (int i = 1; i < owners.size(); i++) {
        		owner += "," + owners.get(i);
        	}
        	humanTaskNode.getWork().setParameter("ActorId", owner);        	
        }
    }
    
    protected void readExtensionElements(org.w3c.dom.Node xmlNode, HumanTaskNode humanTaskNode) {
        NodeList childs = xmlNode.getChildNodes();
        List<TaskDeadline> deadlines = null;
        for (int i = 0; i < childs.getLength(); i++) {
            org.w3c.dom.Node element = childs.item(i);
            
            if ("deadline".equals(element.getNodeName())) {
                TaskDeadline deadline = readDeadline(element, humanTaskNode);
                if (deadlines == null) {
                    deadlines = new ArrayList<TaskDeadline>();
                }
                deadlines.add(deadline);
            }
        }
        if (deadlines != null) {
            humanTaskNode.getWork().setParameter("Deadlines", deadlines);
        }
        
    }

    protected TaskDeadline readDeadline(org.w3c.dom.Node element, HumanTaskNode humanTaskNode) {
        TaskDeadline deadline = new TaskDeadline();
        
        String type = ((Element)element).getAttribute("type");
        String expires = ((Element)element).getAttribute("expires");
        if (!"start".equals(type) && !"complete".equals(type)) {
            throw new IllegalArgumentException("Deadline for user task " 
                    + humanTaskNode.getName() + " is invalid (start|completed are supported)");
        }
        
        if (expires == null || expires.length() == 0) {
            throw new IllegalArgumentException("Deadline for user task " 
                    + humanTaskNode.getName() + " is not set");
        }
        
        deadline.setExpires(expires);
        deadline.setType(type);
        
        // handle reassignment and notification
        element = element.getFirstChild();
        while (element != null) {
            if ("reassignment".equals(element.getNodeName())) {
                TaskReassignment reassignment = new TaskReassignment();
                String reassignUsers = ((Element)element).getAttribute("users");
                String reassignGroups = ((Element)element).getAttribute("groups");
                
                if (reassignGroups == null && reassignUsers == null) {
                    throw new IllegalArgumentException("Deadline->Reassignment for user task " 
                            + humanTaskNode.getName() + " does not have owners to reassign");
                }
                reassignment.setReassignUsers(reassignUsers);
                reassignment.setReassignGroups(reassignGroups);
                
                deadline.addReassignment(reassignment);
            } else if ("notification".equals(element.getNodeName())) {
                TaskNotification notification = new TaskNotification();
                
                String notificationType = ensureDefault(((Element)element).getAttribute("type"), "email");
                if (!"email".equals(notificationType)) {
                    throw new IllegalArgumentException("Deadline->Notification for user task " 
                            + humanTaskNode.getName() + " only email notification is supported");
                }
                notification.setType(notificationType);
                
                // process details of email notification, as that is the only one supported for now
                NodeList notificationDetails = element.getChildNodes();
                
                for (int x = 0; x < notificationDetails.getLength(); x++) {
                    org.w3c.dom.Node detail = notificationDetails.item(x);
                    
                    if ("subject".equals(detail.getNodeName())) {
                        String locale = ensureDefault(((Element)detail).getAttribute("locale"), "en-UK");
                        String subject = detail.getTextContent();
                        
                        notification.addSubject(locale, subject);
                    } else if ("body".equals(detail.getNodeName())) {
                        String locale = ensureDefault(((Element)detail).getAttribute("locale"), "en-UK");
                        String body = detail.getTextContent();
                        
                        notification.addBody(locale, body);
                    } else if ("address".equals(detail.getNodeName())) {
                        String recipients = ((Element)detail).getAttribute("recipients");
                        String grouprecipients = ((Element)detail).getAttribute("grouprecipients");
                        String from = ((Element)detail).getAttribute("from");
                        String replyTo = ((Element)detail).getAttribute("replyTo");
                        
                        if (recipients == null && grouprecipients == null) {
                            throw new IllegalArgumentException("Deadline->Notification for user task " 
                                    + humanTaskNode.getName() + " no recipients defined");
                        }
                        
                        notification.setRecipients(recipients);
                        notification.setGroupRecipients(grouprecipients);
                        notification.setSender(from);
                        notification.setReceiver(replyTo);
                    }
                }
                
                deadline.addNotification(notification);
            }
            
            element = element.getNextSibling();
        }
        
        return deadline;
        
    }

    protected String readPotentialOwner(org.w3c.dom.Node xmlNode, HumanTaskNode humanTaskNode) {
		return xmlNode.getFirstChild().getFirstChild().getFirstChild().getTextContent();
    }
    
	public void writeNode(Node node, StringBuilder xmlDump, int metaDataType) {
		HumanTaskNode humanTaskNode = (HumanTaskNode) node;
		writeNode("userTask", humanTaskNode, xmlDump, metaDataType);
		xmlDump.append(">" + EOL);
		writeScripts(humanTaskNode, xmlDump);
		writeIO(humanTaskNode, xmlDump);
		String ownerString = (String) humanTaskNode.getWork().getParameter("ActorId");
		if (ownerString != null) {
			String[] owners = ownerString.split(",");
			for (String owner: owners) {
				xmlDump.append(
					"      <potentialOwner>" + EOL +
					"        <resourceAssignmentExpression>" + EOL +
					"          <formalExpression>" + owner + "</formalExpression>" + EOL +
					"        </resourceAssignmentExpression>" + EOL +
					"      </potentialOwner>" + EOL);
			}
		}
		endNode("userTask", xmlDump);
	}

	protected void writeIO(WorkItemNode workItemNode, StringBuilder xmlDump) {
		xmlDump.append("      <ioSpecification>" + EOL);
		for (Map.Entry<String, String> entry: workItemNode.getInMappings().entrySet()) {
			xmlDump.append("        <dataInput id=\"" + XmlBPMNProcessDumper.getUniqueNodeId(workItemNode) + "_" + XmlBPMNProcessDumper.replaceIllegalCharsAttribute(entry.getKey()) + "Input\" name=\"" + XmlBPMNProcessDumper.replaceIllegalCharsAttribute(entry.getKey()) + "\" />" + EOL);
		}
		for (Map.Entry<String, Object> entry: workItemNode.getWork().getParameters().entrySet()) {
			if (!"ActorId".equals(entry.getKey()) && entry.getValue() != null) {
				xmlDump.append("        <dataInput id=\"" + XmlBPMNProcessDumper.getUniqueNodeId(workItemNode) + "_" + XmlBPMNProcessDumper.replaceIllegalCharsAttribute(entry.getKey()) + "Input\" name=\"" + XmlBPMNProcessDumper.replaceIllegalCharsAttribute(entry.getKey()) + "\" />" + EOL);
			}
		}
		for (Map.Entry<String, String> entry: workItemNode.getOutMappings().entrySet()) {
			xmlDump.append("        <dataOutput id=\"" + XmlBPMNProcessDumper.getUniqueNodeId(workItemNode) + "_" + XmlBPMNProcessDumper.replaceIllegalCharsAttribute(entry.getKey()) + "Output\" name=\"" + XmlBPMNProcessDumper.replaceIllegalCharsAttribute(entry.getKey()) + "\" />" + EOL);
		}
		xmlDump.append("        <inputSet>" + EOL);
		for (Map.Entry<String, String> entry: workItemNode.getInMappings().entrySet()) {
			xmlDump.append("          <dataInputRefs>" + XmlBPMNProcessDumper.getUniqueNodeId(workItemNode) + "_" + XmlDumper.replaceIllegalChars(entry.getKey()) + "Input</dataInputRefs>" + EOL);
		}
		for (Map.Entry<String, Object> entry: workItemNode.getWork().getParameters().entrySet()) {
			if (!"ActorId".equals(entry.getKey()) && entry.getValue() != null) {
				xmlDump.append("          <dataInputRefs>" + XmlBPMNProcessDumper.getUniqueNodeId(workItemNode) + "_" + XmlDumper.replaceIllegalChars(entry.getKey()) + "Input</dataInputRefs>" + EOL);
			}
		}
		xmlDump.append(
			"        </inputSet>" + EOL);
		xmlDump.append("        <outputSet>" + EOL);
		for (Map.Entry<String, String> entry: workItemNode.getOutMappings().entrySet()) {
			xmlDump.append("          <dataOutputRefs>" + XmlBPMNProcessDumper.getUniqueNodeId(workItemNode) + "_" + XmlDumper.replaceIllegalChars(entry.getKey()) + "Output</dataOutputRefs>" + EOL);
		}
		xmlDump.append(
			"        </outputSet>" + EOL);
		xmlDump.append(
			"      </ioSpecification>" + EOL);
		for (Map.Entry<String, String> entry: workItemNode.getInMappings().entrySet()) {
			xmlDump.append("      <dataInputAssociation>" + EOL);
			xmlDump.append(
				"        <sourceRef>" + XmlDumper.replaceIllegalChars(entry.getValue()) + "</sourceRef>" + EOL +
				"        <targetRef>" + XmlBPMNProcessDumper.getUniqueNodeId(workItemNode) + "_" + XmlDumper.replaceIllegalChars(entry.getKey()) + "Input</targetRef>" + EOL);
			xmlDump.append("      </dataInputAssociation>" + EOL);
		}
		for (Map.Entry<String, Object> entry: workItemNode.getWork().getParameters().entrySet()) {
			if (!"ActorId".equals(entry.getKey()) && entry.getValue() != null) {
				xmlDump.append("      <dataInputAssociation>" + EOL);
				xmlDump.append(
					"        <targetRef>" + XmlBPMNProcessDumper.getUniqueNodeId(workItemNode) + "_" + XmlDumper.replaceIllegalChars(entry.getKey()) + "Input</targetRef>" + EOL +
					"        <assignment>" + EOL +
					"          <from xsi:type=\"tFormalExpression\">" + XmlDumper.replaceIllegalChars(entry.getValue().toString()) + "</from>" + EOL +
					"          <to xsi:type=\"tFormalExpression\">" + XmlBPMNProcessDumper.getUniqueNodeId(workItemNode) + "_" + XmlDumper.replaceIllegalChars(entry.getKey()) + "Input</to>" + EOL +
					"        </assignment>" + EOL);
				xmlDump.append("      </dataInputAssociation>" + EOL);
			}
		}
		for (Map.Entry<String, String> entry: workItemNode.getOutMappings().entrySet()) {
			xmlDump.append("      <dataOutputAssociation>" + EOL);
			xmlDump.append(
				"        <sourceRef>" + XmlBPMNProcessDumper.getUniqueNodeId(workItemNode) + "_" + XmlDumper.replaceIllegalChars(entry.getKey()) + "Output</sourceRef>" + EOL +
				"        <targetRef>" + XmlDumper.replaceIllegalChars(entry.getValue()) + "</targetRef>" + EOL);
			xmlDump.append("      </dataOutputAssociation>" + EOL);
		}
	}
	
	private String ensureDefault(String value, String defaultValue) {
	    if (value == null) {
	        return defaultValue;
	    }
	    
	    return value;
	}

}
