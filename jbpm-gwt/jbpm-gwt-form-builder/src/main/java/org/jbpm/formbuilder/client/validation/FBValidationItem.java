package org.jbpm.formbuilder.client.validation;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.formbuilder.shared.rep.FBValidation;

import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;

public abstract class FBValidationItem {

    private final Map<String, HasValue<String>> propertiesMap = new HashMap<String, HasValue<String>>();
    
    public FBValidationItem() {
    }
    
    public Map<String, HasValue<String>> getPropertiesMap() {
        return propertiesMap;
    }
    
    public void populatePropertiesMap(Map<String, HasValue<String>> map) {
        propertiesMap.putAll(map);
    }
    
    public <T extends FBValidation> T getRepresentation(T representation) {
        for (Map.Entry<String, HasValue<String>> entry : propertiesMap.entrySet()) {
            representation.setProperty(entry.getKey(), entry.getValue().getValue());
        }
        return representation;
    }
    
    public abstract String getName();

    public abstract FBValidation createValidation();
    
    public abstract Widget createDisplay();

    public abstract FBValidationItem cloneItem();
}
