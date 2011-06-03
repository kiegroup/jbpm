package org.jbpm.formbuilder.client.bus;

import com.google.gwt.event.shared.EventHandler;

public interface UndoableEventHandler extends EventHandler {

    /*
     * When event happens (to register it on the queue)
     */
    void onEvent(UndoableEvent event);

    /*
     * Action done to Undo this action
     */
    void undoAction(UndoableEvent event);
    
    /*
     * Action done to Do (and Redo) this action
     */
    void doAction(UndoableEvent event);
}
