package org.jbpm.formbuilder.shared.rep.items;

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
}
