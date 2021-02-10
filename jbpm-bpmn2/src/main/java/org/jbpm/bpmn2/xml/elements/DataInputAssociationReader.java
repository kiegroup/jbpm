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

public class DataInputAssociationReader implements ElementReader<DataAssociation>{

    private AssignmentReader assignmentReader;
    
    
    public DataInputAssociationReader() {
        assignmentReader = new AssignmentReader();
    }
    
    @Override
    public DataAssociation read(Node xmlNode) {
        NodeList nodeList = xmlNode.getChildNodes();
        String source = null;
        String target = null;
        Transformation transformation = null;
        List<Assignment> assignment = new ArrayList<>();
        Map<String, String> dataMapping = (Map<String, String>) xmlNode.getUserData(ElementConstants.METADATA_DATA_MAPPING);
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node subNode = nodeList.item(i);
            switch (subNode.getNodeName()) {
                case ElementConstants.SOURCE_REF:
                    source = subNode.getTextContent();
                    break;
                case ElementConstants.TARGET_REF:
                    target = dataMapping.get(subNode.getTextContent());
                    break;
                case ElementConstants.TRANSFORMATION:
                    String lang = subNode.getAttributes().getNamedItem(ElementConstants.LANG_EXPRESSION_ATTR).getNodeValue();
                    String expression = subNode.getTextContent();
                    transformation = new Transformation(lang, expression, source);
                    break;
                case ElementConstants.ASSIGNMENT:
                    subNode.setUserData(ElementConstants.METADATA_DATA_MAPPING, dataMapping, null);
                    assignment.add(assignmentReader.read(subNode));
                    subNode.setUserData(ElementConstants.METADATA_DATA_MAPPING, null, null);
                    break;
            }
        }
        return new DataAssociation(source, target, assignment, transformation);
    }

}
