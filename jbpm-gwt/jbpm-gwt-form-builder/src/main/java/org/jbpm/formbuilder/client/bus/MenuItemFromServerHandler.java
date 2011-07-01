package org.jbpm.formbuilder.client.bus;

import com.google.gwt.event.shared.EventHandler;

public interface MenuItemFromServerHandler extends EventHandler {

    void onEvent(MenuItemFromServerEvent event);
}
