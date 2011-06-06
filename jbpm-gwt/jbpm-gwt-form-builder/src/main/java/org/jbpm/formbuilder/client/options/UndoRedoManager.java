package org.jbpm.formbuilder.client.options;

import java.util.LinkedList;
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
    
    private List<UndoableEvent> undoRedoWindow = new LinkedList<UndoableEvent>();
    private int index = -1;
    
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
        this.index++;
        while (this.index < this.undoRedoWindow.size()) { //delete all posterior actions when a new action happens
            this.undoRedoWindow.remove(this.index);
        }
        this.undoRedoWindow.add(event);
    }
    
    public synchronized void undo() {
        if (canUndo()) {
            UndoableEvent event = undoRedoWindow.get(index);
            index--;
            event.getRollbackHandler().undoAction(event);
        }
    }
    
    public synchronized void redo() {
        if (canRedo()) {
            UndoableEvent event = undoRedoWindow.get(index);
            index++;
            event.getRollbackHandler().doAction(event);
        }
    }
    
    public boolean canUndo() {
        return undoRedoWindow.size() > 0 && index >= 0;
    }
    
    public boolean canRedo() {
        //System.out.println("undoRedoWindow.size = " + undoRedoWindow.size() + " && index = " + index);
        return undoRedoWindow.size() > 0 && index < (undoRedoWindow.size() - 1);
    }
}
