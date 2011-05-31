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

import org.jbpm.formbuilder.client.form.FBForm;
import org.jbpm.formbuilder.client.form.LayoutFormItem;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class LayoutView extends SimplePanel {

    private FBForm formDisplay = new FBForm();
    
    public LayoutView() {
        formDisplay.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
        setSize("500px", "500px");
        Grid grid = new Grid(1,1);
        grid.setSize("100%", "100%");
        grid.setBorderWidth(2);
        grid.setWidget(0, 0, formDisplay);
        add(grid);
    }

    public Panel getUnderlyingLayout(int x, int y) {
        for (Widget widget : formDisplay) {
            if (widget instanceof LayoutFormItem) {
                LayoutFormItem item = (LayoutFormItem) widget;
                Panel newLayout = item.getUnderlyingLayout(x, y);
                if (newLayout != null) {
                    return newLayout;
                }
            }
        }
        return formDisplay; // TODO Implement a way to see what's in that position
    }
    
    public FBForm getFormDisplay() {
        return formDisplay;
    }
}
