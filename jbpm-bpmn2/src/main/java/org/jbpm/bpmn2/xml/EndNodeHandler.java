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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.jbpm.bpmn2.xml.elements.ThrowEventWriter;
import org.jbpm.process.core.context.exception.CompensationScope;
import org.jbpm.process.instance.impl.JavaAction;
import org.jbpm.workflow.core.DroolsAction;
import org.jbpm.workflow.core.Node;
import org.jbpm.workflow.core.impl.DroolsConsequenceAction;
import org.jbpm.workflow.core.impl.JavaDroolsAction;
import org.jbpm.workflow.core.node.EndNode;
import org.xml.sax.Attributes;

public class EndNodeHandler extends AbstractNodeHandler {
    
    private ThrowEventWriter throwEventWriter = new ThrowEventWriter();

    protected Node createNode(Attributes attrs) {
        throw new IllegalArgumentException("Reading in should be handled by end event handler");
    }

	public Class<EndNode> generateNodeFor() {
        return EndNode.class;
    }

    public void writeNode(Node node, StringBuilder xmlDump, int metaDataType) {
		EndNode endNode = (EndNode) node;
		
		String eventType = (String) endNode.getMetaData("EventType");
        String ref = (String) endNode.getMetaData("Ref");

		writeNode("endEvent", endNode, xmlDump, metaDataType);
		if (endNode.isTerminate()) {
    		xmlDump.append(">" + EOL);
    		writeExtensionElements(endNode, xmlDump);
            xmlDump.append("        <terminateEventDefinition " + (endNode.getScope() == EndNode.PROCESS_SCOPE ? "tns:scope=\"process\"" : "") + "/>" + EOL);
            writeInput(xmlDump, endNode);
    		endNode("endEvent", xmlDump);
		} else {
		    String scope = (String) endNode.getMetaData("customScope");
		    List<DroolsAction> actions = endNode.getActions(EndNode.EVENT_NODE_ENTER);
		    if (actions != null && !actions.isEmpty()) {
		        if (actions.size() == 1) {
                    DroolsAction droolsAction = actions.get(0);
                    if (droolsAction instanceof DroolsConsequenceAction) {
		            DroolsConsequenceAction action = (DroolsConsequenceAction) actions.get(0);
		            String s = action.getConsequence();
		            if (s.startsWith("org.drools.core.process.instance.impl.WorkItemImpl workItem = new org.drools.core.process.instance.impl.WorkItemImpl();")) {
		                xmlDump.append(">" + EOL);
		                writeExtensionElements(endNode, xmlDump);
                        xmlDump.append("      <messageEventDefinition messageRef=\"" + XmlBPMNProcessDumper.getUniqueNodeId(endNode) + "_Message\"/>" + EOL);
                        writeInput(xmlDump, endNode);
                        endNode("endEvent", xmlDump);
		            } else if ("signal".equals(eventType)) {
		                xmlDump.append(">" + EOL);
		                writeExtensionElements(endNode, xmlDump);
		                xmlDump.append("      <signalEventDefinition signalRef=\"" + XmlBPMNProcessDumper.replaceIllegalCharsAttribute(ref) + "\"/>" + EOL);
		                writeInput(xmlDump, endNode);
		                endNode("endEvent", xmlDump);
		            } else if (s.startsWith(RUNTIME_SIGNAL_EVENT)) {
                        xmlDump.append(">" + EOL);
                        writeExtensionElements(endNode, xmlDump);
		                s = s.substring(44);
		                String type = s.substring(0, s.indexOf("\""));
		                xmlDump.append("      <signalEventDefinition signalRef=\"" + XmlBPMNProcessDumper.replaceIllegalCharsAttribute(type) + "\"/>" + EOL);
		                writeInput(xmlDump, endNode);
		                endNode("endEvent", xmlDump);
		            } else if (s.startsWith(PROCESS_INSTANCE_SIGNAL_EVENT) && "processInstance".equals(scope)) {
                        xmlDump.append(">" + EOL);
                        writeExtensionElements(endNode, xmlDump);
                        s = s.substring(43);
                        String type = s.substring(0, s.indexOf("\""));
                        xmlDump.append("      <signalEventDefinition signalRef=\"" + XmlBPMNProcessDumper.replaceIllegalCharsAttribute(type) + "\"/>" + EOL);
                        writeInput(xmlDump, endNode);
                        endNode("endEvent", xmlDump);
                    } else if (s.startsWith(PROCESS_INSTANCE_SIGNAL_EVENT)) {
		            	xmlDump.append(">" + EOL);
		                writeExtensionElements(endNode, xmlDump);
		                int begin =(PROCESS_INSTANCE_SIGNAL_EVENT + "Compensation\", ").length()-2; 
		            	int end = s.length() - 3;
		            	String compensationEvent = s.substring(begin, end);
		            	String activityRef = "";
		            	if( ! compensationEvent.startsWith(CompensationScope.IMPLICIT_COMPENSATION_PREFIX) ) { 
		            	    // specific
		            	    activityRef = "activityRef=\"" + XmlBPMNProcessDumper.replaceIllegalCharsAttribute(activityRef) + "\" ";
		            	} // else general: activityRef = "" (above) 
		            	xmlDump.append("      <compensateEventDefinition " + activityRef + "/>" + EOL);
		            	writeInput(xmlDump, endNode);
		                endNode("endEvent", xmlDump);
		            } else {
		                throw new IllegalArgumentException("Unknown action " + s);
		            }
                } else if (droolsAction instanceof JavaDroolsAction) {
                    JavaAction action = ((JavaDroolsAction) droolsAction).getAction();
                    xmlDump.append(">" + EOL);
                    writeExtensionElements(endNode, xmlDump);
                    writeJavaAction(endNode, action, xmlDump);
                    writeInput(xmlDump, endNode);
                    endNode("endEvent", xmlDump);
                }

		        }
		    } else {
		        endNode(xmlDump);
		    }
		}
	}

    private void writeInput(StringBuilder builder, EndNode node) {
        try(ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            throwEventWriter.write(stream, node.getInDataAssociations());
            builder.append(stream.toString());
        } catch(IOException e) {
            logger.info("Could not write catch event data");
        }
    }
}
