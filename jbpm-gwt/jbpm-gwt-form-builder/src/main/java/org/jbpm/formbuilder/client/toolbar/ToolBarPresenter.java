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

import org.jbpm.formbuilder.client.bus.GetFormRepresentationEvent;
import org.jbpm.formbuilder.client.bus.LoadServerFormEvent;
import org.jbpm.formbuilder.client.bus.GetFormRepresentationResponseEvent;
import org.jbpm.formbuilder.client.bus.GetFormRepresentationResponseHandler;
import org.jbpm.formbuilder.client.command.LoadFormCommand;
import org.jbpm.formbuilder.client.command.SaveFormCommand;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;

/**
 * Toolbar presenter. Registers buttons to send {@link EventBus} notifications
 */
public class ToolBarPresenter {

    private static final String SAVE_TYPE = SaveFormCommand.class.getName();
    private static final String LOAD_TYPE = LoadFormCommand.class.getName();
    
    private final ToolBarView view;
    private final EventBus bus = FormBuilderGlobals.getInstance().getEventBus();

    public ToolBarPresenter(ToolBarView toolBarView) {
        this.view = toolBarView;

        this.view.addButton(FormBuilderResources.INSTANCE.saveButton(), "Save", new ClickHandler() {
            public void onClick(ClickEvent event) {
                bus.fireEvent(new GetFormRepresentationEvent(SAVE_TYPE));
            }
        });
        
        this.view.addButton(FormBuilderResources.INSTANCE.refreshButton(), "Refresh from Server", new ClickHandler() {
            public void onClick(ClickEvent event) {
                bus.fireEvent(new GetFormRepresentationEvent(LOAD_TYPE));
            }
        });
        bus.addHandler(GetFormRepresentationResponseEvent.TYPE, new GetFormRepresentationResponseHandler() {
            public void onEvent(final GetFormRepresentationResponseEvent event) {
                final ToolbarDialog dialog = view.createToolbarDialog(
                        "Attention! if you continue, all data you haven't saved will be lost and " +
                        "replaced with the server information. Are you sure you want to continue?");
                dialog.addOkButtonHandler(new ClickHandler() {
                    public void onClick(ClickEvent clickEvent) {
                        if (LOAD_TYPE.equals(event.getSaveType())) {
                            if (event.getRepresentation().isSaved()) {
                                bus.fireEvent(new LoadServerFormEvent(event.getRepresentation().getName()));
                            }
                        }
                    }
                });
                dialog.show();
            }
        });
    }
}
