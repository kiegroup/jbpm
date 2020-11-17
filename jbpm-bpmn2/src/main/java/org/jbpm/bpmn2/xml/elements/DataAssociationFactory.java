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
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jbpm.process.builder.dialect.ProcessDialectRegistry;
import org.jbpm.util.PatternConstants;
import org.jbpm.workflow.core.node.Assignment;
import org.jbpm.workflow.core.node.DataAssociation;
import org.jbpm.workflow.core.node.Transformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class DataAssociationFactory {
    
    protected static final String LANG_EXPRESSION_ATTR = "language";
    protected static final String DEFAULT_DIALECT = "XPath";
    protected static final String USE_DEFINITION_LANGUAGE_PROPERTY = "org.kie.jbpm.bpmn2.useDefinitionLanguage";
    private static final Logger logger = LoggerFactory.getLogger(DataAssociationFactory.class);
    
    private static Map<String, Pattern> dialectPatterns = buildDialectPatterns(ProcessDialectRegistry.getDialects());

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
                    String lang = subNode.getAttributes().getNamedItem(LANG_EXPRESSION_ATTR).getNodeValue();
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
    
    public static List<Assignment> readAssignments(Node subNode) {
        List<Assignment> assignments = new LinkedList<>();
        while (subNode != null) {
            assignments.add(DataAssociationFactory.readAssignment(subNode));
            subNode = subNode.getNextSibling();
        }
        return assignments;
    }

    public static Assignment readAssignment(Node xmlNode) {
        Node from = xmlNode.getFirstChild();
        if (from == null) {
            throw new IllegalArgumentException("missing from for assignment");
        }
        Node to = from.getNextSibling();
        if (to == null) {
            throw new IllegalArgumentException("missing to for assignment");
        }
        return new Assignment(getDialect(xmlNode, from, to), from.getTextContent(), to.getTextContent());
    }


    protected static String getDialect(Node node, Node from, Node to) {
        Collection<String> dialects = ProcessDialectRegistry.getDialects();
        if (!dialects.equals(dialectPatterns.keySet())) {
            dialectPatterns = buildDialectPatterns(dialects);
        }

        // trying to retrieve dialect from to or from overridden language
        String dialect = findDialect(from.getAttributes().getNamedItem(LANG_EXPRESSION_ATTR));
        if (dialect == null) {
            dialect = findDialect(to.getAttributes().getNamedItem(LANG_EXPRESSION_ATTR));
        }

        // there are some working process which declares MVEL in definition but use XPATH, in order
        // to prevent these files to fail, we check a flag (disable by default) before reading expression 
        // language from definition
        if (dialect == null && Boolean.getBoolean(USE_DEFINITION_LANGUAGE_PROPERTY)) {
            Node parentNode = node.getParentNode();
            while (parentNode != null && !parentNode.getLocalName().equals("Definitions")) {
                parentNode = parentNode.getParentNode();
            }
            if (parentNode != null) {
                dialect = findDialect(parentNode.getAttributes().getNamedItem("expressionLanguage"));
            }
        }
        // finally, if still not able to determine language, check if from or to contains a mvel expression 
        if (dialect == null && (PatternConstants.PARAMETER_MATCHER.matcher(from.getTextContent()).matches() ||
                                PatternConstants.PARAMETER_MATCHER.matcher(to.getTextContent()).matches())) {
            dialect = "mvel";
        }
        return dialect == null ? DEFAULT_DIALECT : dialect;
    }

    private static String findDialect(Node languageAttr) {
        if (languageAttr != null) {
            for (Map.Entry<String, Pattern> dialect : dialectPatterns.entrySet()) {
                if (dialect.getValue().matcher(languageAttr.getNodeValue()).find()) {
                    return dialect.getKey();
                }
            }
        }
        return null;
    }

    private static Map<String, Pattern> buildDialectPatterns(Collection<String> dialects) {
        return dialects.stream().collect(
                Collectors.toMap(x -> x, dialect -> Pattern.compile("\\b" + dialect + "\\b",
                        Pattern.CASE_INSENSITIVE)));
    }
}
