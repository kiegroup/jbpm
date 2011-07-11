package org.jbpm.formbuilder.client.bus;

import java.util.List;

import org.jbpm.formbuilder.shared.task.TaskRef;

import com.google.gwt.event.shared.GwtEvent;

public class ExistingTasksResponseEvent extends GwtEvent<ExistingTasksResponseHandler> {

    public static final Type<ExistingTasksResponseHandler> TYPE = new Type<ExistingTasksResponseHandler>();
    
    private final List<TaskRef> tasks;
    private final String filter;
    
    public ExistingTasksResponseEvent(List<TaskRef> tasks, String filter) {
        super();
        this.tasks = tasks;
        this.filter = filter;
    }
    
    public List<TaskRef> getTasks() {
        return tasks;
    }
    
    public String getFilter() {
        return filter;
    }

    @Override
    public Type<ExistingTasksResponseHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(ExistingTasksResponseHandler handler) {
        handler.onEvent(this);
    }

}
