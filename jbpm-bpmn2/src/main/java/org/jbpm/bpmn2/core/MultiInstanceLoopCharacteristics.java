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
