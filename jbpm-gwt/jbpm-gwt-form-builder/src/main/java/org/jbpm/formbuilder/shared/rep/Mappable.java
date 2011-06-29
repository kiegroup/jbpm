package org.jbpm.formbuilder.shared.rep;

import java.util.Map;

public interface Mappable {

    Map<String, Object> getDataMap();
    
    void setDataMap(Map<String, Object> dataMap);
}
