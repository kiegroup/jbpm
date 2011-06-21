package org.jbpm.formbuilder.shared.rep;

import java.util.Map;

public interface FBValidation {

    boolean isValid();
    
    FBValidation clone();
    
    String getName();
    
    Map<String, String> getPropertiesMap();
    
    void setProperty(String name, String value);
}
