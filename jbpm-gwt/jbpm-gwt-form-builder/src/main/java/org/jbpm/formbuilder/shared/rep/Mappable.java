package org.jbpm.formbuilder.shared.rep;

import java.util.Map;

import org.jbpm.formbuilder.shared.form.FormEncodingException;

public interface Mappable {

    Map<String, Object> getDataMap();
    
    void setDataMap(Map<String, Object> dataMap) throws FormEncodingException;
}
