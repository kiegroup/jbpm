package org.jbpm.formbuilder.client.bus;

import com.google.gwt.event.shared.GwtEvent;

public class EvaluateScriptEvent extends GwtEvent<EvaluateScriptEventHandler> {

    public static final Type<EvaluateScriptEventHandler> TYPE = new Type<EvaluateScriptEventHandler>();
    
    @Override
    public Type<EvaluateScriptEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(EvaluateScriptEventHandler handler) {
        handler.onEvent(this);
    }

}
