package org.jbpm.formbuilder.client.bus;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.event.shared.GwtEvent;

public class UndoableEvent extends GwtEvent<UndoableEventHandler> {

    public static final Type<UndoableEventHandler> TYPE = new Type<UndoableEventHandler>();
    
    private final UndoableEventHandler rollbackHandler;
    private final Map<String, Object> dataSnapshot;

    public UndoableEvent(Map<String, Object> dataSnapshot, UndoableEventHandler rollbackHandler) {
        this.rollbackHandler = rollbackHandler;
        if (dataSnapshot == null) {
            dataSnapshot = new HashMap<String, Object>();
        }
        this.dataSnapshot = dataSnapshot;
        this.rollbackHandler.doAction(this);
    }
    
    public UndoableEventHandler getRollbackHandler() {
        return rollbackHandler;
    }
    
    public Map<String, Object> getDataSnapshot() {
        return dataSnapshot;
    }
    
    public Object getData(String key) {
        return dataSnapshot.get(key);
    }
    
    @Override
    public Type<UndoableEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(UndoableEventHandler handler) {
        handler.onEvent(this);
    }

}
