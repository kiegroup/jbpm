package org.jbpm.formbuilder.client.bus;

import org.jbpm.formbuilder.client.form.FBFormItem;

import com.google.gwt.event.shared.GwtEvent;

public class FormItemSelectionEvent extends GwtEvent<FormItemSelectionEventHandler> {

    public static Type<FormItemSelectionEventHandler> TYPE = new Type<FormItemSelectionEventHandler>();
    
    private final FBFormItem formItemSelected;
    private final boolean selected;
    
    public FormItemSelectionEvent(FBFormItem formItemSelected, boolean selected) {
        super();
        this.formItemSelected = formItemSelected;
        this.selected = selected;
    }
    
    public FBFormItem getFormItemSelected() {
        return formItemSelected;
    }
    
    public boolean isSelected() {
        return selected;
    }
    
    @Override
    public Type<FormItemSelectionEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(FormItemSelectionEventHandler handler) {
        handler.onEvent(this);
        
    }

}
