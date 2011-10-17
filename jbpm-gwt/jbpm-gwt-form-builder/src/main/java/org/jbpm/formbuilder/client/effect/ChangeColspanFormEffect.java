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
package org.jbpm.formbuilder.client.effect;

import org.jbpm.formbuilder.client.FormBuilderGlobals;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.form.items.MIGLayoutFormItem;
import org.jbpm.formbuilder.client.messages.I18NConstants;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.gwtent.reflection.client.Reflectable;

@Reflectable
public class ChangeColspanFormEffect extends FBFormEffect {

    private final I18NConstants i18n = FormBuilderGlobals.getInstance().getI18n();
    private final IntegerBox colspan = new IntegerBox();
    private final IntegerBox rowspan = new IntegerBox();
    
    
    public ChangeColspanFormEffect() {
        super(FormBuilderGlobals.getInstance().getI18n().ChangeColspanEffectLabel(), true);
    }

    @Override
    protected void createStyles() {
        FBFormItem item = getItem();
        Widget parent = item.getParent();
        while (!(parent instanceof MIGLayoutFormItem)) {
            parent = parent.getParent();
        }
        MIGLayoutFormItem container = (MIGLayoutFormItem) parent;
        container.setSpan(item, colspan.getValue(), rowspan.getValue());
    }
    
    @Override
    public PopupPanel createPanel() {
        final PopupPanel panel = new PopupPanel();
        Grid data = new Grid(3, 2);
        Button cancelButton = new Button(i18n.CancelButton(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                panel.hide();
            }
        });
        Button confirmButton = new Button(i18n.ConfirmButton(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                createStyles();
                panel.hide();
            }
        });
        data.setWidget(0, 0, new Label(i18n.ColspanLabel()));
        data.setWidget(0, 1, colspan);
        data.setWidget(1, 0, new Label(i18n.RowspanLabel()));
        data.setWidget(1, 1, rowspan);
        data.setWidget(2, 0, cancelButton);
        data.setWidget(2, 1, confirmButton);
        panel.add(data);
        return panel;
    }

}
