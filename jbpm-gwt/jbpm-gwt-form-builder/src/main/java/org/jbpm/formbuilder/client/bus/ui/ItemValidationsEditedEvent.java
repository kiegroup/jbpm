package org.jbpm.formbuilder.client.bus.ui;

import java.util.List;

import org.jbpm.formbuilder.shared.rep.FBValidation;

import com.google.gwt.event.shared.GwtEvent;

public class ItemValidationsEditedEvent extends GwtEvent<ItemValidationsEditedHandler> {
    
    public static final Type<ItemValidationsEditedHandler> TYPE = new Type<ItemValidationsEditedHandler>();

    private final List<FBValidation> validations;
    
    public ItemValidationsEditedEvent(List<FBValidation> validations) {
        this.validations = validations;
    }
    
    public List<FBValidation> getValidations() {
        return validations;
    }

    @Override
    public Type<ItemValidationsEditedHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(ItemValidationsEditedHandler handler) {
        handler.onEvent(this);
    }

}
