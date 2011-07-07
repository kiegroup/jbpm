package org.jbpm.formbuilder.client.bus.ui;

import org.jbpm.formbuilder.client.form.FBFormItem;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.Widget;

public class FormItemAddedEvent extends GwtEvent<FormItemAddedHandler> {

    public static final Type<FormItemAddedHandler> TYPE = new Type<FormItemAddedHandler>();
    
    private final FBFormItem formItem;
    private final Widget formItemHolder;
    
    public FormItemAddedEvent(FBFormItem formItem, Widget formItemHolder) {
        super();
        this.formItem = formItem;
        this.formItemHolder = formItemHolder;
    }

    public FBFormItem getFormItem() {
        return formItem;
    }
    
    public Widget getFormItemHolder() {
        return formItemHolder;
    }
    
    @Override
    public Type<FormItemAddedHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(FormItemAddedHandler handler) {
        handler.onEvent(this);
    }

}
