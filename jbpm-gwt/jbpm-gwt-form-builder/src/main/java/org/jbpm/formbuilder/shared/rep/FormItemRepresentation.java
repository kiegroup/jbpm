package org.jbpm.formbuilder.shared.rep;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.formbuilder.shared.rep.trans.LanguageException;

public abstract class FormItemRepresentation {
    
    private List<FBValidation> itemValidations = new ArrayList<FBValidation>();
    private OutputData output;
    private InputData input;
    
    private String width;
    private String height;

    public List<FBValidation> getItemValidations() {
        return itemValidations;
    }

    public void setItemValidations(List<FBValidation> itemValidations) {
        this.itemValidations = itemValidations;
    }

    public OutputData getOutput() {
        return output;
    }

    public void setOutput(OutputData output) {
        this.output = output;
    }

    public InputData getInput() {
        return input;
    }

    public void setInput(InputData input) {
        this.input = input;
    }
    
    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public abstract String translate(String language) throws LanguageException;
}
