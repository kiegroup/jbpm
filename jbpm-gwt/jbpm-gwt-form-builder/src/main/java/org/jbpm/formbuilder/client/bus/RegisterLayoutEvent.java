package org.jbpm.formbuilder.client.bus;

import org.jbpm.formbuilder.client.form.LayoutFormItem;

import com.google.gwt.event.shared.GwtEvent;

public class RegisterLayoutEvent extends GwtEvent<RegisterLayoutEventHandler> {

    public static final Type<RegisterLayoutEventHandler> TYPE = new Type<RegisterLayoutEventHandler>();
    
    private final LayoutFormItem layout;
    
    public RegisterLayoutEvent(LayoutFormItem layout) {
        this.layout = layout;
    }
    
    public LayoutFormItem getLayout() {
        return layout;
    }
    
    @Override
    public Type<RegisterLayoutEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(RegisterLayoutEventHandler handler) {
        handler.onEvent(this);
    }

    
}
