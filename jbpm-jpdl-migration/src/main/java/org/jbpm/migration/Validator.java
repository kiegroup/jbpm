/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.migration;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This class validates an XML file (like a process definition) against the applicable XML schema.
 * <p>
 * TODO: Make jPDL version support flexible (now only 3.2 is supported).<br>
 * 
 * @author Eric D. Schabell
 * @author Maurice de Chateau
 */
final class Validator {
    /* XML Schema file for jPDL version 3.2 on the classpath. */
    private static final String JPDL_3_2_SCHEMA = "jpdl-3.2.xsd";
    /* XML Schema files for BPMN version 2.0 on the classpath. */
    private static final String BPMN_2_0_SCHEMA = "BPMN20.xsd";

    /** Logging facility. */
    private static final Logger LOGGER = LoggerFactory.getLogger(Validator.class);

    /** Private constructor to enforce non-instantiability. */
    private Validator() {
    }

    /**
     * Load a process definition from file.
     * 
     * @param def
     *            The {@link File} which contains a definition.
     * @return The definition in {@link Document} format, or <code>null</code> if the file could not be found or didn't contain parseable XML.
     */
    static Document loadDefinition(final File def) {
        // Parse the jDPL definition into a DOM tree.
        final Document document = XmlUtils.parseFile(def);
        if (document == null) {
            return null;
        }

        // Log the jPDL version from the process definition (if applicable and available).
        final Node xmlnsNode = document.getFirstChild().getAttributes().getNamedItem("xmlns");
        if (xmlnsNode != null && StringUtils.isNotBlank(xmlnsNode.getNodeValue()) && xmlnsNode.getNodeValue().contains("jpdl")) {
            final String version = xmlnsNode.getNodeValue().substring(xmlnsNode.getNodeValue().length() - 3);
            LOGGER.info("jPDL version == " + version);
        }

        return document;
    }

    /**
     * Validate a given jPDL process definition against the applicable definition language's schema.
     * 
     * @param def
     *            The process definition, in {@link Document} format.
     * @param language
     *            The process definition language for which the given definition is to be validated.
     * @return Whether the validation was successful.
     */
    static boolean validateDefinition(final Document def, final ProcessLanguage language) {
        return XmlUtils.validate(new DOMSource(def), language.getSchemaSources());
    }

    /**
     * Validate a given jPDL process definition against the applicable definition language's schema.
     * 
     * @param def
     *            The process definition, in {@link String} format.
     * @param language
     *            The process definition language for which the given definition is to be validated.
     * @return Whether the validation was successful.
     */
    static boolean validateDefinition(final String def, final ProcessLanguage language) {
        return XmlUtils.validate(new StreamSource(new StringReader(def)), language.getSchemaSources());
    }

    enum ProcessLanguage {
        JPDL(JPDL_3_2_SCHEMA), BPMN(BPMN_2_0_SCHEMA);

        private final List<String> schemas = new ArrayList<String>();

        private ProcessLanguage(final String... schemas) {
            for (final String schema : schemas) {
                this.schemas.add(schema);
            }
        }

        Source[] getSchemaSources() {
            final Source[] sources = new Source[schemas.size()];
            for (final String schema : schemas) {
                sources[schemas.indexOf(schema)] = new StreamSource(Thread.currentThread().getContextClassLoader().getResourceAsStream(schema));
            }
            return sources;
        }
    }
}
