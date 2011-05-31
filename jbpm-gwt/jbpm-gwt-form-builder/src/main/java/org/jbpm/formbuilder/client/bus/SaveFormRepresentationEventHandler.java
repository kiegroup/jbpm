package org.jbpm.formbuilder.client.bus;

import com.google.gwt.event.shared.EventHandler;

public interface SaveFormRepresentationEventHandler extends EventHandler {

    void onEvent(SaveFormRepresentationEvent event);
}
