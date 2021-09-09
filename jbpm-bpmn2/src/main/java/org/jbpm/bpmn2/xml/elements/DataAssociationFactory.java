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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
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
import org.w3c.dom.Text;

public final class DataAssociationFactory {

    protected static final String LANG_EXPRESSION_ATTR = "language";
    protected static final String DEFAULT_DIALECT = "XPath";
    protected static final String USE_DEFINITION_LANGUAGE_PROPERTY = "org.kie.jbpm.bpmn2.useDefinitionLanguage";
    private static final String SOURCE_REF = "sourceRef";
    private static final String TARGET_REF = "targetRef";
    private static final String TRANSFORMATION = "transformation";
    private static final String ASSIGNMENT = "assignment";
    private static final Logger logger = LoggerFactory.getLogger(DataAssociationFactory.class);
    private static Map<String, Pattern> dialectPatterns = buildDialectPatterns(ProcessDialectRegistry.getDialects());

    private DataAssociationFactory() {}

    public static DataAssociation readDataOutputAssociation(Node xmlNode, Map<String, String> dataOutputs) {
        NodeList nodeList = xmlNode.getChildNodes();
        String source = null;
        String target = null;
        Transformation transformation = null;
        List<Assignment> assignment = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node subNode = nodeList.item(i);
            switch (subNode.getNodeName()) {
                case SOURCE_REF:
                    source = subNode.getTextContent();
                    String mapped = dataOutputs.get(source);
                    if (mapped == null) {
                        logger.warn("Data outputs in this node {} does not contain source {} not mapped", xmlNode.getAttributes().getNamedItem("id"), source);
                    }
                    else {
                        source = mapped;
                    }
                    break;
                case TARGET_REF:
                    target = subNode.getTextContent();
                    break;
                case TRANSFORMATION:
                    String lang = subNode.getAttributes().getNamedItem(LANG_EXPRESSION_ATTR).getNodeValue();
                    String expression = subNode.getTextContent();
                    transformation = new Transformation(lang, expression, source);
                    break;
                case ASSIGNMENT:
                    assignment.add(readAssignment(subNode, dataOutputs));
                    break;
            }
        }
        return new DataAssociation(source, target, assignment, transformation);
    }

    public static void readDataInputAssociation(Node xmlNode,
                                                Map<String, String> dataInputs,
                                                BiPredicate<String, List<Assignment>> isLegacy,
                                                Consumer<DataAssociation> addAssignment,
                                                BiConsumer<String, Object> legacyAssignment) {
        NodeList nodeList = xmlNode.getChildNodes();
        String source = null;
        String target = null;
        Transformation transformation = null;
        List<Assignment> assignments = new ArrayList<>();
        Node assignmentNode = null;
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node subNode = nodeList.item(i);
            switch (subNode.getNodeName()) {
                case SOURCE_REF:
                    source = subNode.getTextContent();
                    break;
                case TARGET_REF:
                    target = subNode.getTextContent();
                    String mapped = dataInputs.get(target);
                    if (mapped == null) {
                        logger.warn("Data inputs in this node {} does not contain target {}", xmlNode.getAttributes()
                                .getNamedItem("id"), target);
                    }
                    else {
                        target = mapped;
                    }
                    break;
                case TRANSFORMATION:
                    transformation = new Transformation(subNode.getAttributes().getNamedItem(LANG_EXPRESSION_ATTR)
                            .getNodeValue(), subNode.getTextContent());
                    break;
                case ASSIGNMENT:
                    assignmentNode = subNode;
                    assignments.add(readAssignment(subNode, dataInputs));
                    break;
            }
        }
        if (isLegacy.test(source, assignments))
        {
            Object value = getValue(assignmentNode);
            if (value != null) {
                legacyAssignment.accept(target, value);
            }
        } else {
            addAssignment.accept(new DataAssociation(source, target, assignments, transformation));
        }
    }

    public static boolean isLegacyAssignment(String source, List<Assignment> assignments) {
        return source == null && assignments.isEmpty() || source == null && !assignments.isEmpty() && assignments.get(0)
                .getDialect().equals(DEFAULT_DIALECT);
    }

    private static Object getValue(Node assignmentNode) {
        Object value = null;
        if (assignmentNode != null) {
            Node fromNode = assignmentNode.getFirstChild();
            NodeList nl = fromNode.getChildNodes();
            if (nl.getLength() > 1) {
                value = fromNode.getNodeValue();
            } else if (nl.getLength() == 1) {
                value = nl.item(0);
                if (value instanceof Text) {
                    String text = ((Text) value).getTextContent();
                    value = text.startsWith("\"") && text.endsWith("\"") ? text.substring(1, text.length() - 1)
                            : text;
                }
            }
        }
        return value;
    }


    public static List<Assignment> readAssignments(Node subNode) {
        List<Assignment> assignments = new LinkedList<>();
        while (subNode != null) {
            assignments.add(readAssignment(subNode));
            subNode = subNode.getNextSibling();
        }
        return assignments;
    }

    public static Assignment readAssignment(Node xmlNode) {
        return readAssignment(xmlNode, Collections.emptyMap());
    }

    public static Assignment readAssignment(Node xmlNode, Map<String, String> mapping) {
        NodeList nodeList = xmlNode.getChildNodes();
        Node from = null;
        Node to = null;
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node subNode = nodeList.item(i);
            switch (subNode.getNodeName()) {
                case "from":
                    from = subNode;
                    break;
                case "to":
                    to = subNode;
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized node name " + subNode.getNodeName() +
                                                       " in assigment " + xmlNode.getAttributes().getNamedItem("id")
                                                               .getNodeValue());
            }
        }
        if (to == null && from == null) {
            throw new IllegalArgumentException(" Assignment " + xmlNode.getAttributes().getNamedItem("id")
                    .getNodeValue() + " should have at least from or to subnode");
        }
        return new Assignment(getDialect(xmlNode, from, to), resolveContent(from, mapping), resolveContent(to,
                mapping));
    }

    private static String resolveContent(Node node, Map<String, String> mapping) {
        if (node == null) {
            return null;
        }
        String content = node.getTextContent();
        String mapped = mapping.get(content);
        return mapped == null ? content : mapped;
    }

    protected static String getDialect(Node node, Node from, Node to) {
        Collection<String> dialects = ProcessDialectRegistry.getDialects();
        if (!dialects.equals(dialectPatterns.keySet())) {
            dialectPatterns = buildDialectPatterns(dialects);
        }

        // trying to retrieve dialect from to or from overridden language
        String dialect = null;
        if (from != null) {
            dialect = findDialect(from.getAttributes().getNamedItem(LANG_EXPRESSION_ATTR));
        }
        if (dialect == null && to != null) {
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
        if (dialect == null && (from != null && PatternConstants.PARAMETER_MATCHER.matcher(from.getTextContent())
                .matches() ||
                                to != null && PatternConstants.PARAMETER_MATCHER.matcher(to.getTextContent())
                                        .matches())) {
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
