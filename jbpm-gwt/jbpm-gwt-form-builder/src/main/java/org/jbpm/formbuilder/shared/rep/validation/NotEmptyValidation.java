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

    public String getJsonCode() {
        StringBuilder builder = new StringBuilder("{");
        builder.append("'className': 'org.jbpm.formbuilder.shared.rep.validation.NotEmptyValidtion', ");
        builder.append("'propertiesMap': ");
        if (propertiesMap == null) {
            builder.append("null");
        } else {
            builder.append("{");
            for (Map.Entry<String, String> entry : propertiesMap.entrySet()) {
                builder.append("'").append(entry.getKey()).append("': '").append(entry.getValue()).append("', ");
            }
            builder.append("}");
        }
        return builder.append("}").toString();
    }
}
