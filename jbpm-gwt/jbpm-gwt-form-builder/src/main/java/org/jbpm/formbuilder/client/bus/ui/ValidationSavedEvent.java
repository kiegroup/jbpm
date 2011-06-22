package org.jbpm.formbuilder.client.bus.ui;

import java.util.List;

import org.jbpm.formbuilder.client.validation.FBValidationItem;

import com.google.gwt.event.shared.GwtEvent;

public class ValidationSavedEvent extends GwtEvent<ValidationSavedHandler> {

    public static final Type<ValidationSavedHandler> TYPE = new Type<ValidationSavedHandler>();
    
    private final List<FBValidationItem> validations;
    
    public ValidationSavedEvent(List<FBValidationItem> validations) {
        this.validations = validations;
    }
    
    public List<FBValidationItem> getValidations() {
        return validations;
    }

    @Override
    public Type<ValidationSavedHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(ValidationSavedHandler handler) {
        handler.onEvent(this);
    }

}
