package org.jbpm.formbuilder.client.bus;

import com.google.gwt.event.shared.EventHandler;

public interface ExistingValidationsResponseHandler extends EventHandler {

    void onEvent(ExistingValidationsResponseEvent event);
}
