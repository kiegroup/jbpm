package org.jbpm.formbuilder.client.bus.ui;

import com.google.gwt.event.shared.EventHandler;

public interface FormSavedHandler extends EventHandler {

    void onEvent(FormSavedEvent event);
}
