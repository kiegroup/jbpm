package org.jbpm.formbuilder.client;

import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.TextCallback;
import org.jbpm.formbuilder.client.bus.ui.NotificationEvent;
import org.jbpm.formbuilder.client.bus.ui.NotificationEvent.Level;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;

import com.google.gwt.event.shared.EventBus;

public abstract class SimpleTextCallback implements TextCallback {

    private final EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
    private final String errorMessage;
    
    public SimpleTextCallback(String onErrorMessage) {
        this.errorMessage = onErrorMessage;
    }
    
    @Override
    public void onFailure(Method method, Throwable exception) {
        bus.fireEvent(new NotificationEvent(Level.ERROR, this.errorMessage, exception));
    }
}
