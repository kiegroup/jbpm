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
package org.jbpm.formbuilder.client.edition;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.formbuilder.client.bus.UndoableEvent;
import org.jbpm.formbuilder.client.bus.UndoableEventHandler;
import org.jbpm.formbuilder.client.command.DisposeDropController;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;

public class EditionView extends ScrollPanel {

    private SimplePanel panel = new SimplePanel();
    
    public EditionView() {
        setSize("100%", "100%");
        setAlwaysShowScrollBars(false);
        panel.setSize("100%", "100%");
        add(panel);
        
        PickupDragController dragController = FormBuilderGlobals.getInstance().getDragController();
        dragController.registerDropController(new DisposeDropController(this));
    }
    
    public void populate(final FBFormItem itemSelected) {
        final EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
        final Map<String, Object> map = itemSelected.getFormItemPropertiesMap();
        final Grid grid = new Grid(map.size() + 2, 2);
        grid.setWidget(0, 0, new HTML("<strong>Property Name</strong>"));
        grid.setWidget(0, 1, new HTML("<strong>Property Value</strong>"));
        int index = 1;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            grid.setWidget(index, 0, new Label(entry.getKey()));
            TextBox textBox = new TextBox();
            textBox.setText(entry.getValue() == null ? "" : entry.getValue().toString());
            grid.setWidget(index, 1, textBox);
            index++;
        }
        Button saveButton = new Button("Save changes");
        saveButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                Map<String, Object> dataSnapshot = new HashMap<String, Object>();
                Map<String, Object> newItems = asPropertiesMap(grid);
                dataSnapshot.put("oldItems", map);
                dataSnapshot.put("newItems", newItems);
                dataSnapshot.put("itemSelected", itemSelected);
                bus.fireEvent(new UndoableEvent(dataSnapshot, new UndoableEventHandler() {
                    public void onEvent(UndoableEvent event) {  }
                    @SuppressWarnings("unchecked")
                    public void undoAction(UndoableEvent event) {
                        FBFormItem itemSelected = (FBFormItem) event.getData("itemSelected");
                        itemSelected.saveValues((Map<String, Object>) event.getData("oldItems"));
                    }
                    @SuppressWarnings("unchecked")
                    public void doAction(UndoableEvent event) {
                        FBFormItem itemSelected = (FBFormItem) event.getData("itemSelected");
                        itemSelected.saveValues((Map<String, Object>) event.getData("newItems"));
                    }
                }));
            }
        });
        
        Button resetButton = new Button("Reset changes");
        resetButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                Map<String, Object> dataSnapshot = new HashMap<String, Object>();
                dataSnapshot.put("newItems", asPropertiesMap(grid));
                dataSnapshot.put("fakeItemSelected", itemSelected.cloneItem());
                bus.fireEvent(new UndoableEvent(dataSnapshot, new UndoableEventHandler() {
                    public void onEvent(UndoableEvent event) {  }
                    @SuppressWarnings("unchecked")
                    public void undoAction(UndoableEvent event) {
                        FBFormItem itemSelected = (FBFormItem) event.getData("fakeItemSelected");
                        itemSelected.saveValues((Map<String, Object>) event.getData("newItems"));
                        populate(itemSelected);
                    }
                    public void doAction(UndoableEvent event) {
                        FBFormItem itemSelected = (FBFormItem) event.getData("fakeItemSelected");
                        populate(itemSelected);
                    }
                }));
            }
        });
        
        grid.setWidget(index, 0, saveButton);
        grid.setWidget(index, 1, resetButton);
        
        panel.clear();
        panel.add(grid);
        setScrollPosition(0);
    }

    @Override
    public void clear() {
        panel.clear();
    }
    
    private Map<String, Object> asPropertiesMap(Grid grid) {
        Map<String, Object> map = new HashMap<String, Object>();
        for (int row = 1; row < grid.getRowCount() - 1; row++) {
            map.put(((Label) grid.getWidget(row, 0)).getText(), ((TextBox) grid.getWidget(row, 1)).getValue());
        }
        return map;
    }
    
}
