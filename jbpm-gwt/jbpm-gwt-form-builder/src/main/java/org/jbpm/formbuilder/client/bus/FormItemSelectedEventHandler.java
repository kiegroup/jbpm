package org.jbpm.formbuilder.client.bus;

import com.google.gwt.event.shared.EventHandler;

public interface FormItemSelectedEventHandler extends EventHandler {
    
    public void onEvent(FormItemSelectedEvent event);
}
