package org.jbpm.formbuilder.client.bus;

import org.jbpm.formbuilder.client.menu.FormItem;

import com.google.gwt.event.shared.GwtEvent;

public class FormItemSelectedEvent extends GwtEvent<FormItemSelectedEventHandler> {

    public static Type<FormItemSelectedEventHandler> TYPE = new Type<FormItemSelectedEventHandler>();
    
    private final FormItem formItemSelected;
    
    public FormItemSelectedEvent(FormItem formItemSelected) {
        super();
        this.formItemSelected = formItemSelected;
    }
    
    public FormItem getFormItemSelected() {
        return formItemSelected;
    }
    
    @Override
    public Type<FormItemSelectedEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(FormItemSelectedEventHandler handler) {
        handler.onEvent(this);
        
    }

}
