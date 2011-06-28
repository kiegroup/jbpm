package org.jbpm.formbuilder.shared.rep;

import java.util.Map;

public interface FBValidation {

    boolean isValid(FormItemRepresentation item);
    
    FBValidation cloneValidation();
    
    String getValidationId();
    
    Map<String, String> getPropertiesMap();
    
    void setProperty(String name, String value);

    String getJsonCode();
}
