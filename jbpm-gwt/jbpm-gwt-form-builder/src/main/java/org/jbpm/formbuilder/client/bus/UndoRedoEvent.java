package org.jbpm.formbuilder.client.bus;

import com.google.gwt.event.shared.GwtEvent;

public class UndoRedoEvent extends GwtEvent<UndoRedoEventHandler> {

    public static final Type<UndoRedoEventHandler> TYPE = new Type<UndoRedoEventHandler>();
    
    @Override
    public Type<UndoRedoEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(UndoRedoEventHandler handler) {
        handler.onEvent(this);
    }

}
