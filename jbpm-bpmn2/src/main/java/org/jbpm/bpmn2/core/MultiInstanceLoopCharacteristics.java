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

package org.jbpm.bpmn2.core;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.process.core.datatype.DataType;

public class MultiInstanceLoopCharacteristics {

    private String completionConditionExpression;

    private Map<String, DataType> inputVariables;
    private Map<String, DataType> outputVariables;

    private String outputCollectionExpression;
    private String inputCollectionExpression;
    
    public MultiInstanceLoopCharacteristics() {
        inputVariables = new HashMap<>();
        outputVariables = new HashMap<>();
    }
    
    public void setCompletionConditionExpression(String completionConditionExpression) {
        this.completionConditionExpression = completionConditionExpression;
    }

    public String getCompletionConditionExpression() {
        return completionConditionExpression;
    }

    public void addInputVariable(String variableName, DataType dataType) {
        inputVariables.put(variableName, dataType);
        
    }

    public void addOutputVariable(String variableName, DataType dataType) {
        inputVariables.put(variableName, dataType);
    }

    
    public Map<String, DataType> getInputVariables() {
        return inputVariables;
    }

    public Map<String, DataType> getOutputVariables() {
        return outputVariables;
    }

    public void setOutputCollectionExpression(String outputCollectionExpression) {
        this.outputCollectionExpression = outputCollectionExpression;
    }


    public String getOutputCollectionExpression() {
        return outputCollectionExpression;
    }

    public String getInputCollectionExpression() {
        return inputCollectionExpression;
    }

    public void setInputCollectionExpression(String inputCollectionExpression) {
        this.inputCollectionExpression = inputCollectionExpression;
    }
}
