package org.jbpm.formbuilder.client.effect.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.shared.rep.FBValidation;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ValidationsEffectView extends VerticalPanel {

    private final EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
    
    private ListBox validationsAvailableList = new ListBox();
    private Grid editionGrid = new Grid(1, 1);
    private VerticalPanel editionPanel = new VerticalPanel();
    
    private boolean editingNew = false;
    
    private final List<FBValidation> currentValidations = new ArrayList<FBValidation>();
    private final Map<String, TextBox> currentValidationProperties = new HashMap<String, TextBox>();
    private FBValidation currentValidation = null;
    private final Map<String, FBValidation> availableValidations = new HashMap<String, FBValidation>();
    
    public ValidationsEffectView() {
        add(createValidationListPanel());
        add(crateValidationParametersPanel());
        add(createSelectedValidationsPanel());
        add(createButtonsPanel());
    }
    
    public void setAvailableValidations(List<FBValidation> availableValidations) {
        validationsAvailableList.clear();
        for (FBValidation validation : availableValidations) {
            this.availableValidations.put(validation.getName(), validation);
            validationsAvailableList.addItem(validation.getName(), validation.getName());
        }
    }

    private Panel createSelectedValidationsPanel() {
        return new VerticalPanel(); //TODO implement
    }
    
    private Panel createButtonsPanel() {
        return new VerticalPanel(); //TODO implement
    }
    
    private Panel crateValidationParametersPanel() {
        editionPanel.add(editionGrid);
        HorizontalPanel hPanel = new HorizontalPanel();
        Button resetButton = new Button("Reset");
        resetButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                if (editingNew) {
                    removeValidationFromTable(getLastValidationFromTable());
                }
                editionPanel.setVisible(false);
            }
        });
        hPanel.add(resetButton);
        Button okButton = new Button("Ok");
        okButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                saveCurrentValidationValues();
                editionPanel.setVisible(false);
            }
        });
        hPanel.add(okButton);
        editionPanel.add(hPanel);
        return editionPanel;
    }

    private Panel createValidationListPanel() {
        HorizontalPanel panel = new HorizontalPanel();
        panel.add(new Label("Validation Type:"));
        panel.add(validationsAvailableList);
        Button addValidationButton = new Button("Add Validation");
        addValidationButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                String validationKey = validationsAvailableList.getValue(validationsAvailableList.getSelectedIndex());
                FBValidation validation = availableValidations.get(validationKey).clone();
                setCurrentValidation(validation);
                addValidationToTable(validation);
                editionPanel.setVisible(true);
            }
        });
        panel.add(addValidationButton);
        return panel;
    }

    public void setCurrentValidation(FBValidation newValidation) {
        this.currentValidation = newValidation;
        // TODO Auto-generated method stub
        Map<String, String> validationProperties = newValidation.getPropertiesMap();
        int propertiesSize = (validationProperties == null ? 0 : validationProperties.size());
        editionGrid.resize(3, propertiesSize);
        currentValidationProperties.clear();
        if (validationProperties != null) {
            int index = 0;
            Iterator<Map.Entry<String, String>> iter = validationProperties.entrySet().iterator(); 
            for (; iter.hasNext(); index++) {
                Map.Entry<String, String> entry = iter.next();
                editionGrid.setWidget(index, 0, new Label(entry.getKey() + ": "));
                editionGrid.setWidget(index, 1, new HTML("&nbsp;&nbsp;&nbsp;"));
                TextBox textBox = new TextBox();
                textBox.setValue(entry.getValue());
                currentValidationProperties.put(entry.getKey(), textBox);
                editionGrid.setWidget(index, 2, textBox);
            }
        }
        editionPanel.setVisible(true);
    }

    private void saveCurrentValidationValues() {
        if (currentValidation != null) {
            for (Map.Entry<String, TextBox> entry : currentValidationProperties.entrySet()) {
                currentValidation.setProperty(entry.getKey(), entry.getValue().getValue());
            }
        }
    }
    
    public void addValidationToTable(FBValidation newValidation) {
        editingNew = true;
        // TODO Auto-generated method stub
        
    }

    private void removeValidationFromTable(FBValidation lastValidationFromTable) {
        editingNew = false;
        // TODO Auto-generated method stub
        
    }

    private FBValidation getLastValidationFromTable() {
        // TODO Auto-generated method stub
        return null;
    }
    
    public void addValidationType(String validationKey) {
        validationsAvailableList.addItem(validationKey, validationKey);
    }
}
