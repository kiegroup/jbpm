package org.jbpm.formbuilder.shared.rep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormRepresentation {

    private String name;
    private String taskId;
    private List<FBValidation> formValidations = new ArrayList<FBValidation>();
    private List<FormItemRepresentation> formItems = new ArrayList<FormItemRepresentation>();
    private Map<String, OutputData> outputs = new HashMap<String, OutputData>();
    private Map<String, InputData> inputs = new HashMap<String, InputData>();
    private FBScript onLoadScript;
    private FBScript onSubmitScript;
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public List<FBValidation> getFormValidations() {
        return formValidations;
    }

    public void setFormValidations(List<FBValidation> formValidations) {
        this.formValidations = formValidations;
    }
    
    public void addFormValidation(FBValidation formValidation) {
        this.formValidations.add(formValidation);
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
    
    public void addFormItem(FormItemRepresentation formItem) {
        this.formItems.add(formItem);
    }
    
    public FBScript getOnLoadScript() {
        return onLoadScript;
    }
    
    public void setOnLoadScript(FBScript onLoadScript) {
        this.onLoadScript = onLoadScript;
    }
    
    public FBScript getOnSubmitScript() {
        return onSubmitScript;
    }
    
    public void setOnSubmitScript(FBScript onSubmitScript) {
        this.onSubmitScript = onSubmitScript;
    }
}
