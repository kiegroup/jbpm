package org.jbpm.formbuilder.client.bus.ui;

import org.jbpm.formbuilder.shared.task.TaskRef;

import com.google.gwt.event.shared.GwtEvent;

public class EmbededIOReferenceEvent extends GwtEvent<EmbededIOReferenceHandler> {

    public static final Type<EmbededIOReferenceHandler> TYPE = new Type<EmbededIOReferenceHandler>();
    
    private final TaskRef ioRef;
    
    public EmbededIOReferenceEvent(TaskRef ioRef) {
        super();
        this.ioRef = ioRef;
    }

    public TaskRef getIoRef() {
        return ioRef;
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
