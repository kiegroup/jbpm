package org.jbpm.formbuilder.client.bus;

import com.google.gwt.event.shared.GwtEvent;

public class FormItemDeselectedEvent extends GwtEvent<FormItemDeselectedEventHandler> {

    public static Type<FormItemDeselectedEventHandler> TYPE = new Type<FormItemDeselectedEventHandler>();
    
    public FormItemDeselectedEvent() {
        super();
    }
    
    @Override
    public Type<FormItemDeselectedEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(FormItemDeselectedEventHandler handler) {
        handler.onEvent(this);
        
    }

}
