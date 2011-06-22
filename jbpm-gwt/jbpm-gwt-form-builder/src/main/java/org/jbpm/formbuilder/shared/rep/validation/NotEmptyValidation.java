package org.jbpm.formbuilder.shared.rep.validation;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.formbuilder.shared.rep.FBValidation;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;

public class NotEmptyValidation implements FBValidation {

    private Map<String, String> propertiesMap = new HashMap<String, String>();
    
    public boolean isValid(FormItemRepresentation item) {
        return item != null && item.getInput() != null && item.getInput().getValue() != null && !item.getInput().getValue().trim().equals("");
    }
    
    public String getValidationId() {
        return "notEmpty";
    }

    public Map<String, String> getPropertiesMap() {
        return propertiesMap;
    }

    public void setProperty(String name, String value) {
        propertiesMap.put(name, value);
    }

    public FBValidation cloneValidation() {
        NotEmptyValidation validation = new NotEmptyValidation();
        for (Map.Entry<String, String> entry : propertiesMap.entrySet()) {
            validation.setProperty(entry.getKey(), entry.getValue());
        }
        return validation;
    }
}
