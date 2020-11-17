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

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DataAssociationFactoryTest {

    private Node node;
    private Node from;
    private Node to;
    private NamedNodeMap attrs;

    @Before
    public void setup() {
        node = mock(Node.class);
        attrs = mock(NamedNodeMap.class);
        from = mock(Node.class);
        to = mock(Node.class);
        when(from.getTextContent()).thenReturn(".");
        when(to.getTextContent()).thenReturn(".");
        when(from.getAttributes()).thenReturn(attrs);
        when(to.getAttributes()).thenReturn(attrs);
    }

    @Test
    public void testGetDialect() {
        assertEquals(DataAssociationFactory.DEFAULT_DIALECT, DataAssociationFactory.getDialect(node, to, from));
        Node attr = mock(Node.class);
        when(attrs.getNamedItem(DataAssociationFactory.LANG_EXPRESSION_ATTR)).thenReturn(attr);
        when(attr.getNodeValue()).thenReturn("http://www.mvel.org/2.0");
        assertEquals("mvel", DataAssociationFactory.getDialect(node, to, from));
        when(attr.getNodeValue()).thenReturn("java");
        assertEquals("java", DataAssociationFactory.getDialect(node, to, from));
        when(attr.getNodeValue()).thenReturn("javas");
        assertEquals("XPath", DataAssociationFactory.getDialect(node, to, from));
        when(attr.getNodeValue()).thenReturn("javascript");
        assertEquals("JavaScript", DataAssociationFactory.getDialect(node, to, from));
    }

    @Test
    public void testGetDialectFromDefinition() {
        Node parentNode = mock(Node.class);
        when(node.getParentNode()).thenReturn(parentNode);
        when(node.getAttributes()).thenReturn(mock(NamedNodeMap.class));
        when(parentNode.getAttributes()).thenReturn(attrs);
        when(parentNode.getLocalName()).thenReturn("Definitions");
        Node attr = mock(Node.class);
        when(attr.getNodeValue()).thenReturn("http://www.mvel.org/2.0");
        when(attrs.getNamedItem("expressionLanguage")).thenReturn(attr);
        assertEquals("XPath", DataAssociationFactory.getDialect(node, from, to));
        System.setProperty(DataAssociationFactory.USE_DEFINITION_LANGUAGE_PROPERTY, "true");
        try {
            assertEquals("mvel", DataAssociationFactory.getDialect(node, from, to));
        } finally {
            System.clearProperty(DataAssociationFactory.USE_DEFINITION_LANGUAGE_PROPERTY);
        }
    }

}
