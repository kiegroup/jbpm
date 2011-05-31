package org.jbpm.formbuilder.client.form;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.formbuilder.client.validation.FBValidationItem;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class FBForm extends VerticalPanel implements FBCompositeItem {

    private String name;
    private String taskId;
    private List<FBFormItem> formItems = new ArrayList<FBFormItem>();
    private List<FBValidationItem> validationItems = new ArrayList<FBValidationItem>();
    
    public FBForm() {
        super();
    }

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

    public List<FBFormItem> getItems() {
        return formItems;
    }

    public void setItems(List<FBFormItem> items) {
        this.formItems = items;
    }

    public List<FBValidationItem> getValidationItems() {
        return validationItems;
    }

    public void setValidationItems(List<FBValidationItem> validationItems) {
        this.validationItems = validationItems;
    }
    
    @Override
    public boolean remove(Widget w) {
        if (w instanceof FBFormItem) {
            this.formItems.remove((FBFormItem) w);
        }
        return super.remove(w);
    }
    
    @Override
    public void add(Widget w) {
        super.add(w);
        if (w instanceof FBFormItem) {
            this.formItems.add((FBFormItem) w);
        }
    }
    
    public void addValidation(FBValidationItem item) {
        this.validationItems.add(item);
    }

    public void onFormLoad() {
        
    }
    
    public void onFormSubmit() {
        
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
