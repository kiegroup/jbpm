package org.jbpm.formbuilder.client.validation;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.formbuilder.client.bus.NotificationEvent;
import org.jbpm.formbuilder.client.bus.NotificationEvent.Level;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.shared.form.FormEncodingException;
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
        Map<String, Object> dataMap = new HashMap<String, Object>();
        for (Map.Entry<String, HasValue<String>> entry : propertiesMap.entrySet()) {
            dataMap.put(entry.getKey(), entry.getValue().getValue());
        }
        try {
            representation.setDataMap(dataMap);
        } catch (FormEncodingException e) {
            FormBuilderGlobals.getInstance().getEventBus().fireEvent(
                    new NotificationEvent(Level.ERROR, "Couldn't create validation item representation", e));
        }
        return representation;
    }
    
    public abstract String getName();

    public abstract FBValidation createValidation();
    
    public abstract Widget createDisplay();

    public abstract FBValidationItem cloneItem();
}
