package org.jbpm.formbuilder.client.bus.ui;

import com.google.gwt.event.shared.GwtEvent;

public class TaskNameFilterEvent extends GwtEvent<TaskNameFilterEventHandler> {

    public static final Type<TaskNameFilterEventHandler> TYPE = new Type<TaskNameFilterEventHandler>();
    
    private final String taskNameFilter;
    
    public TaskNameFilterEvent(String taskNameFilter) {
        super();
        this.taskNameFilter = taskNameFilter;
    }

    public String getTaskNameFilter() {
        return taskNameFilter;
    }
    
    @Override
    public Type<TaskNameFilterEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(TaskNameFilterEventHandler handler) {
        handler.onEvent(this);
    }

}
