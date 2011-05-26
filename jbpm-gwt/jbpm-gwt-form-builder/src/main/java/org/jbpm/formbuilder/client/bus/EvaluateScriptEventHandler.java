package org.jbpm.formbuilder.client.bus;

import com.google.gwt.event.shared.EventHandler;

public interface EvaluateScriptEventHandler extends EventHandler {

    void onEvent(EvaluateScriptEvent event);
}
