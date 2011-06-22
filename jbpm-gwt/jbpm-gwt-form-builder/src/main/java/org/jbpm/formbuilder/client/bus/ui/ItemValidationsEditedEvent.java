package org.jbpm.formbuilder.client.bus.ui;

import java.util.List;

import org.jbpm.formbuilder.client.validation.FBValidationItem;

import com.google.gwt.event.shared.GwtEvent;

public class ItemValidationsEditedEvent extends GwtEvent<ItemValidationsEditedHandler> {
    
    public static final Type<ItemValidationsEditedHandler> TYPE = new Type<ItemValidationsEditedHandler>();

    private final List<FBValidationItem> validations;
    
    public ItemValidationsEditedEvent(List<FBValidationItem> validations) {
        this.validations = validations;
    }
    
    public List<FBValidationItem> getValidations() {
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
