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
import org.jbpm.formbuilder.client.bus.GetFormRepresentationResponseEvent;
import org.jbpm.formbuilder.client.bus.GetFormRepresentationResponseHandler;
import org.jbpm.formbuilder.client.bus.LoadServerFormEvent;
import org.jbpm.formbuilder.client.bus.ui.EmbededIOReferenceEvent;
import org.jbpm.formbuilder.client.bus.ui.EmbededIOReferenceHandler;
import org.jbpm.formbuilder.client.bus.ui.TaskSelectedEvent;
import org.jbpm.formbuilder.client.bus.ui.TaskSelectedHandler;
import org.jbpm.formbuilder.client.command.LoadFormCommand;
import org.jbpm.formbuilder.client.command.SaveFormCommand;
import org.jbpm.formbuilder.client.messages.I18NConstants;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;
import org.jbpm.formbuilder.common.panels.ConfirmDialog;

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
    private final I18NConstants i18n = FormBuilderGlobals.getInstance().getI18n();

    private final ToolRegistration saveRef;
    
    public ToolBarPresenter(ToolBarView toolBarView) {
        this.view = toolBarView;

        this.saveRef = this.view.addButton(FormBuilderResources.INSTANCE.saveButton(), i18n.SaveChangesButton(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                bus.fireEvent(new GetFormRepresentationEvent(SAVE_TYPE));
            }
        });
        this.view.addButton(FormBuilderResources.INSTANCE.refreshButton(), i18n.RefreshFromServerButton(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                bus.fireEvent(new GetFormRepresentationEvent(LOAD_TYPE));
            }
        });
        
        bus.addHandler(GetFormRepresentationResponseEvent.TYPE, new GetFormRepresentationResponseHandler() {
            @Override
            public void onEvent(final GetFormRepresentationResponseEvent event) {
                if (LOAD_TYPE.equals(event.getSaveType())) {
                    final ConfirmDialog dialog = new ConfirmDialog(i18n.RefreshButtonWarning());
                    dialog.addOkButtonHandler(new ClickHandler() {
                        @Override
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
            }
        });
        bus.addHandler(EmbededIOReferenceEvent.TYPE, new EmbededIOReferenceHandler() {
            @Override
            public void onEvent(EmbededIOReferenceEvent event) {
                if (event.getProfileName() != null) {
                    saveRef.remove();
                }
            }
        });
        bus.addHandler(TaskSelectedEvent.TYPE, new TaskSelectedHandler() {
            @Override
            public void onSelectedTask(TaskSelectedEvent event) {
                if (event.getSelectedTask() != null) {
                    view.addMessage(i18n.PackageLabel(), event.getSelectedTask().getPackageName());
                    view.addMessage(i18n.ProcessLabel(), event.getSelectedTask().getProcessId());
                    view.addMessage(i18n.TaskNameLabel(), event.getSelectedTask().getTaskName());
                }
            }
        });
    }
}
