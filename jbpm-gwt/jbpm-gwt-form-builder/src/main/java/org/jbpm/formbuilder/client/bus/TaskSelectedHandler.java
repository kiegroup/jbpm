package org.jbpm.formbuilder.client.bus;

import com.google.gwt.event.shared.EventHandler;

public interface TaskSelectedHandler extends EventHandler {

    void onSelectedTask(TaskSelectedEvent event);
}
