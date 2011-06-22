package org.jbpm.formbuilder.client.effect.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.bus.ui.ItemValidationsEditedEvent;
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
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class ValidationsEffectView extends VerticalPanel {

    private final EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
    
    private ListBox validationsAvailableList = new ListBox();
    private Grid editionGrid = new Grid(1, 1);
    private VerticalPanel editionPanel = new VerticalPanel();
    private Grid currentValidationsList = new Grid(1,1);
    private PopupPanel parentPopup = null;
    
    private boolean editingNew = false;
    
    private final List<FBValidation> currentValidationsOrder = new ArrayList<FBValidation>();
    private final Map<String, FBValidation> currentValidations = new HashMap<String, FBValidation>();
    private final Map<String, TextBox> currentValidationProperties = new HashMap<String, TextBox>();
    private FBValidation currentValidation = null;
    private final Map<String, FBValidation> availableValidations = new HashMap<String, FBValidation>();
    
    public ValidationsEffectView() {
        add(createValidationListPanel());
        add(createValidationEditionPanel());
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
        currentValidationsList.setWidget(0, 0, new HTML("<strong>CurrentValdations</strong>"));
        for (FBValidation validation : currentValidations.values()) {
            int rowCount = currentValidationsList.getRowCount();
            currentValidationsList.resizeRows(rowCount + 1);
            currentValidationsList.setWidget(rowCount, 0, new Label(validation.getName()));
        }
        currentValidationsList.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                for (Widget widget : currentValidationsList) {
                    widget.removeStyleName("selectedValidationRow");
                }
                int row = currentValidationsList.getCellForEvent(event).getRowIndex();
                Label selectedWidget = (Label) currentValidationsList.getWidget(row, 0);
                selectedWidget.addStyleName("selectedValidationRow");
                setCurrentValidation(currentValidations.get(selectedWidget.getText()));
            }
        });
        VerticalPanel panel = new VerticalPanel();
        panel.add(currentValidationsList);
        HorizontalPanel tableButtonsPanel = new HorizontalPanel();
        Button removeButton = new Button("Remove");
        removeButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                removeValidationFromTable(currentValidation);
            }
        });
        final Button moveUpButton = new Button("Move up");
        final Button moveDownButton = new Button("Move down");
        moveUpButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                moveSelectedValidation(true);
                moveDownButton.setEnabled(currentValidationsOrder.indexOf(currentValidation) > 0);
                moveUpButton.setEnabled(currentValidationsOrder.indexOf(currentValidation) < currentValidationsOrder.size());
            }
        });
        moveDownButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                moveSelectedValidation(false);
                moveDownButton.setEnabled(currentValidationsOrder.indexOf(currentValidation) > 0);
                moveUpButton.setEnabled(currentValidationsOrder.indexOf(currentValidation) < currentValidationsOrder.size());
            }
        });
        tableButtonsPanel.add(removeButton);
        tableButtonsPanel.add(moveUpButton);
        tableButtonsPanel.add(moveDownButton);
        panel.add(tableButtonsPanel);
        return panel;
    }
    
    private Panel createButtonsPanel() {
        HorizontalPanel panel = new HorizontalPanel();
        Button applyButton = new Button("Apply");
        applyButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                bus.fireEventFromSource(new ItemValidationsEditedEvent(currentValidationsOrder), ValidationsEffectView.this);
                if (parentPopup != null) {
                    parentPopup.hide();
                }
            }
        });
        Button cancelButton = new Button("Cancel");
        cancelButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                if (parentPopup != null) {
                    parentPopup.hide();
                }
            }
        });
        panel.add(applyButton);
        panel.add(cancelButton);
        return panel; //TODO implement
    }
    
    private Panel createValidationEditionPanel() {
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
        currentValidations.put(newValidation.getName(), newValidation);
        currentValidationsOrder.add(newValidation);
    }

    public void moveSelectedValidation(boolean up) {
        int index = currentValidationsOrder.indexOf(currentValidation);
        if (up) {
            FBValidation supValidation = currentValidationsOrder.get(index + 1);
            currentValidationsOrder.set(index + 1, currentValidation);
            currentValidationsOrder.set(index, supValidation);
        } else {
            FBValidation subValidation = currentValidationsOrder.get(index - 1);
            currentValidationsOrder.set(index - 1, currentValidation);
            currentValidationsOrder.set(index, subValidation);
        }
    }
    
    private void removeValidationFromTable(FBValidation validation) {
        editingNew = false;
        currentValidations.remove(validation.getName());
        currentValidationsOrder.remove(validation);
    }

    private FBValidation getLastValidationFromTable() {
        return currentValidationsOrder.get(currentValidationsOrder.size() - 1);
    }
    
    public void addValidationType(String validationKey) {
        validationsAvailableList.addItem(validationKey, validationKey);
    }
    
    public void setParentPopup(PopupPanel popup) {
        this.parentPopup = popup;
    }
}
