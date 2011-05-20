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
package org.jbpm.formbuilder.client;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.formbuilder.client.form.FBFormItem;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;

public class EditionView extends SimplePanel {

    private SimplePanel panel = new SimplePanel();
    
    public EditionView() {
        setSize("270px", "245px");
        Grid grid = new Grid(1,1);
        grid.setWidget(0, 0, panel);
        grid.setSize("100%", "100%");
        grid.setBorderWidth(2);
        add(grid);
    }
    
    public void populate(final FBFormItem itemSelected) {
        Map<String, Object> map = itemSelected.getFormItemPropertiesMap();
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
                itemSelected.saveValues(asPropertiesMap(grid));
            }
        });
        
        Button resetButton = new Button("Reset changes");
        resetButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                populate(itemSelected);
            }
        });
        
        grid.setWidget(index, 0, saveButton);
        grid.setWidget(index, 1, resetButton);
        
        panel.clear();
        panel.add(grid);
    }

    @Override
    public void clear() {
        panel.clear();
    }
    
    private Map<String, Object> asPropertiesMap(Grid grid) {
        Map<String, Object> map = new HashMap<String, Object>();
        for (int row = 1; row < grid.getRowCount() - 1; row++) {
            map.put(grid.getHTML(row, 0), grid.getHTML(row, 1));
        }
        return map;
    }
    
}
