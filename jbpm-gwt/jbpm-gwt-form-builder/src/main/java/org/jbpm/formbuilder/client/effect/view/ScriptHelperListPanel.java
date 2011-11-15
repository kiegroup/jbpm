/*
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

import org.jbpm.formbuilder.client.FormBuilderGlobals;
import org.jbpm.formbuilder.client.messages.I18NConstants;
import org.jbpm.formbuilder.shared.api.FBScriptHelper;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class ScriptHelperListPanel extends VerticalPanel {

    private final I18NConstants i18n = FormBuilderGlobals.getInstance().getI18n();
    
    public void addScriptHelper(FBScriptHelper helper) {
        Widget editor = helper.draw();
        if (editor == null) {
            editor = new Label("Problem: null editor"); //TODO i18n
        }
        final HorizontalPanel panel = new HorizontalPanel();
        String number = String.valueOf(getWidgetCount() + 1);
        panel.add(new Label(number));
        panel.add(editor);
        VerticalPanel buttons = new VerticalPanel();
        panel.add(buttons);
        buttons.add(createRemoveButton(panel));
        buttons.add(createMoveUpButton(panel));
        buttons.add(createMoveDownButton(panel));
        add(panel);
    }

    private Button createMoveDownButton(final HorizontalPanel panel) {
        return new Button("Move down", new ClickHandler() { //TODO i18n
            @Override
            public void onClick(ClickEvent event) {
                int index = getWidgetIndex(panel);
                if (index + 1 < getWidgetCount()) {
                    remove(panel);
                    insert(panel, index + 1);
                    renumber();
                }
            }
        });
    }

    private Button createMoveUpButton(final HorizontalPanel panel) {
        return new Button("Move up", new ClickHandler() { //TODO i18n
            @Override
            public void onClick(ClickEvent event) {
                int index = getWidgetIndex(panel);
                if (index -1 >= 0) {
                    remove(panel);
                    insert(panel, index - 1);
                    renumber();
                }
            }
        });
    }

    private Button createRemoveButton(final HorizontalPanel panel) {
        return new Button(i18n.RemoveButton(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                remove(panel);
                renumber();
            }
        });
    }
    
    private void renumber() {
        for (Widget widget : this) {
            HorizontalPanel panel = (HorizontalPanel) widget;
            int index = getWidgetIndex(panel);
            String number = String.valueOf(index + 1);
            panel.remove(0);
            panel.insert(new Label(number), 0);
        }
         
    }
}
