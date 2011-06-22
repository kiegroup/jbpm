package org.jbpm.formbuilder.client.effect.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.validation.FBValidationItem;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;

public class ValidationListPanel extends HorizontalPanel {

    private static final String NULL_VALIDATION = "...";
    
    private final Button addValidationButton = new Button("Add Validation");
    private final ListBox validationsAvailableList = new ListBox();
    
    private Map<String, FBValidationItem> availableValidations = new HashMap<String, FBValidationItem>();
    
    public ValidationListPanel() {
        add(new Label("Validation Type:"));
        add(validationsAvailableList);
        add(addValidationButton);
    }
    
    public void onAdd(ClickHandler handler) {
        addValidationButton.addClickHandler(handler);
    }
    
    public FBValidationItem getValidationSelection() {
        String validationKey = validationsAvailableList.getValue(validationsAvailableList.getSelectedIndex());
        return (validationKey.equals(NULL_VALIDATION) ? null : availableValidations.get(validationKey).cloneItem());
    }
    
    public void setAvailableValidations(List<FBValidationItem> availableValidations) {
        this.validationsAvailableList.clear();
        this.availableValidations.clear();
        this.validationsAvailableList.addItem(NULL_VALIDATION);
        for (FBValidationItem validation : availableValidations) {
            this.availableValidations.put(validation.getName(), validation);
            this.validationsAvailableList.addItem(validation.getName(), validation.getName());
        }
    }

}
