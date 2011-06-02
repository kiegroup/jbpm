package org.jbpm.formbuilder.client.bus;

import com.google.gwt.event.shared.EventHandler;

public interface MenuOptionAddedEventHandler extends EventHandler {

    void onEvent(MenuOptionAddedEvent event);
}
