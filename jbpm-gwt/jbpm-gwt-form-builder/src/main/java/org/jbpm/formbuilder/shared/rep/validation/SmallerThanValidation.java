package org.jbpm.formbuilder.shared.rep.validation;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.formbuilder.shared.rep.FBValidation;

import com.gwtent.reflection.client.Reflectable;

@Reflectable
public class SmallerThanValidation implements FBValidation {
    
    private Map<String, Object> propertiesMap = new HashMap<String, Object>();
    private Comparable<?> value;
    
    @Override
    public boolean isValid(Object obj) {
        if (obj instanceof Comparable<?>) {
            @SuppressWarnings("unchecked")
            Comparable<Object> comp = (Comparable<Object>) obj;
            return comp.compareTo(value) < 0;
        }
        return String.valueOf(obj).compareTo(String.valueOf(value)) < 0;
    }

    @Override
    public String getValidationId() {
        return "smallerThan";
    }

    @Override
    public FBValidation cloneValidation() {
        SmallerThanValidation validation = new SmallerThanValidation();
        validation.setDataMap(getDataMap());
        return validation;
    }

    @Override
    public Map<String, Object> getDataMap() {
        if (!propertiesMap.containsKey("@className") ||
                !SmallerThanValidation.class.getName().equals(propertiesMap.get("@className"))) {
            propertiesMap.put("@className", SmallerThanValidation.class.getName());
        }
        propertiesMap.put("value", this.value);
        return propertiesMap;
    }

    @Override
    public void setDataMap(Map<String, Object> dataMap) {
        if (dataMap == null) {
            dataMap = new HashMap<String, Object>();
        }
        this.propertiesMap = dataMap;
        this.value = (Comparable<?>) dataMap.get("value");
    }
}
