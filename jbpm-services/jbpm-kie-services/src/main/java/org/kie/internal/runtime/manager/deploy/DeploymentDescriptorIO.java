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

package org.kie.internal.runtime.manager.deploy;

import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.kie.internal.runtime.conf.DeploymentDescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * XML based deployment descriptor IO manager to read and write descriptors.
 * Underlying uses <code>XStream</code> for serialization with special class
 * and field mapping for more readability of the produced XML output.
 *
 */
public class DeploymentDescriptorIO {

    private static final List<String> TOP_LEVEL_ELEMENT_ORDER = Arrays.asList(
            "persistence-unit",
            "audit-persistence-unit",
            "audit-mode",
            "persistence-mode",
            "runtime-strategy",
            "marshalling-strategies",
            "event-listeners",
            "task-event-listeners",
            "globals",
            "work-item-handlers",
            "environment-entries",
            "configurations",
            "required-roles",
            "remoteable-classes",
            "limit-serialization-classes"
    );

    private static final List<String> OBJECT_MODEL_CHILD_ORDER = Arrays.asList(
            "resolver",
            "identifier",
            "parameters"
    );

    private static final List<String> NAMED_OBJECT_MODEL_CHILD_ORDER = Arrays.asList(
            "resolver",
            "identifier",
            "parameters",
            "name"
    );

    private static final List<String> OBJECT_MODEL_ELEMENT_NAMES = Arrays.asList(
            "marshalling-strategy",
            "event-listener",
            "task-event-listener",
            "objectModel"
    );

    private static final List<String> NAMED_OBJECT_MODEL_ELEMENT_NAMES = Arrays.asList(
            "work-item-handler",
            "global",
            "environment-entry",
            "configuration",
            "namedObjectModel"
    );

    private static JAXBContext context = null;
    private static Schema schema = null;

    /**
     * Reads XML data from given input stream and produces valid instance of
     * <code>DeploymentDescriptor</code>
     * @param inputStream input stream that comes with xml data of the descriptor
     * @return instance of the descriptor after deserialization
     */
    public static DeploymentDescriptor fromXml(InputStream inputStream) {
        try {
            Unmarshaller unmarshaller = getContext().createUnmarshaller();
            unmarshaller.setSchema(schema);
            DeploymentDescriptor descriptor = (DeploymentDescriptor) unmarshaller.unmarshal(inputStream);

            return descriptor;
        } catch (Exception e) {
            throw new RuntimeException("Unable to read deployment descriptor from xml", e);
        }
    }

    /**
     * Serializes descriptor instance to XML
     * @param descriptor descriptor to be serialized
     * @return xml representation of descriptor as string
     */
    public static String toXml(DeploymentDescriptor descriptor) {
        try {

            Marshaller marshaller = getContext().createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "http://www.jboss.org/jbpm deployment-descriptor.xsd");

            // clone the object and cleanup transients
            DeploymentDescriptor clone = ((DeploymentDescriptorImpl) descriptor).clearClone();

            Document document = newDocument();
            marshaller.marshal(clone, document);

            // Normalize element order to keep the XML schema-compliant regardless of iteration order
            canonicalizeElementOrder(document.getDocumentElement());
            schema.newValidator().validate(new DOMSource(document));

            return toString(document);
        } catch (Exception e) {
            throw new RuntimeException("Unable to generate xml from deployment descriptor", e);
        }
    }

    public static JAXBContext getContext() throws JAXBException, SAXException {
        if (context == null) {
            Class<?>[] jaxbClasses = {DeploymentDescriptorImpl.class};
            context = JAXBContext.newInstance(jaxbClasses);
            // load schema for validation
            URL schemaLocation = DeploymentDescriptorIO.class.getResource("/deployment-descriptor.xsd");
            schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(schemaLocation);
        }

        return context;
    }

    private static Document newDocument() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory.newDocumentBuilder().newDocument();
    }

    private static void canonicalizeElementOrder(Element element) {
        List<String> expectedOrder = expectedChildOrderFor(element);
        if (expectedOrder != null) {
            reorderChildren(element, expectedOrder);
        }

        for (Element child : directChildElements(element)) {
            canonicalizeElementOrder(child);
        }
    }

    private static List<String> expectedChildOrderFor(Element element) {
        String name = elementLocalName(element);
        if ("deployment-descriptor".equals(name)) {
            return TOP_LEVEL_ELEMENT_ORDER;
        }
        if (OBJECT_MODEL_ELEMENT_NAMES.contains(name)) {
            return OBJECT_MODEL_CHILD_ORDER;
        }
        if (NAMED_OBJECT_MODEL_ELEMENT_NAMES.contains(name)) {
            return NAMED_OBJECT_MODEL_CHILD_ORDER;
        }
        return null;
    }

    private static void reorderChildren(Element root, List<String> expectedOrder) {
        List<Element> children = directChildElements(root);
        if (children.isEmpty()) {
            return;
        }

        // Split children into expected buckets and a tail list preserving original order
        Set<String> expectedNames = new HashSet<>(expectedOrder);
        Map<String, List<Element>> expectedBuckets = new HashMap<>();
        List<Element> tail = new ArrayList<>();
        for (Element child : children) {
            String name = elementLocalName(child);
            if (expectedNames.contains(name)) {
                expectedBuckets.computeIfAbsent(name, k -> new ArrayList<>()).add(child);
            } else {
                tail.add(child);
            }
        }

        removeAllChildren(root);

        for (String name : expectedOrder) {
            List<Element> bucket = expectedBuckets.get(name);
            if (bucket != null) {
                for (Element el : bucket) {
                    root.appendChild(el);
                }
            }
        }
        // Append nodes not listed in expectedOrder, preserving their original order
        for (Element el : tail) {
            root.appendChild(el);
        }
    }

    private static List<Element> directChildElements(Element root) {
        List<Element> elements = new ArrayList<>();
        NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                elements.add((Element) node);
            }
        }
        return elements;
    }

    private static void removeAllChildren(Element root) {
        Node node = root.getFirstChild();
        while (node != null) {
            Node next = node.getNextSibling();
            root.removeChild(node);
            node = next;
        }
    }

    private static String elementLocalName(Element element) {
        String localName = element.getLocalName();
        return localName != null ? localName : element.getNodeName();
    }

    private static String toString(Document document) throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

        StringWriter stringWriter = new StringWriter();
        transformer.transform(new DOMSource(document), new StreamResult(stringWriter));
        return stringWriter.toString();
    }
}
