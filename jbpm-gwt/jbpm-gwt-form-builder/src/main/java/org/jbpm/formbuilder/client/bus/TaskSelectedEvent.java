package org.jbpm.formbuilder.client.bus;

import org.jbpm.formbuilder.shared.task.TaskRef;

import com.google.gwt.event.shared.GwtEvent;

public class TaskSelectedEvent extends GwtEvent<TaskSelectedHandler> {

    public static final Type<TaskSelectedHandler> TYPE = new Type<TaskSelectedHandler>();
    
    private final TaskRef selectedTask;
    
    public TaskSelectedEvent(TaskRef selectedTask) {
        this.selectedTask = selectedTask;
    }
    
    public TaskRef getSelectedTask() {
        return selectedTask;
    }

    @Override
    public Type<TaskSelectedHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(TaskSelectedHandler handler) {
        handler.onSelectedTask(this);
    }

    
}
