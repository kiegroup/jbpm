package org.jbpm.formbuilder.shared.rep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormRepresentation {

    private List<FBValidation> formValidations = new ArrayList<FBValidation>();
    private List<FormItemRepresentation> formItems = new ArrayList<FormItemRepresentation>();
    private Map<String, OutputData> outputs = new HashMap<String, OutputData>();
    private Map<String, InputData> inputs = new HashMap<String, InputData>();
    private Script onLoadScript;
    private Script onSubmitScript;
    
    
    public List<FBValidation> getFormValidations() {
        return formValidations;
    }

    public void setFormValidations(List<FBValidation> formValidations) {
        this.formValidations = formValidations;
    }

    public Map<String, InputData> getInputs() {
        return inputs;
    }

    public void setInputs(Map<String, InputData> inputs) {
        this.inputs = inputs;
    }

    public Map<String, OutputData> getOutputs() {
        return outputs;
    }

    public void setOutputs(Map<String, OutputData> outputs) {
        this.outputs = outputs;
    }
    
    public List<FormItemRepresentation> getFormItems() {
        return formItems;
    }
    
    public void setFormItems(List<FormItemRepresentation> formItems) {
        this.formItems = formItems;
    }
    
    public Script getOnLoadScript() {
        return onLoadScript;
    }
    
    public void setOnLoadScript(Script onLoadScript) {
        this.onLoadScript = onLoadScript;
    }
    
    public Script getOnSubmitScript() {
        return onSubmitScript;
    }
    
    public void setOnSubmitScript(Script onSubmitScript) {
        this.onSubmitScript = onSubmitScript;
    }
}
