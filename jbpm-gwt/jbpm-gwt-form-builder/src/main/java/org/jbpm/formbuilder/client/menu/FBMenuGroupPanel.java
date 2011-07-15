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
package org.jbpm.formbuilder.client.menu;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Panel for all accordion elements. Handles which are
 * collapsed and which are expanded
 */
public class FBMenuGroupPanel extends SimplePanel {

    private final FBMenuPanel menuPanel;
    private final Button expandButton;
    private final Button collapseButton;
    
    public FBMenuGroupPanel(String group, FBMenuPanel menuPanel) {
        this.menuPanel = menuPanel;
        this.expandButton = new Button(group + " (+)");
        this.collapseButton = new Button(group + " (-)");
        setWidth("100%");
        this.expandButton.setWidth("100%");
        this.collapseButton.setWidth("100%");
        this.expandButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                expand();
            }
        });
        this.collapseButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                collapse();
            }
        });
        add(this.expandButton);
    }

    public void collapse() {
        setWidget(expandButton);
    }
    
    public void expand() {
        VerticalPanel vPanel = new VerticalPanel();
        vPanel.setWidth("100%");
        vPanel.add(collapseButton);
        vPanel.add(menuPanel);
        setWidget(vPanel);
    }
    
    public boolean hasWidgets() {
        return menuPanel.getWidgetCount() > 0;
    }
    
    public void add(FBMenuItem menuItem) {
        menuPanel.add(menuItem);
    }
    
    public void remove(FBMenuItem menuItem) {
        menuPanel.fullRemove(menuItem);
    }
    
}
