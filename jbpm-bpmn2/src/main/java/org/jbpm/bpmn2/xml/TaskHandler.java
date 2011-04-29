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

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.drools.process.core.Work;
import org.drools.process.core.datatype.DataType;
import org.drools.process.core.datatype.impl.type.ObjectDataType;
import org.drools.process.core.impl.WorkImpl;
import org.drools.xml.ExtensibleXmlParser;
import org.jbpm.bpmn2.core.ItemDefinition;
import org.jbpm.bpmn2.core.SequenceFlow;
import org.jbpm.compiler.xml.ProcessBuildData;
import org.jbpm.workflow.core.Connection;
import org.jbpm.workflow.core.Node;
import org.jbpm.workflow.core.NodeContainer;
import org.jbpm.workflow.core.impl.ConnectionImpl;
import org.jbpm.workflow.core.impl.ConstraintImpl;
import org.jbpm.workflow.core.node.Assignment;
import org.jbpm.workflow.core.node.CompositeContextNode;
import org.jbpm.workflow.core.node.CompositeNode;
import org.jbpm.workflow.core.node.DataAssociation;
import org.jbpm.workflow.core.node.EndNode;
import org.jbpm.workflow.core.node.ForEachNode;
import org.jbpm.workflow.core.node.Join;
import org.jbpm.workflow.core.node.Split;
import org.jbpm.workflow.core.node.StartNode;
import org.jbpm.workflow.core.node.WorkItemNode;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class TaskHandler extends AbstractNodeHandler {
    
	private Map<String, String> dataInputs = new HashMap<String, String>();
	private Map<String, String> dataOutputs = new HashMap<String, String>();

    protected Node createNode(Attributes attrs) {
        return new WorkItemNode();
    }
    
    @SuppressWarnings("unchecked")
	public Class generateNodeFor() {
        return Node.class;
    }

    protected void handleNode(final Node node, final Element element, final String uri, 
            final String localName, final ExtensibleXmlParser parser) throws SAXException {
    	super.handleNode(node, element, uri, localName, parser);
    	WorkItemNode workItemNode = (WorkItemNode) node;
        String name = getTaskName(element);
        Work work = new WorkImpl();
        work.setName(name);
    	workItemNode.setWork(work);
    	org.w3c.dom.Node xmlNode = element.getFirstChild();
        while (xmlNode != null) {
        	String nodeName = xmlNode.getNodeName();
        	if ("ioSpecification".equals(nodeName)) {
        		readIoSpecification(xmlNode, dataInputs, dataOutputs);
        	} else if ("dataInputAssociation".equals(nodeName)) {
        		readDataInputAssociation(xmlNode, workItemNode, dataInputs);
        	} else if ("dataOutputAssociation".equals(nodeName)) {
        		readDataOutputAssociation(xmlNode, workItemNode, dataOutputs);
        	}
    		xmlNode = xmlNode.getNextSibling();
        }
        handleScript(workItemNode, element, "onEntry");
        handleScript(workItemNode, element, "onExit");
	}
    
    protected String getTaskName(final Element element) {
        return element.getAttribute("taskName");
    }
    
    protected void readIoSpecification(org.w3c.dom.Node xmlNode, Map<String, String> dataInputs, Map<String, String> dataOutputs) {
    	org.w3c.dom.Node subNode = xmlNode.getFirstChild();
		while (subNode instanceof Element) {
			String subNodeName = subNode.getNodeName();
        	if ("dataInput".equals(subNodeName)) {
        		String id = ((Element) subNode).getAttribute("id");
        		String inputName = ((Element) subNode).getAttribute("name");
        		dataInputs.put(id, inputName);
        	}
        	if ("dataOutput".equals(subNodeName)) {
        		String id = ((Element) subNode).getAttribute("id");
        		String outputName = ((Element) subNode).getAttribute("name");
        		dataOutputs.put(id, outputName);
        	}
        	subNode = subNode.getNextSibling();
		}
    }

    protected void readDataInputAssociation(org.w3c.dom.Node xmlNode, WorkItemNode workItemNode, Map<String, String> dataInputs) {
		// sourceRef
		org.w3c.dom.Node subNode = xmlNode.getFirstChild();
		if ("sourceRef".equals(subNode.getNodeName())) {
    		String source = subNode.getTextContent();
    		// targetRef
    		subNode = subNode.getNextSibling();
    		String target = subNode.getTextContent();
    		subNode = subNode.getNextSibling();
    		List<Assignment> assignments = new LinkedList<Assignment>();
    		while(subNode != null){
    			org.w3c.dom.Node ssubNode = subNode.getFirstChild();
    			String from = ssubNode.getTextContent();
    			String to = ssubNode.getNextSibling().getTextContent();
    			assignments.add(new Assignment(null, from, to));

        		subNode = subNode.getNextSibling();
    		}
    		workItemNode.addInAssociation(new DataAssociation(
    				source,
    				dataInputs.get(target), assignments, null));
		} else {
			// targetRef
			String to = subNode.getTextContent();
			// assignment
			subNode = subNode.getNextSibling();
    		org.w3c.dom.Node subSubNode = subNode.getFirstChild();
    		NodeList nl = subSubNode.getChildNodes();
    		if (nl.getLength() > 1) {
    		    // not supported ?
    		    workItemNode.getWork().setParameter(dataInputs.get(to), subSubNode.getTextContent());
    		    return;
    		} else if (nl.getLength() == 0) {
    		    return;
    		}
    		Object result = null;
    		Object from = nl.item(0);
    		if (from instanceof Text) {
    		    String text = ((Text) from).getTextContent();
    		    if (text.startsWith("\"") && text.endsWith("\"")) {
                    result = text.substring(1, text.length() -1);
    		    } else {
    		        result = text;
    		    }
			} else {
			    result = nl.item(0);
			}
    		workItemNode.getWork().setParameter(dataInputs.get(to), result);
		}
    }
    
    protected void readDataOutputAssociation(org.w3c.dom.Node xmlNode, WorkItemNode workItemNode, Map<String, String> dataOutputs) {
		// sourceRef
		org.w3c.dom.Node subNode = xmlNode.getFirstChild();
		if ("sourceRef".equals(subNode.getNodeName())) {
		String source = subNode.getTextContent();
		// targetRef
		subNode = subNode.getNextSibling();
		String target = subNode.getTextContent();
		subNode = subNode.getNextSibling();
		List<Assignment> assignments = new LinkedList<Assignment>();
		while(subNode != null){
			org.w3c.dom.Node ssubNode = subNode.getFirstChild();
			String from = ssubNode.getTextContent();
			String to = ssubNode.getNextSibling().getTextContent();
			assignments.add(new Assignment(null, from, to));

    		subNode = subNode.getNextSibling();
		}
		workItemNode.addOutAssociation(new DataAssociation(dataOutputs.get(source), target, assignments, null));
		} else {
			// targetRef
			String target = subNode.getTextContent();
			subNode = subNode.getNextSibling();
			List<Assignment> assignments = new LinkedList<Assignment>();
			while(subNode != null){
				org.w3c.dom.Node ssubNode = subNode.getFirstChild();
				String from = ssubNode.getTextContent();
				String to = ssubNode.getNextSibling().getTextContent();
    		    if (from.startsWith("\"") && from.endsWith("\"")) {
                    from = from.substring(1, from.length() -1);
    		    }
				assignments.add(new Assignment(null, from, to));

	    		subNode = subNode.getNextSibling();
			}
			workItemNode.addOutAssociation(new DataAssociation(new LinkedList<String>(), target, assignments, null));			
		}
    }

    @Override
    public void writeNode(Node node, StringBuilder xmlDump, int metaDataType) {
        throw new IllegalArgumentException(
            "Writing out should be handled by the WorkItemNodeHandler");
    }
    
    public Object end(final String uri, final String localName,
            final ExtensibleXmlParser parser) throws SAXException {
		final Element element = parser.endElementBuilder();
		Node node = (Node) parser.getCurrent();
		// determine type of event definition, so the correct type of node
		// can be generated
    	handleNode(node, element, uri, localName, parser);
		boolean found = false;
		org.w3c.dom.Node xmlNode = element.getFirstChild();
		while (xmlNode != null) {
			String nodeName = xmlNode.getNodeName();
			if ("multiInstanceLoopCharacteristics".equals(nodeName)) {
				// create new timerNode
				long id = node.getId();
				ForEachNode forEachNode = new ForEachNode(node);
				forEachNode.setId(id);
				forEachNode.setName(node.getName());
				forEachNode.setMetaData("UniqueId", ((WorkItemNode) node).getMetaData("UniqueId"));
				forEachNode.setInMapping(((WorkItemNode) node).getInAssociations());
				forEachNode.setOutMapping(((WorkItemNode) node).getOutAssociations());
				node = forEachNode;
				handleForEachNode(node, element, uri, localName, parser);
				found = true;
				break;
			}
			if ("standardLoopCharacteristics".equals(nodeName)) {
				CompositeNode composite = new CompositeNode();
				composite.setId(node.getId());
				composite.setName(node.getName());
				composite.setMetaData("UniqueId", node.getMetaData("UniqueId"));

				StartNode start = new StartNode();
				composite.addNode(start);

				Join join = new Join();
				join.setType(Join.TYPE_XOR);
				composite.addNode(join);

				Split split = new Split(Split.TYPE_XOR);
				composite.addNode(split);

				node.setId(4);
				composite.addNode(node);

				EndNode end = new EndNode();
				composite.addNode(end);
				end.setTerminate(false);

				new ConnectionImpl(
						composite.getNode(1), org.jbpm.workflow.core.Node.CONNECTION_DEFAULT_TYPE,
						composite.getNode(2), org.jbpm.workflow.core.Node.CONNECTION_DEFAULT_TYPE
				);
				new ConnectionImpl(
						composite.getNode(2), org.jbpm.workflow.core.Node.CONNECTION_DEFAULT_TYPE,
						composite.getNode(3), org.jbpm.workflow.core.Node.CONNECTION_DEFAULT_TYPE
				);
				Connection c1 = new ConnectionImpl(
						composite.getNode(3), org.jbpm.workflow.core.Node.CONNECTION_DEFAULT_TYPE,
						composite.getNode(4), org.jbpm.workflow.core.Node.CONNECTION_DEFAULT_TYPE
				);
				new ConnectionImpl(
						composite.getNode(4), org.jbpm.workflow.core.Node.CONNECTION_DEFAULT_TYPE,
						composite.getNode(2), org.jbpm.workflow.core.Node.CONNECTION_DEFAULT_TYPE
				);
				Connection c2 = new ConnectionImpl(
						composite.getNode(3), org.jbpm.workflow.core.Node.CONNECTION_DEFAULT_TYPE,
						composite.getNode(5), org.jbpm.workflow.core.Node.CONNECTION_DEFAULT_TYPE
				);

				start.setMetaData("hidden", true);
				join.setMetaData("hidden", true);
				split.setMetaData("hidden", true);
				end.setMetaData("hidden", true);

				ConstraintImpl cons1 = new ConstraintImpl();
				cons1.setDialect("XPath");
				cons1.setConstraint(xmlNode.getFirstChild().getTextContent());
				cons1.setType("code");
				split.setConstraint(c1, cons1);

				ConstraintImpl cons2 = new ConstraintImpl();
				cons2.setDialect("XPath");
				cons2.setConstraint("");
				cons2.setType("code");
				cons2.setDefault(true);
				split.setConstraint(c2, cons2);		        

				super.handleNode(node, element, uri, localName, parser);
				node = composite;
				found = true;
				break;
			}

			xmlNode = xmlNode.getNextSibling();
		}
		
		NodeContainer nodeContainer = (NodeContainer) parser.getParent();
		nodeContainer.addNode(node);
		return node;
	}

	protected void handleForEachNode(final Node node, final Element element, final String uri, 
            final String localName, final ExtensibleXmlParser parser) throws SAXException {
    	ForEachNode forEachNode = (ForEachNode) node;
    	org.w3c.dom.Node xmlNode = element.getFirstChild();
        while (xmlNode != null) {
            String nodeName = xmlNode.getNodeName();
            if ("multiInstanceLoopCharacteristics".equals(nodeName)) {
            	readMultiInstanceLoopCharacteristics(xmlNode, forEachNode, parser);
            }
            xmlNode = xmlNode.getNextSibling();
        }
    }

	protected void readMultiInstanceLoopCharacteristics(org.w3c.dom.Node xmlNode, ForEachNode forEachNode, ExtensibleXmlParser parser) {
        // sourceRef
        org.w3c.dom.Node subNode = xmlNode.getFirstChild();
        while (subNode != null) {
            String nodeName = subNode.getNodeName();
            if ("inputDataItem".equals(nodeName)) {
            	String variableName = ((Element) subNode).getAttribute("id");
            	String itemSubjectRef = ((Element) subNode).getAttribute("itemSubjectRef");
            	DataType dataType = null;
            	Map<String, ItemDefinition> itemDefinitions = (Map<String, ItemDefinition>)
	            	((ProcessBuildData) parser.getData()).getMetaData("ItemDefinitions");
		        if (itemDefinitions != null) {
		        	ItemDefinition itemDefinition = itemDefinitions.get(itemSubjectRef);
		        	if (itemDefinition != null) {
		        		dataType = new ObjectDataType(itemDefinition.getStructureRef());
		        	}
		        }
		        if (dataType == null) {
		        	dataType = new ObjectDataType("java.lang.Object");
		        }
                if (variableName != null && variableName.trim().length() > 0) {
                	forEachNode.setVariable(variableName, dataType);
                }
            }
            else if("loopDataInput".equals(nodeName)) {
                String inputVariable = subNode.getFirstChild().getTextContent();
                if (inputVariable != null && inputVariable.trim().length() > 0) {
                	forEachNode.setCollectionExpression(dataInputs.get(inputVariable));
                }
            }
            subNode = subNode.getNextSibling();
        }
    }
}
