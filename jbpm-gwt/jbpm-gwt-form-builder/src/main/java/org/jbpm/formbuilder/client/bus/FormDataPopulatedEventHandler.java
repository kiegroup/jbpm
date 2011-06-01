package org.jbpm.formbuilder.client.bus;

import com.google.gwt.event.shared.EventHandler;

public interface FormDataPopulatedEventHandler extends EventHandler {

    void onEvent(FormDataPopulatedEvent event);
}
