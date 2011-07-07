package org.jbpm.formbuilder.client.bus.ui;

import com.google.gwt.event.shared.EventHandler;

public interface FormItemRemovedHandler extends EventHandler {

    void onEvent(FormItemRemovedEvent event);
}
