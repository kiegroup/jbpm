/**
 * Copyright 2011 JBoss Inc 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.formbuilder.client.effect.view;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.formbuilder.client.messages.Constants;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.client.validation.FBValidationItem;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class ValidationTablePanel extends VerticalPanel implements HasSelectionHandlers<FBValidationItem> {

    private List<SelectionHandler<FBValidationItem>> tableHandlers = new ArrayList<SelectionHandler<FBValidationItem>>();
    
    private final Constants i18n = FormBuilderGlobals.getInstance().getI18n();
    private final Grid validationsTable = new Grid(1,1);
    private final List<FBValidationItem> currentValidations = new ArrayList<FBValidationItem>();
    private final Button removeButton = new Button(i18n.ValidationRemove());
    private final Button moveUpButton = new Button(i18n.ValidationMoveUp());
    private final Button moveDownButton = new Button(i18n.ValidationModeDown());
    
    private FBValidationItem selectedValidation = null;
    
    public ValidationTablePanel() {
        validationsTable.setWidget(0, 0, new HTML("<strong>" + i18n.CurrentValidations() + "</strong>"));
        validationsTable.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                for (Widget widget : validationsTable) {
                    widget.removeStyleName("selectedValidationRow");
                }
                int row = getSelectedRow(event);
                if (row > 0) {
                    Widget selectedWidget = validationsTable.getWidget(row, 0);
                    if (selectedWidget.getStyleName().contains("selectedValidationRow")) {
                        selectedWidget.removeStyleName("selectedValidationRow");
                        setCurrentValidation(null);
                        fireSelectedValidation();
                        removeButton.setEnabled(false);
                        moveUpButton.setEnabled(false);
                        moveDownButton.setEnabled(false);
                    } else {
                        selectedWidget.addStyleName("selectedValidationRow");
                        setCurrentValidation(currentValidations.get(row - 1));
                        fireSelectedValidation();
                        removeButton.setEnabled(true);
                        moveUpButton.setEnabled(currentValidations.size() > 1);
                        moveDownButton.setEnabled(currentValidations.size() > 1);
                    }
                }
            }
        });
        add(validationsTable);
        HorizontalPanel tableButtonsPanel = new HorizontalPanel();
        removeButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                removeValidation(selectedValidation);
            }
        });
        moveUpButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                moveSelectedValidation(true);
                moveDownButton.setEnabled(currentValidations.indexOf(selectedValidation) > 0);
                moveUpButton.setEnabled(currentValidations.indexOf(selectedValidation) < currentValidations.size());
            }
        });
        moveDownButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                moveSelectedValidation(false);
                moveDownButton.setEnabled(currentValidations.indexOf(selectedValidation) > 0);
                moveUpButton.setEnabled(currentValidations.indexOf(selectedValidation) < currentValidations.size());
            }
        });
        tableButtonsPanel.add(removeButton);
        tableButtonsPanel.add(moveUpButton);
        tableButtonsPanel.add(moveDownButton);
        removeButton.setEnabled(false);
        moveUpButton.setEnabled(false);
        moveDownButton.setEnabled(false);
        add(tableButtonsPanel);
    }
    
    public void setCurrentValidation(FBValidationItem validation) {
        this.selectedValidation = validation;
    }
    
    public HandlerRegistration addSelectionHandler(final SelectionHandler<FBValidationItem> handler) {
        HandlerRegistration handlerRegistration = new HandlerRegistration() {
            public void removeHandler() {
                if (tableHandlers.contains(handler)) {
                    tableHandlers.remove(handler);
                }
            }
        };
        if (!tableHandlers.contains(handler)) {
            tableHandlers.add(handler);
        }
        return handlerRegistration;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void fireEvent(GwtEvent<?> event) {
        for (SelectionHandler<FBValidationItem> handler : tableHandlers) {
            handler.onSelection((SelectionEvent<FBValidationItem>) event);
        }
    }
    
    protected void fireSelectedValidation() {
        SelectionEvent.fire(this, this.selectedValidation);
    }
    
    public void addValidation(FBValidationItem validation) {
        if (!currentValidations.contains(validation)) {
            int rowCount = validationsTable.getRowCount();
            validationsTable.resizeRows(rowCount + 1);
            validationsTable.setWidget(rowCount, 0, new Label(validation.getName()));
            currentValidations.add(validation);
            if (!isVisible()) {
                setVisible(true);
            }
        }
    }
    
    public int getSelectedRow(ClickEvent event) {
        return validationsTable.getCellForEvent(event).getRowIndex();
    }
    
    public void removeValidation(FBValidationItem validation) {
        currentValidations.remove(validation);
    }
    
    public void moveSelectedValidation(boolean up) {
        int index = currentValidations.indexOf(selectedValidation);
        if (up) {
            FBValidationItem supValidation = currentValidations.get(index + 1);
            currentValidations.set(index + 1, selectedValidation);
            currentValidations.set(index, supValidation);
        } else {
            FBValidationItem subValidation = currentValidations.get(index - 1);
            currentValidations.set(index - 1, selectedValidation);
            currentValidations.set(index, subValidation);
        }
    }

    public List<FBValidationItem> getCurrentValidations() {
        return currentValidations;
    }
}
