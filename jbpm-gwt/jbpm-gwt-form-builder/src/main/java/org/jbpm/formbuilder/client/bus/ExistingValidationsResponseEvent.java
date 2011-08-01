package org.jbpm.formbuilder.client.bus;

import java.util.List;

import org.jbpm.formbuilder.client.validation.FBValidationItem;

import com.google.gwt.event.shared.GwtEvent;

public class ExistingValidationsResponseEvent extends GwtEvent<ExistingValidationsResponseHandler> {

    public static final Type<ExistingValidationsResponseHandler> TYPE = new Type<ExistingValidationsResponseHandler>();
    
    private final List<FBValidationItem> existingValidations;
    
    public ExistingValidationsResponseEvent(List<FBValidationItem> existingValidations) {
        super();
        this.existingValidations = existingValidations;
    }
    
    public List<FBValidationItem> getExistingValidations() {
        return existingValidations;
    }

    @Override
    public Type<ExistingValidationsResponseHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(ExistingValidationsResponseHandler handler) {
        handler.onEvent(this);
    }

}
