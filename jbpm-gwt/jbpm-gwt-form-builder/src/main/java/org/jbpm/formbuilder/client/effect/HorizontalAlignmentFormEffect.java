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
package org.jbpm.formbuilder.client.effect;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.formbuilder.client.bus.UndoableEvent;
import org.jbpm.formbuilder.client.bus.UndoableHandler;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Allows to set alignment
 */
public class HorizontalAlignmentFormEffect extends FBFormEffect {

    private ListBox alignmentBox = new ListBox();
    private EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
    
    public HorizontalAlignmentFormEffect() {
        super("Horizontal Alignment", true);
    }
    
    @Override
    protected void createStyles() {
        int index = this.alignmentBox.getSelectedIndex();
        String value = this.alignmentBox.getValue(index);
        Widget widget = getWidget();
        if (widget instanceof HasHorizontalAlignment) {
            HasHorizontalAlignment hw = (HasHorizontalAlignment) widget;
            HorizontalAlignmentConstant align = null;
            if ("left".equals(value)) {
                align = HasHorizontalAlignment.ALIGN_LEFT;
            } else if ("right".equals(value)) {
                align = HasHorizontalAlignment.ALIGN_RIGHT;
            } else if ("center".equals(value)) {
                align = HasHorizontalAlignment.ALIGN_CENTER;
            } else if ("justify".equals(value)) {
                align = HasHorizontalAlignment.ALIGN_JUSTIFY;
            }
            Map<String, Object> dataSnapshot = new HashMap<String, Object>();
            dataSnapshot.put("oldAlignment", hw.getHorizontalAlignment());
            dataSnapshot.put("newAlignment", align);
            dataSnapshot.put("hwidget", hw);
            bus.fireEvent(new UndoableEvent(dataSnapshot, new UndoableHandler() {
                public void onEvent(UndoableEvent event) {  }
                public void undoAction(UndoableEvent event) {
                    HorizontalAlignmentConstant oldAlignment = (HorizontalAlignmentConstant) event.getData("oldAlignment");
                    HasHorizontalAlignment hwidget = (HasHorizontalAlignment) event.getData("hwidget");
                    hwidget.setHorizontalAlignment(oldAlignment);
                }
                
                public void doAction(UndoableEvent event) {
                    HorizontalAlignmentConstant newAlignment = (HorizontalAlignmentConstant) event.getData("newAlignment");
                    HasHorizontalAlignment hwidget = (HasHorizontalAlignment) event.getData("hwidget");
                    hwidget.setHorizontalAlignment(newAlignment);
                }
            }));
        }
    }
    
    @Override
    public void remove(FBFormItem item) {
        super.remove(item);
        Widget widget = getWidget();
        if (widget instanceof HasHorizontalAlignment) {
            HasHorizontalAlignment hw = (HasHorizontalAlignment) widget;
            hw.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_DEFAULT);
        }
    }
    
    @Override
    public PopupPanel createPanel() {
        final PopupPanel panel = new PopupPanel();
        panel.setSize("300px", "200px");
        VerticalPanel vPanel = new VerticalPanel();
        HorizontalPanel hPanel = new HorizontalPanel();
        hPanel.add(new Label("Alignment:"));
        alignmentBox.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                HorizontalAlignmentFormEffect.this.createStyles();
                panel.hide();
            };
        });
        hPanel.add(alignmentBox);
        Button fontSizeButton = new Button("Apply");
        fontSizeButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                HorizontalAlignmentFormEffect.this.createStyles();
                panel.hide();
            }
        });
        vPanel.add(alignmentBox);
        vPanel.add(fontSizeButton);
        panel.add(vPanel);
        return panel;
    }
}
