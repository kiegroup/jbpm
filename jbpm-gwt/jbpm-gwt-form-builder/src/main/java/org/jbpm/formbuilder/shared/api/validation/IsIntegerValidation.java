package org.jbpm.formbuilder.shared.api.validation;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.formbuilder.shared.api.FBValidation;

public class IsIntegerValidation implements FBValidation {

    private Map<String, Object> propertiesMap = new HashMap<String, Object>();
    
    @Override
    public boolean isValid(Object item) {
        if (item == null) {
            return false;
        }
        if (item instanceof Integer) {
            return true;
        }
        try {
            Integer.parseInt(item.toString());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    @Override
    public String getValidationId() {
        return "isInteger";
    }

    @Override
    public FBValidation cloneValidation() {
        IsIntegerValidation validation = new IsIntegerValidation();
        validation.setDataMap(getDataMap());
        return validation;
    }

    @Override
    public Map<String, Object> getDataMap() {
        if (!propertiesMap.containsKey("@className") ||
                !IsIntegerValidation.class.getName().equals(propertiesMap.get("@className"))) {
            propertiesMap.put("@className", IsIntegerValidation.class.getName());
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
