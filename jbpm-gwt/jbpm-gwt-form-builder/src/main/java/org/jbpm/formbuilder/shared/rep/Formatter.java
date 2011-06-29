package org.jbpm.formbuilder.shared.rep;

import java.util.Map;

public interface Formatter {

    Object format(Object object);
    
    String getJsonCode();

	Map<String, Object> getDataMap();
}
