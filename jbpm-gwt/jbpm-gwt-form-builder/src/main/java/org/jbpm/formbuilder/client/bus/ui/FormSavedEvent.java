package org.jbpm.formbuilder.client.bus.ui;

import org.jbpm.formbuilder.shared.rep.FormRepresentation;

import com.google.gwt.event.shared.GwtEvent;

public class FormSavedEvent extends GwtEvent<FormSavedHandler> {

    public static final Type<FormSavedHandler> TYPE = new Type<FormSavedHandler>();
    
    private final FormRepresentation form;
    
    public FormSavedEvent(FormRepresentation form) {
        this.form = form;
    }
    
    public FormRepresentation getForm() {
        return form;
    }
    
    @Override
    public Type<FormSavedHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(FormSavedHandler handler) {
        handler.onEvent(this);
    }

}
