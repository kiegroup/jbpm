package org.jbpm.formbuilder.client.bus.ui;

import com.google.gwt.event.shared.EventHandler;

public interface FormItemAddedHandler extends EventHandler {

    void onEvent(FormItemAddedEvent event);
}
