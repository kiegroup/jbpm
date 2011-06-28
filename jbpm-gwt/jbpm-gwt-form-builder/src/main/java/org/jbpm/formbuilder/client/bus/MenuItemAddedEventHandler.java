package org.jbpm.formbuilder.client.bus;

import com.google.gwt.event.shared.EventHandler;

public interface MenuItemAddedEventHandler extends EventHandler {

    void onEvent(MenuItemAddedEvent event);
}
