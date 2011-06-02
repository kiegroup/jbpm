package org.jbpm.formbuilder.client.bus;

import com.google.gwt.event.shared.GwtEvent; 

public class FormDataPopulatedEvent extends GwtEvent<FormDataPopulatedEventHandler> {

    public static final Type<FormDataPopulatedEventHandler> TYPE = new Type<FormDataPopulatedEventHandler>();

    private final String action;
    private final String method;
    private final String taskId;
    private final String enctype;
    private final String name;
    
    public FormDataPopulatedEvent(String action, String method, String taskId, String enctype, String name) {
        this.name = name;
        this.action = action;
        this.method = method;
        this.taskId = taskId;
        this.enctype = enctype;
    }
    
    @Override
    public Type<FormDataPopulatedEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(FormDataPopulatedEventHandler handler) {
        handler.onEvent(this);
    }

    public String getAction() {
        return action;
    }

    public String getMethod() {
        return method;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getEnctype() {
        return enctype;
    }

    public String getName() {
        return name;
    }
}
