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

import org.jbpm.formbuilder.client.bus.FormItemSelectionEvent;
import org.jbpm.formbuilder.client.bus.FormItemSelectionHandler;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Populates edition panel when a form item is selected
 */
public class EditionPresenter {

    private final EditionView editView;
    private final EventBus bus;
    
    public EditionPresenter(EditionView view) {
        super();
        this.editView = view;
        this.bus = FormBuilderGlobals.getInstance().getEventBus();
        
        this.bus.addHandler(FormItemSelectionEvent.TYPE, new FormItemSelectionHandler() {
            @Override
            public void onEvent(FormItemSelectionEvent event) {
                if (event.isSelected()) {
                    Widget parent = editView.getParent();
                    while (!(parent instanceof TabLayoutPanel)) {
                        parent = parent.getParent();
                    }
                    TabLayoutPanel tab = (TabLayoutPanel) parent;
                    tab.selectTab(editView);
                    editView.populate(event.getFormItemSelected());
                }
            }
        });
    }
}
