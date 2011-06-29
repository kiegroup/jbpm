package org.jbpm.formbuilder.shared.rep.validation;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.formbuilder.shared.rep.FBValidation;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;

public class NotEmptyValidation implements FBValidation {

    private Map<String, Object> propertiesMap = new HashMap<String, Object>();
    
    public boolean isValid(FormItemRepresentation item) {
        return item != null && item.getInput() != null && item.getInput().getValue() != null && !item.getInput().getValue().trim().equals("");
    }
    
    public String getValidationId() {
        return "notEmpty";
    }

    public FBValidation cloneValidation() {
        NotEmptyValidation validation = new NotEmptyValidation();
        validation.setDataMap(getDataMap());
        return validation;
    }

    public Map<String, Object> getDataMap() {
        if (!propertiesMap.containsKey("@className") ||
                !NotEmptyValidation.class.getName().equals(propertiesMap.get("@className"))) {
            propertiesMap.put("@className", NotEmptyValidation.class.getName());
        }
        return propertiesMap;
    }

    public void setDataMap(Map<String, Object> dataMap) {
        if (dataMap == null) {
            dataMap = new HashMap<String, Object>();        }
        this.propertiesMap = dataMap;
    }
}
