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

import org.drools.rule.builder.dialect.java.JavaDialect;
import org.drools.xml.ExtensibleXmlParser;
import org.jbpm.workflow.core.Connection;
import org.jbpm.workflow.core.Node;
import org.jbpm.workflow.core.NodeContainer;
import org.jbpm.workflow.core.impl.ConnectionImpl;
import org.jbpm.workflow.core.impl.ConstraintImpl;
import org.jbpm.workflow.core.impl.DroolsConsequenceAction;
import org.jbpm.workflow.core.node.ActionNode;
import org.jbpm.workflow.core.node.CompositeContextNode;
import org.jbpm.workflow.core.node.CompositeNode;
import org.jbpm.workflow.core.node.EndNode;
import org.jbpm.workflow.core.node.ForEachNode;
import org.jbpm.workflow.core.node.Join;
import org.jbpm.workflow.core.node.Split;
import org.jbpm.workflow.core.node.StartNode;
import org.jbpm.workflow.core.node.WorkItemNode;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class ScriptTaskHandler extends AbstractNodeHandler {
    
    protected Node createNode(Attributes attrs) {
        ActionNode result = new ActionNode();
        result.setAction(new DroolsConsequenceAction());
        return result;
    }
    
    @SuppressWarnings("unchecked")
	public Class generateNodeFor() {
        return Node.class;
    }

    protected void handleNode(final Node node, final Element element, final String uri, 
            final String localName, final ExtensibleXmlParser parser) throws SAXException {
    	super.handleNode(node, element, uri, localName, parser);
        ActionNode actionNode = (ActionNode) node;
        DroolsConsequenceAction action = (DroolsConsequenceAction) actionNode.getAction();
        if (action == null) {
        	action = new DroolsConsequenceAction();
        	actionNode.setAction(action);
        }
		String language = element.getAttribute("scriptFormat");
		if (XmlBPMNProcessDumper.JAVA_LANGUAGE.equals(language)) {
			action.setDialect(JavaDialect.ID);
		}
		action.setConsequence("");
        org.w3c.dom.Node xmlNode = element.getFirstChild();
        while (xmlNode != null) {
        	if (xmlNode instanceof Element && "script".equals(xmlNode.getNodeName())) {
        		action.setConsequence(xmlNode.getTextContent());
        	}
        	xmlNode = xmlNode.getNextSibling();
        }
	}

	public void writeNode(Node node, StringBuilder xmlDump, int metaDataType) {
	    throw new IllegalArgumentException("Writing out should be handled by action node handler");
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


}
