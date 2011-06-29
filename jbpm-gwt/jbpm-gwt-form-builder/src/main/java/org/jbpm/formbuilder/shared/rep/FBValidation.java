package org.jbpm.formbuilder.shared.rep;

public interface FBValidation extends Mappable {

    boolean isValid(FormItemRepresentation item);
    
    FBValidation cloneValidation();
    
    String getValidationId();
}
