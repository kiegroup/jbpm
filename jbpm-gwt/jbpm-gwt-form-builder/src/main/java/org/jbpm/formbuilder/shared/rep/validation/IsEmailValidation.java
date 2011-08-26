package org.jbpm.formbuilder.shared.rep.validation;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.formbuilder.shared.rep.FBValidation;

public class IsEmailValidation implements FBValidation {

    private Map<String, Object> propertiesMap = new HashMap<String, Object>();
    
    @Override
    public boolean isValid(Object item) {
        if (item == null) {
            return false;
        }
        String reg = "/^([A-Za-z0-9_\\-\\.])+\\@([A-Za-z0-9_\\-\\.])+\\.([A-Za-z]{2,4})$/";
        return reg.matches(item.toString());
    }
    
    @Override
    public String getValidationId() {
        return "isEmail";
    }

    @Override
    public FBValidation cloneValidation() {
        IsEmailValidation validation = new IsEmailValidation();
        validation.setDataMap(getDataMap());
        return validation;
    }

    @Override
    public Map<String, Object> getDataMap() {
        if (!propertiesMap.containsKey("@className") ||
                !IsEmailValidation.class.getName().equals(propertiesMap.get("@className"))) {
            propertiesMap.put("@className", IsEmailValidation.class.getName());
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
