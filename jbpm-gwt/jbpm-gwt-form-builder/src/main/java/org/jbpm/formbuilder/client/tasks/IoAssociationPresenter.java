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
package org.jbpm.formbuilder.client.tasks;

import org.jbpm.formbuilder.client.FormBuilderService;
import org.jbpm.formbuilder.client.bus.ExistingTasksResponseEvent;
import org.jbpm.formbuilder.client.bus.ExistingTasksResponseHandler;
import org.jbpm.formbuilder.client.bus.ui.NotificationEvent;
import org.jbpm.formbuilder.client.bus.ui.NotificationEvent.Level;
import org.jbpm.formbuilder.client.bus.ui.EmbededIOReferenceEvent;
import org.jbpm.formbuilder.client.bus.ui.EmbededIOReferenceHandler;
import org.jbpm.formbuilder.client.bus.ui.TaskNameFilterEvent;
import org.jbpm.formbuilder.client.bus.ui.TaskNameFilterHandler;
import org.jbpm.formbuilder.client.bus.ui.TaskSelectedEvent;
import org.jbpm.formbuilder.client.bus.ui.TaskSelectedHandler;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;

import com.google.gwt.event.shared.EventBus;

/**
 * Tasks presenter. Handles server querying of existing tasks 
 * and view population
 */
public class IoAssociationPresenter {
    
    private final IoAssociationView view;
    private final FormBuilderService model = FormBuilderGlobals.getInstance().getService();
    
    private final EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
    
    public IoAssociationPresenter(IoAssociationView tasksView) {
        this.view = tasksView;
        bus.addHandler(TaskNameFilterEvent.TYPE, new TaskNameFilterHandler() {
            public void onEvent(TaskNameFilterEvent event) {
                String filter = event.getTaskNameFilter();
                try {
                    model.getExistingIoAssociations(filter);
                } catch (Exception e) {
                    bus.fireEvent(new NotificationEvent(Level.WARN, "Couldn't populate autocomplete", e));
                }
            }
        });
        bus.addHandler(ExistingTasksResponseEvent.TYPE, new ExistingTasksResponseHandler() {
            public void onEvent(ExistingTasksResponseEvent event) {
                view.setTasks(event.getTasks());
            }
        });
        bus.addHandler(TaskSelectedEvent.TYPE, new TaskSelectedHandler() {
            public void onSelectedTask(TaskSelectedEvent event) {
                view.setSelectedTask(event.getSelectedTask());
            }
        });
        bus.addHandler(EmbededIOReferenceEvent.TYPE, new EmbededIOReferenceHandler() {
            public void onEvent(EmbededIOReferenceEvent event) {
                view.disableSearch();
                bus.fireEvent(new TaskSelectedEvent(event.getIoRef()));
                // TODO UI Components should get prepared to allow IO bindings with the task selected event.
            }
        });
    }

}
