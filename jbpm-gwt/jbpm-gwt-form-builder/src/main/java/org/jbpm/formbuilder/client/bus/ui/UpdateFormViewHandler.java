package org.jbpm.formbuilder.client.bus.ui;

import com.google.gwt.event.shared.EventHandler;

public interface UpdateFormViewHandler extends EventHandler {
    
    void onEvent(UpdateFormViewEvent event);
}
