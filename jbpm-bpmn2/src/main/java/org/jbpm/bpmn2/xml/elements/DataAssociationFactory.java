/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.bpmn2.xml.elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jbpm.workflow.core.node.Assignment;
import org.jbpm.workflow.core.node.DataAssociation;
import org.jbpm.workflow.core.node.Transformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class DataAssociationFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(DataAssociationFactory.class);
    
    private DataAssociationFactory() {
        // do nothing
    }

    public static DataAssociation readDataOutputAssociation(Node xmlNode, Map<String, String> dataOutputs) {

        NodeList nodeList = xmlNode.getChildNodes();
        String source = null;
        String target = null;
        Transformation transformation = null;
        List<Assignment> assignment = new ArrayList<>();
        for(int i = 0; i < nodeList.getLength(); i++) {
            Node subNode = nodeList.item(i);
            switch(subNode.getNodeName()) {
                case "sourceRef":
                {
                    source = subNode.getTextContent();
                    if(!dataOutputs.containsKey(source)) {
                        logger.warn("Data outputs in this node {} does not contain source {}", xmlNode.getAttributes().getNamedItem("id"), source);
                    }
                    // indirection
                    source = dataOutputs.get(source);
                    break;
                }
                case "targetRef":
                {
                    target = subNode.getTextContent();
                    break;
                }
                case "transformation":
                {
                    String lang = subNode.getAttributes().getNamedItem("language").getNodeValue();
                    String expression = subNode.getTextContent();
                    transformation = new Transformation(lang, expression, source);
                    break;
                }
                case "assignment":
                {
                    assignment.add(readAssignment(subNode));
                    break;
                }
            }

        }
        return new DataAssociation(source, target, assignment, transformation);
    }
    
    private static Assignment readAssignment(Node xmlNode) {
        NodeList nodeList = xmlNode.getChildNodes();
        String from = null;
        String to = null;
        for(int i = 0; i < nodeList.getLength(); i++) {
            Node subNode = nodeList.item(i);
            switch (subNode.getNodeName()) {
                case "from":
                    from = subNode.getTextContent();
                    break;
                case "to":
                    to = subNode.getTextContent();
                    break;
            }
        }
        return new Assignment("XPath", from, to);
    }

}
