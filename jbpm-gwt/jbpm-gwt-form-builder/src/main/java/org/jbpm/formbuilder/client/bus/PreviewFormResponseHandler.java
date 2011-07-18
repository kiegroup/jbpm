package org.jbpm.formbuilder.client.bus;

import com.google.gwt.event.shared.EventHandler;

public interface PreviewFormResponseHandler extends EventHandler {

    void onServerResponse(PreviewFormResponseEvent event);
}
