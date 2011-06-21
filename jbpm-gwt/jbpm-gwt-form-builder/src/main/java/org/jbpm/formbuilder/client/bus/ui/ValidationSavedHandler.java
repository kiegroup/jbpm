package org.jbpm.formbuilder.client.bus.ui;

import com.google.gwt.event.shared.EventHandler;

public interface ValidationSavedHandler extends EventHandler {

    void onEvent(ValidationSavedEvent event);
}
