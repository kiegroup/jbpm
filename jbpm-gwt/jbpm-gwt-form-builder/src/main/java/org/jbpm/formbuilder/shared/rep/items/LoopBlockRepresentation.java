package org.jbpm.formbuilder.shared.rep.items;

import java.util.Map;

import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;

public class LoopBlockRepresentation extends FormItemRepresentation {

    private String inputName;
    private String variableName;
    private FormItemRepresentation loopBlock;
    
    public LoopBlockRepresentation() {
        super("loopBlock");
    }

    public String getInputName() {
        return inputName;
    }

    public void setInputName(String inputName) {
        this.inputName = inputName;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public FormItemRepresentation getLoopBlock() {
        return loopBlock;
    }

    public void setLoopBlock(FormItemRepresentation loopBlock) {
        this.loopBlock = loopBlock;
    }
    
    @Override
    public Map<String, Object> getData() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public void setData(Map<String, Object> data) {
        // TODO Auto-generated method stub
        
    }
}
