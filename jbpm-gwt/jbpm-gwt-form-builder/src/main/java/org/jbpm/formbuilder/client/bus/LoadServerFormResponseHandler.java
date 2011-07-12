package org.jbpm.formbuilder.client.bus;

import com.google.gwt.event.shared.EventHandler;

public interface LoadServerFormResponseHandler extends EventHandler {

    void onGetForm(LoadServerFormResponseEvent event);
    void onListForms(LoadServerFormResponseEvent event);
}
