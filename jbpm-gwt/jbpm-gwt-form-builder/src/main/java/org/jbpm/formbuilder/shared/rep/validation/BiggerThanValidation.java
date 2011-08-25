package org.jbpm.formbuilder.shared.rep.validation;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.formbuilder.shared.rep.FBValidation;

import com.gwtent.reflection.client.Reflectable;

@Reflectable
public class BiggerThanValidation implements FBValidation {

    private Map<String, Object> propertiesMap = new HashMap<String, Object>();
    private Comparable<?> value;
    
    @Override
    public boolean isValid(Object obj) {
        if (obj instanceof Comparable<?>) {
            @SuppressWarnings("unchecked")
            Comparable<Object> comp = (Comparable<Object>) obj;
            return comp.compareTo(value) > 0;
        }
        return String.valueOf(obj).compareTo(String.valueOf(value)) > 0;
    }

    @Override
    public String getValidationId() {
        return "biggerThan";
    }

    @Override
    public FBValidation cloneValidation() {
        BiggerThanValidation validation = new BiggerThanValidation();
        validation.setDataMap(getDataMap());
        return validation;
    }

    @Override
    public Map<String, Object> getDataMap() {
        if (!propertiesMap.containsKey("@className") ||
                !BiggerThanValidation.class.getName().equals(propertiesMap.get("@className"))) {
            propertiesMap.put("@className", BiggerThanValidation.class.getName());
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
