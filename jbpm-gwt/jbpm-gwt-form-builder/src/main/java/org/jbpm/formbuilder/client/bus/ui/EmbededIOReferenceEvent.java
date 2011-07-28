package org.jbpm.formbuilder.client.bus.ui;

import org.jbpm.formbuilder.shared.task.TaskRef;

import com.google.gwt.event.shared.GwtEvent;

public class EmbededIOReferenceEvent extends GwtEvent<EmbededIOReferenceHandler> {

    public static final Type<EmbededIOReferenceHandler> TYPE = new Type<EmbededIOReferenceHandler>();
    
    private final TaskRef ioRef;
    private final String profileName;
    
    public EmbededIOReferenceEvent(TaskRef ioRef, String profileName) {
        super();
        this.ioRef = ioRef;
        this.profileName = profileName;
    }

    public TaskRef getIoRef() {
        return ioRef;
    }
    
    public String getProfileName() {
        return profileName;
    }
    
    @Override
    public Type<EmbededIOReferenceHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(EmbededIOReferenceHandler handler) {
        handler.onEvent(this);
    }

}
