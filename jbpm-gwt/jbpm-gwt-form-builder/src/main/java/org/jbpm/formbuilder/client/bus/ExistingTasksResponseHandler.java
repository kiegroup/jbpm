package org.jbpm.formbuilder.client.bus;

import com.google.gwt.event.shared.EventHandler;

public interface ExistingTasksResponseHandler extends EventHandler {

    void onEvent(ExistingTasksResponseEvent event);
}
