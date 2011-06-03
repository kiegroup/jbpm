package org.jbpm.formbuilder.client.options;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.formbuilder.client.bus.UndoableEvent;
import org.jbpm.formbuilder.client.bus.UndoableEventHandler;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;

import com.google.gwt.event.shared.EventBus;

public class UndoRedoManager {

    /* static methods */
    
    private static final UndoRedoManager INSTANCE = new UndoRedoManager();
    
    public static UndoRedoManager getInstance() {
        return INSTANCE;
    }
    
    /* instance methods */
    
    private List<UndoableEvent> undoRedoWindow = new ArrayList<UndoableEvent>();
    private int index = 0;
    
    private UndoRedoManager() {
        EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
        bus.addHandler(UndoableEvent.TYPE, new UndoableEventHandler() {
            public void onEvent(UndoableEvent event) {
                syncAdd(event);
            }
            public void undoAction(UndoableEvent event) { }
            public void doAction(UndoableEvent event) { }
        });
    }
    
    protected synchronized void syncAdd(UndoableEvent event) {
        undoRedoWindow.add(event);
    }
    
    public synchronized void undo() {
        UndoableEvent event = undoRedoWindow.get(index);
        event.getRollbackHandler().undoAction(event);
        index--;
    }
    
    public boolean canUndo() {
        return undoRedoWindow.size() > 0;
    }
    
    public synchronized void redo() {
        UndoableEvent event = undoRedoWindow.get(index);
        event.getRollbackHandler().doAction(event);
        index++;
        while (undoRedoWindow.size() > index) {
            undoRedoWindow.remove(index);
        }
    }
    
    public boolean canRedo() {
        return undoRedoWindow.size() > 0 && index < undoRedoWindow.size();
    }
}
