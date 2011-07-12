package org.jbpm.formbuilder.client.bus.ui;

import org.jbpm.formbuilder.client.form.FBForm;

import com.google.gwt.event.shared.GwtEvent;

public class GetFormDisplayEvent extends GwtEvent<GetFormDisplayHandler> {

    public static final Type<GetFormDisplayHandler> TYPE = new Type<GetFormDisplayHandler>();
    
    private FBForm formDisplay;
    
    public GetFormDisplayEvent() {
        super();
    }
    
    public FBForm getFormDisplay() {
        return formDisplay;
    }

    public void setFormDisplay(FBForm formDisplay) {
        this.formDisplay = formDisplay;
    }

    @Override
    public Type<GetFormDisplayHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(GetFormDisplayHandler handler) {
        handler.onEvent(this);
    }

}
