package org.jbpm.formbuilder.client.bus.ui;

import org.jbpm.formbuilder.client.form.FBFormItem;

import com.google.gwt.event.shared.GwtEvent;

public class FormItemRemovedEvent extends GwtEvent<FormItemRemovedHandler> {

    public static final Type<FormItemRemovedHandler> TYPE = new Type<FormItemRemovedHandler>();
    
    private final FBFormItem formItem;
    
    public FormItemRemovedEvent(FBFormItem formItem) {
        super();
        this.formItem = formItem;
    }

    public FBFormItem getFormItem() {
        return formItem;
    }
    
    @Override
    public Type<FormItemRemovedHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(FormItemRemovedHandler handler) {
        handler.onEvent(this);
    }

}
