package org.jbpm.formbuilder.shared.menu;

import java.util.HashMap;
import java.util.Map;

public class ValidationDescription {

    private String className;
    private Map<String, String> properties;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Map<String, String> getProperties() {
        if (properties == null) {
            properties = new HashMap<String, String>();
        }
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
}
