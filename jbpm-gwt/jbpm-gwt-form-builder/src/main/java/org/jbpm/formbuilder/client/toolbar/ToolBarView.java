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
package org.jbpm.formbuilder.client.toolbar;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;

/**
 * Toolbar view. shows buttons as told from outside
 */
public class ToolBarView extends AbsolutePanel {

    private HorizontalPanel hPanel = new HorizontalPanel();
    
    public ToolBarView() {
        setSize("100%", "100%");
        hPanel.setSize("100%", "100%");
        hPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        add(hPanel);
    }

    public ToolRegistration addButton(ImageResource imgRes, String name, ClickHandler handler) {
        final Button button = new Button();
        Image image = new Image(imgRes);
        image.setAltText(name);
        image.setTitle(name);
        button.setHTML(new SafeHtmlBuilder().appendHtmlConstant(image.toString()).toSafeHtml());
        button.addClickHandler(handler);
        hPanel.add(button);
        return new ToolRegistration() {
            public void remove() {
                hPanel.remove(button);
            }
        };
    }

    public ToolRegistration addMessage(String name, String value) {
        if (value != null && !"".equals(value)) {
            final HTML label = new HTML("<strong>" + name + ":</strong> " + value);
            hPanel.add(label);
            return new ToolRegistration() {
                public void remove() {
                    hPanel.remove(label);
                }
            };
        } else {
            return new ToolRegistration() {
                public void remove() { }
            };
        }
    }
    
    public ToolbarDialog createToolbarDialog(String warningText) {
        return new ToolbarDialog(warningText);
    }
}
