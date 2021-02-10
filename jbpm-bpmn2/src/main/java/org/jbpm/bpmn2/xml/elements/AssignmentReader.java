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

import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jbpm.process.builder.dialect.ProcessDialectRegistry;
import org.jbpm.util.PatternConstants;
import org.jbpm.workflow.core.node.Assignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AssignmentReader implements ElementReader<Assignment> {

    private static final Logger logger = LoggerFactory.getLogger(AssignmentReader.class);

    private Map<String, Pattern> dialectPatterns;
    
    public AssignmentReader() {
        dialectPatterns = buildDialectPatterns(ProcessDialectRegistry.getDialects());
    }

    @Override
    public Assignment read(Node xmlNode) {
        Map<String, String> dataMapping = (Map<String, String>) xmlNode.getUserData(ElementConstants.METADATA_DATA_MAPPING);

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
        return new Assignment(getDialect(xmlNode, from, to), resolveContent(from, dataMapping), resolveContent(to, dataMapping));
    }

    private static String resolveContent(Node node, Map<String, String> mapping) {
        if (node == null) {
            return null;
        }
        String content = node.getTextContent();
        String mapped = mapping.get(content);
        return mapped == null ? content : mapped;
    }

    protected String getDialect(Node node, Node from, Node to) {
        Collection<String> dialects = ProcessDialectRegistry.getDialects();
        if (!dialects.equals(dialectPatterns.keySet())) {
            dialectPatterns = buildDialectPatterns(dialects);
        }

        // trying to retrieve dialect from to or from overridden language
        String dialect = null;
        if (from != null) {
            dialect = findDialect(from.getAttributes().getNamedItem(ElementConstants.LANG_EXPRESSION_ATTR));
        }
        if (dialect == null && to != null) {
            dialect = findDialect(to.getAttributes().getNamedItem(ElementConstants.LANG_EXPRESSION_ATTR));
        }

        // there are some working process which declares MVEL in definition but use XPATH, in order
        // to prevent these files to fail, we check a flag (disable by default) before reading expression 
        // language from definition
        if (dialect == null && Boolean.getBoolean(ElementConstants.USE_DEFINITION_LANGUAGE_PROPERTY)) {
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
        return dialect == null ? ElementConstants.DEFAULT_DIALECT : dialect;
    }

    private String findDialect(Node languageAttr) {
        if (languageAttr != null) {
            for (Map.Entry<String, Pattern> dialect : dialectPatterns.entrySet()) {
                if (dialect.getValue().matcher(languageAttr.getNodeValue()).find()) {
                    return dialect.getKey();
                }
            }
        }
        return null;
    }

    private Map<String, Pattern> buildDialectPatterns(Collection<String> dialects) {
        return dialects.stream().collect(
                Collectors.toMap(x -> x, dialect -> Pattern.compile("\\b" + dialect + "\\b",
                        Pattern.CASE_INSENSITIVE)));
    }
}
