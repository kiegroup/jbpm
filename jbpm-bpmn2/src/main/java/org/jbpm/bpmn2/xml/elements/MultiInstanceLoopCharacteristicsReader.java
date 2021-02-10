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

import java.util.Map;

import org.jbpm.bpmn2.core.ItemDefinition;
import org.jbpm.bpmn2.core.MultiInstanceLoopCharacteristics;
import org.jbpm.compiler.xml.ProcessBuildData;
import org.jbpm.process.core.datatype.DataType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MultiInstanceLoopCharacteristicsReader implements ElementReader<MultiInstanceLoopCharacteristics> {

    @Override
    public MultiInstanceLoopCharacteristics read(Node xmlNode) {
        MultiInstanceLoopCharacteristics multiInstanceLoopCharacteristics = new MultiInstanceLoopCharacteristics();
        Map<String, ItemDefinition> itemDefinitions = (Map<String, ItemDefinition>) xmlNode.getUserData(ElementConstants.METADATA_ITEMS_DEFINITIONS);
        Map<String, String> inputDataMapping = (Map<String, String>) xmlNode.getUserData(ElementConstants.METADATA_DATA_INPUT);
        Map<String, String> outputDataMapping = (Map<String, String>) xmlNode.getUserData(ElementConstants.METADATA_DATA_OUTPUT);
        ClassLoader classLoader = (ClassLoader) xmlNode.getUserData(ElementConstants.METADATA_CLASSLOADER);

        // sourceRef
        org.w3c.dom.Node subNode = xmlNode.getFirstChild();
        while (subNode != null) {
            String nodeName = subNode.getNodeName();
            if ("inputDataItem".equals(nodeName)) {
                String variableName = ((Element) subNode).getAttribute("id");
                String itemSubjectRef = ((Element) subNode).getAttribute("itemSubjectRef");
                DataType dataType = DataTypesUtil.getDataType(itemDefinitions, classLoader, itemSubjectRef);

                if (variableName != null && variableName.trim().length() > 0) {
                    multiInstanceLoopCharacteristics.addInputVariable(variableName, dataType);
                }
            } else if ("outputDataItem".equals(nodeName)) {
                String variableName = ((Element) subNode).getAttribute("id");
                String itemSubjectRef = ((Element) subNode).getAttribute("itemSubjectRef");
                DataType dataType = DataTypesUtil.getDataType(itemDefinitions, classLoader, itemSubjectRef);
                if (variableName != null && variableName.trim().length() > 0) {
                    multiInstanceLoopCharacteristics.addOutputVariable(variableName, dataType);
                }
            } else if ("loopDataOutputRef".equals(nodeName)) {
                String outputDataRef = ((Element) subNode).getTextContent();

                if (outputDataRef != null && outputDataRef.trim().length() > 0) {
                    String collectionName = null; //outputAssociation.get(outputDataRef);
                    if (collectionName == null) {
                        collectionName = outputDataMapping.get(outputDataRef);
                    }
                    multiInstanceLoopCharacteristics.setOutputCollectionExpression(collectionName);

                }
//                forEachNode.setMetaData("MICollectionOutput", outputDataRef);

            } else if ("loopDataInputRef".equals(nodeName)) {

                String inputDataRef = ((Element) subNode).getTextContent();

                if (inputDataRef != null && inputDataRef.trim().length() > 0) {
                    String collectionName = null; //inputAssociation.get(inputDataRef);
                    if (collectionName == null) {
                        collectionName = inputDataMapping.get(inputDataRef);
                    }
                    multiInstanceLoopCharacteristics.setInputCollectionExpression(collectionName);

                }
//                forEachNode.setMetaData("MICollectionInput", inputDataRef);

            } else if ("completionCondition".equals(nodeName)) {
                String expression = subNode.getTextContent();
                multiInstanceLoopCharacteristics.setCompletionConditionExpression(expression);
            }
            subNode = subNode.getNextSibling();
        }

        return multiInstanceLoopCharacteristics;
    }

}
