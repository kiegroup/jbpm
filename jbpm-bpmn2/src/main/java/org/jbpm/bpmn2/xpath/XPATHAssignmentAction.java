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

package org.jbpm.bpmn2.xpath;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.jbpm.process.instance.impl.AssignmentAction;
import org.jbpm.process.instance.impl.AssignmentProducer;
import org.jbpm.workflow.core.node.Assignment;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.ProcessContext;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class XPATHAssignmentAction implements AssignmentAction {
	
	private Assignment assignment;
    private AssignmentProducer producer;
    private BiFunction<ProcessContext, NodeInstance, Object> source;
    private BiFunction<ProcessContext, NodeInstance, Object> target;
    private String sourceExpr;
    private String targetExpr;
	
    public XPATHAssignmentAction(Assignment assignment, String sourceExpr, String targetExpr,
                                 BiFunction<ProcessContext, NodeInstance, Object> source,
                                 BiFunction<ProcessContext, NodeInstance, Object> target, AssignmentProducer producer) {
		this.assignment = assignment;
		this.sourceExpr = sourceExpr;
		this.targetExpr = targetExpr;
		this.source = source;
		this.target = target;
		this.producer = producer;
	}

	public void execute(NodeInstance nodeInstance, ProcessContext context) throws Exception {
        String from = assignment.getFrom();
        String to = assignment.getTo();
        
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpathFrom = factory.newXPath();

        XPathExpression exprFrom = xpathFrom.compile(from);

        XPath xpathTo = factory.newXPath();

        XPathExpression exprTo = xpathTo.compile(to);

        Object target;
        Object source;
        
        source = this.source.apply(context, nodeInstance);
        target = this.target.apply(context, nodeInstance);
        
        
        Object targetElem = null;

        // now pick the leaf for this operation
        if (target != null) {
            org.w3c.dom.Node parent;
                parent = ((org.w3c.dom.Node) target).getParentNode();
                
                
            targetElem = exprTo.evaluate(parent, XPathConstants.NODE);
            
            if (targetElem == null) {
                throw new RuntimeException("Nothing was selected by the to expression " + to + " on " + targetExpr);
            }
        }
        NodeList nl = null;
        if (source instanceof org.w3c.dom.Node) {
             nl = (NodeList) exprFrom.evaluate(source, XPathConstants.NODESET);
        } else if (source instanceof String) {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.newDocument();
            //quirky: create a temporary element, use its nodelist
            Element temp = doc.createElementNS(null, "temp");
            temp.appendChild(doc.createTextNode((String) source));
            nl = temp.getChildNodes();
        } else if (source == null) {
            // don't throw errors yet ?
            throw new RuntimeException("Source value was null for source " + sourceExpr);
        }
        
        if (nl == null || nl.getLength() == 0) {
            throw new RuntimeException("Nothing was selected by the from expression " + from + " on " + sourceExpr);
        }
        for (int i = 0 ; i < nl.getLength(); i++) {
            
            if (!(targetElem instanceof org.w3c.dom.Node)) {
                if (nl.item(i) instanceof Attr) {
                    targetElem = ((Attr) nl.item(i)).getValue();
                } else if (nl.item(i) instanceof Text) {
                    targetElem = ((Text) nl.item(i)).getWholeText();
                } else {
                    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                    Document doc = builder.newDocument();
                    targetElem  = doc.importNode(nl.item(i), true);
                }
                target = targetElem;
            } else {
                org.w3c.dom.Node n  = ((org.w3c.dom.Node) targetElem).getOwnerDocument().importNode(nl.item(i), true);
                if (n instanceof Attr) {
                    ((Element) targetElem).setAttributeNode((Attr) n);
                } else {
                    ((org.w3c.dom.Node) targetElem).appendChild(n);
                }
            }
        }
        
        producer.accept(context, nodeInstance, target);
	}

}
