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
package org.jbpm.formbuilder.client.form.editors;

import org.jbpm.formbuilder.client.form.FBInplaceEditor;
import org.jbpm.formbuilder.client.form.items.HeaderFormItem;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * Inplace editor for {@link HeaderFormItem}
 */
public class HeaderInplaceEditor extends FBInplaceEditor {

    private final TextBox textBox = new TextBox();
    private final FocusWrapper wrapper = new FocusWrapper();
    
    public HeaderInplaceEditor(final HeaderFormItem item) {
        final HorizontalPanel editPanel = new HorizontalPanel();
        editPanel.setBorderWidth(1);
        textBox.setValue(item.getHeader().getText());
        textBox.addBlurHandler(new BlurHandler() {
            public void onBlur(BlurEvent event) {
                wrapper.setValue(false);
                item.reset();
            }
        });
        textBox.addFocusHandler(new FocusHandler() {
            public void onFocus(FocusEvent event) {
                wrapper.setValue(true);
            }
        });
        textBox.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                item.setContent("<h1>" + textBox.getValue() + "</h1>");
                item.reset();
            }
        });
        editPanel.add(textBox);
        textBox.setWidth(item.getHeader().getOffsetWidth() + "px");
        textBox.setFocus(true);
        add(editPanel);
    }
    
    @Override
    public void focus() {
        textBox.setFocus(true);
    }

    @Override
    public boolean isFocused() {
        return wrapper.getValue();
    }
}
