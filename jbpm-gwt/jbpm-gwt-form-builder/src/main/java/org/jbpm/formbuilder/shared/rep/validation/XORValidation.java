package org.jbpm.formbuilder.shared.rep.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.shared.form.FormEncodingException;
import org.jbpm.formbuilder.shared.form.FormEncodingFactory;
import org.jbpm.formbuilder.shared.rep.FBValidation;

import com.gwtent.reflection.client.Reflectable;

@Reflectable
public class XORValidation implements FBValidation {

    //TODO create the FBValidationItem associated with this FBValidation
    private Map<String, Object> propertiesMap = new HashMap<String, Object>();
    private List<FBValidation> validations = new ArrayList<FBValidation>();
    
    @Override
    public boolean isValid(Object obj) {
        if (validations != null && !validations.isEmpty()) {
            Iterator<FBValidation> iterator = validations.iterator();
            boolean retval = true && iterator.next().isValid(obj);
            while (iterator.hasNext()) {
                retval = retval ^ iterator.next().isValid(obj);
            }
            return retval;
        }
        return true;
    }

    @Override
    public String getValidationId() {
        return "xor";
    }

    @Override
    public Map<String, Object> getDataMap() {
        if (!propertiesMap.containsKey("@className") || !XORValidation.class.getName().equals(propertiesMap.get("@className"))) {
            propertiesMap.put("@className", XORValidation.class.getName());
        }
        List<Object> validationsMap = new ArrayList<Object>();
        if (validations != null && !validations.isEmpty()) {
            for (FBValidation validation : validations) {
                validationsMap.add(validation.getDataMap());
            }
        }
        propertiesMap.put("validations", validationsMap);
        return propertiesMap;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setDataMap(Map<String, Object> dataMap)
            throws FormEncodingException {
        if (dataMap == null) {
            dataMap = new HashMap<String, Object>();
        }
        this.propertiesMap = dataMap;
        List<Object> validationsMap = (List<Object>) dataMap.get("validations");
        this.validations.clear();
        if (validationsMap != null) {
            for (Object obj : validationsMap) {
                Map<String, Object> subMap = (Map<String, Object>) obj;
                FBValidation subVal = (FBValidation) FormEncodingFactory.getDecoder().decode(subMap);
                this.validations.add(subVal);
            }
        }
    }
    
    @Override
    public FBValidation cloneValidation() {
        XORValidation validation = new XORValidation();
        validation.validations.addAll(validations);
        validation.propertiesMap.putAll(propertiesMap);
        return validation;
    }
}
