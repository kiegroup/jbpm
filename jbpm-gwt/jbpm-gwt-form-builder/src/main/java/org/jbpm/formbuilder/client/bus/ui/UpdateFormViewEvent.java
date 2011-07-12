package org.jbpm.formbuilder.client.bus.ui;

import org.jbpm.formbuilder.shared.rep.FormRepresentation;

import com.google.gwt.event.shared.GwtEvent;

public class UpdateFormViewEvent extends GwtEvent<UpdateFormViewHandler> {

    public static final Type<UpdateFormViewHandler> TYPE = new Type<UpdateFormViewHandler>();
    
    private final FormRepresentation formRepresentation;
    
    public UpdateFormViewEvent(FormRepresentation form) {
        super();
        this.formRepresentation = form;
    }

    public FormRepresentation getFormRepresentation() {
        return formRepresentation;
    }
    
    @Override
    public Type<UpdateFormViewHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(UpdateFormViewHandler handler) {
        handler.onEvent(this);
    }

}
