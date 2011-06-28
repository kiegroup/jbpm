package org.jbpm.formbuilder.shared.rep;

public interface Formatter {

    Object format(Object object);
    
    String getJsonCode();
}
