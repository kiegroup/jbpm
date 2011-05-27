package org.jbpm.formbuilder.client.form;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.formbuilder.client.validation.FBValidationItem;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;

public class FBForm {

    private String name;
    private String taskId;
    private List<FBFormItem> formItems = new ArrayList<FBFormItem>();
    private List<FBValidationItem> validationItems = new ArrayList<FBValidationItem>();
    
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

    public List<FBFormItem> getFormItems() {
        return formItems;
    }

    public void setFormItems(List<FBFormItem> formItems) {
        this.formItems = formItems;
    }

    public List<FBValidationItem> getValidationItems() {
        return validationItems;
    }

    public void setValidationItems(List<FBValidationItem> validationItems) {
        this.validationItems = validationItems;
    }

    public void onLoad() {
        
    }
    
    public void onSubmit() {
        
    }
    
    public FormRepresentation createRepresentation() {
        FormRepresentation rep = new FormRepresentation();
        rep.setName(name);
        rep.setTaskId(taskId);
        for (FBFormItem item : formItems) {
            rep.addFormItem(item.getRepresentation());
        }
        for (FBValidationItem item : validationItems) {
            rep.addFormValidation(item.getRepresentation());
        }
        /* TODO rep.setInputs(inputs);
        rep.setOutputs(outputs);
        rep.setOnLoadScript(onLoadScript);
        rep.setOnSubmitScript(onSubmitScript); */
        return rep;
    }
}
