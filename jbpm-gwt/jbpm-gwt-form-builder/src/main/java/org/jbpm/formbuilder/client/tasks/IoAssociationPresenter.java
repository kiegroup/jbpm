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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.FormBuilderService;
import org.jbpm.formbuilder.client.bus.ExistingTasksResponseEvent;
import org.jbpm.formbuilder.client.bus.ExistingTasksResponseHandler;
import org.jbpm.formbuilder.client.bus.ui.NotificationEvent;
import org.jbpm.formbuilder.client.bus.ui.TaskNameFilterEvent;
import org.jbpm.formbuilder.client.bus.ui.TaskNameFilterHandler;
import org.jbpm.formbuilder.client.bus.ui.TaskSelectedEvent;
import org.jbpm.formbuilder.client.bus.ui.NotificationEvent.Level;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.shared.task.TaskRef;

import com.google.gwt.event.shared.EventBus;

/**
 * Tasks presenter. Handles server querying of existing tasks 
 * and view population
 */
public class IoAssociationPresenter {
    
    private final IoAssociationView view;
    private final FormBuilderService model;
    
    private final EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
    
    public IoAssociationPresenter(FormBuilderService service, IoAssociationView tasksView) {
        this.view = tasksView;
        this.model = service;
        bus.addHandler(TaskNameFilterEvent.TYPE, new TaskNameFilterHandler() {
            public void onEvent(TaskNameFilterEvent event) {
                String filter = event.getTaskNameFilter();
                try {
                    model.getExistingTasks(filter);
                } catch (Exception e) {
                    bus.fireEvent(new NotificationEvent(Level.WARN, "Couldn't populate autocomplete", e));
                }
            }
        });
        bus.addHandler(ExistingTasksResponseEvent.TYPE, new ExistingTasksResponseHandler() {
            public void onEvent(ExistingTasksResponseEvent event) {
                List<TaskRef> tasks = event.getTasks();
                String filter = event.getFilter();
                Map<String, String> taskItems = new HashMap<String, String>();
                for (TaskRef task : tasks) {
                    taskItems.put(task.getTaskId(), task.getTaskName() + " (from " + task.getProcessId() + ")");
                }
                view.setTaskCombo(tasks);
                if (taskItems.get(filter) != null) {
                    TaskRef selectedTask = null;
                    for (TaskRef task : tasks) {
                        if (task.getTaskId().equals(filter)) {
                            selectedTask = task;
                            break;
                        }
                    }
                    bus.fireEvent(new TaskSelectedEvent(selectedTask));
                    view.setTaskInputs(selectedTask.getInputs());
                    view.setTaskOutputs(selectedTask.getOutputs());
                    view.setData(selectedTask.getMetaData());
                }
            }
        });
    }

}
