package org.jbpm.formbuilder.client.bus;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.Window;

public class FormDataPopulatedEvent extends GwtEvent<FormDataPopulatedEventHandler> {

    public static final Type<FormDataPopulatedEventHandler> TYPE = new Type<FormDataPopulatedEventHandler>();

    private String action;
    private String method;
    private String taskId;
    private String enctype;
    private String name;
    
    public FormDataPopulatedEvent() {
        Window.alert("Somebody to listen this event and ask for form data is required yet");
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

    public void setAction(String action) {
        this.action = action;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getEnctype() {
        return enctype;
    }

    public void setEnctype(String enctype) {
        this.enctype = enctype;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
