package org.jbpm.formbuilder.shared.api.validation;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.formbuilder.shared.api.FBValidation;

public class IsNumberValidation implements FBValidation {

    private Map<String, Object> propertiesMap = new HashMap<String, Object>();
    
    @Override
    public boolean isValid(Object item) {
        if (item == null) {
            return false;
        }
        if (item instanceof Number) {
            return true;
        }
        try {
            Double.parseDouble(item.toString());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    @Override
    public String getValidationId() {
        return "isNumber";
    }

    @Override
    public FBValidation cloneValidation() {
        IsNumberValidation validation = new IsNumberValidation();
        validation.setDataMap(getDataMap());
        return validation;
    }

    @Override
    public Map<String, Object> getDataMap() {
        if (!propertiesMap.containsKey("@className") ||
                !IsNumberValidation.class.getName().equals(propertiesMap.get("@className"))) {
            propertiesMap.put("@className", IsNumberValidation.class.getName());
        }
        return propertiesMap;
    }

    @Override
    public void setDataMap(Map<String, Object> dataMap) {
        if (dataMap == null) {
            dataMap = new HashMap<String, Object>();        
        }
        this.propertiesMap = dataMap;
    }
}
