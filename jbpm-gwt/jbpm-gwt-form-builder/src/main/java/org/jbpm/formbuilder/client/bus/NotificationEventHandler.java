package org.jbpm.formbuilder.client.bus;

import com.google.gwt.event.shared.EventHandler;

public interface NotificationEventHandler extends EventHandler {

    void onEvent(NotificationEvent event);
}
