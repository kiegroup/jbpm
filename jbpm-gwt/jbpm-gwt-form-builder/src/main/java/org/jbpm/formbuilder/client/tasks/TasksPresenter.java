package org.jbpm.formbuilder.client.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.FormBuilderService;
import org.jbpm.formbuilder.client.bus.NotificationEvent;
import org.jbpm.formbuilder.client.bus.NotificationEvent.Level;
import org.jbpm.formbuilder.client.bus.TaskSelectedEvent;
import org.jbpm.formbuilder.client.bus.ui.TaskNameFilterEvent;
import org.jbpm.formbuilder.client.bus.ui.TaskNameFilterEventHandler;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.shared.task.TaskRef;

import com.google.gwt.event.shared.EventBus;

public class TasksPresenter {
    
    private final TasksView view;
    private final FormBuilderService model;
    
    private final EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
    
    public TasksPresenter(FormBuilderService service, TasksView tasksView) {
        this.view = tasksView;
        this.model = service;
        bus.addHandler(TaskNameFilterEvent.TYPE, new TaskNameFilterEventHandler() {
            public void onEvent(TaskNameFilterEvent event) {
                String filter = event.getTaskNameFilter();
                List<TaskRef> tasks; 
                try {
                    tasks = model.getExistingTasks(filter);
                } catch (Exception e) {
                    bus.fireEvent(new NotificationEvent(Level.WARN, "Couldn't populate autocomplete", e));
                    tasks = new ArrayList<TaskRef>();
                }
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
