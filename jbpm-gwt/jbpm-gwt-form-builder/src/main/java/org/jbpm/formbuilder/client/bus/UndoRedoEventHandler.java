package org.jbpm.formbuilder.client.bus;

import com.google.gwt.event.shared.EventHandler;

public interface UndoRedoEventHandler extends EventHandler {

    void onEvent(UndoRedoEvent event);
}
