package org.jbpm.formbuilder.shared.rep.items;

import java.util.Map;

import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;

public class ConditionalBlockRepresentation extends FormItemRepresentation {

    private FormItemRepresentation ifBlock;
    private FormItemRepresentation elseBlock;
    private String condition;
    
    public ConditionalBlockRepresentation() {
        super("conditionalBlock");
    }

    public FormItemRepresentation getIfBlock() {
        return ifBlock;
    }

    public void setIfBlock(FormItemRepresentation ifBlock) {
        this.ifBlock = ifBlock;
    }

    public FormItemRepresentation getElseBlock() {
        return elseBlock;
    }

    public void setElseBlock(FormItemRepresentation elseBlock) {
        this.elseBlock = elseBlock;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
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
