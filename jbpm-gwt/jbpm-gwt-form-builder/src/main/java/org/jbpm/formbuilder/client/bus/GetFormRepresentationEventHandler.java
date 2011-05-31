package org.jbpm.formbuilder.client.bus;

import com.google.gwt.event.shared.EventHandler;

public interface GetFormRepresentationEventHandler extends EventHandler {

    void onEvent(GetFormRepresentationEvent event);
}
