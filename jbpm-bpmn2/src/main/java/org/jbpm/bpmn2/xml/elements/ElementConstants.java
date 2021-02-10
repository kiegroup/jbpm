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

public final class ElementConstants {


    private ElementConstants() {
        // do nothign
    }
    protected static final String EOL = System.getProperty("line.separator");

    // properties
    public static final String USE_DEFINITION_LANGUAGE_PROPERTY = "org.kie.jbpm.bpmn2.useDefinitionLanguage";

    // defaults
    public static final String DEFAULT_DIALECT = "XPath";

    // elements
    public static final String DATA_OUTPUT_ASSOCIATION = "dataOutputAssociation";
    public static final String DATA_INPUT_ASSOCIATION = "dataInputAssociation";

    public static final String DATAOUTPUT= "dataOutput";
    public static final String DATAINPUT = "dataInput";
    public static final String ASSIGNMENT = "assignment";
    public static final String SOURCE_REF = "sourceRef";
    public static final String TARGET_REF = "targetRef";
    public static final String TRANSFORMATION = "transformation";

    // atributes
    public static final String LANG_EXPRESSION_ATTR = "language";

    // metadata
    public static final String METADATA_DATA_MAPPING = "dataMapping";
    public static final String METADATA_ITEMS_DEFINITIONS = "itemsDefinitions";
    public static final String METADATA_DATA_INPUT = "inputDataMapping";
    public static final String METADATA_DATA_OUTPUT = "outputDataMapping";
    public static final String METADATA_CLASSLOADER = "classLoader";
}
