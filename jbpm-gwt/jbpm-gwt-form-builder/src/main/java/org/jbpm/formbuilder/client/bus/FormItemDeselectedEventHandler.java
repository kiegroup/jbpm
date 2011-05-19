package org.jbpm.formbuilder.client.bus;

import com.google.gwt.event.shared.EventHandler;

public interface FormItemDeselectedEventHandler extends EventHandler {
    
    public void onEvent(FormItemDeselectedEvent event);
}
