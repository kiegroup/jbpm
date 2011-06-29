package org.jbpm.formbuilder.client.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.shared.form.FormEncodingException;
import org.jbpm.formbuilder.shared.form.FormRepresentationDecoder;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;
import org.jbpm.formbuilder.shared.rep.Mappable;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.rpc.impl.ReflectionHelper;

public class FormRepresentationDecoderClient implements FormRepresentationDecoder {

    
    public FormRepresentationDecoderClient() {
    }
    
    public FormRepresentation decode(String code) throws FormEncodingException {
        FormRepresentation form = new FormRepresentation();
        
        JSONValue json = JSONParser.parseLenient(code);
        if (json.isObject() != null) {
            JSONObject jsonObj = json.isObject();
            form.setAction(jsonObj.get("action").isString().stringValue());
            form.setDocumentation(jsonObj.get("documentation").isString().stringValue());
            form.setEnctype(jsonObj.get("enctype").isString().stringValue());
            form.setLastModified(Double.valueOf(jsonObj.get("lastModified").isNumber().doubleValue()).longValue());
            form.setMethod(jsonObj.get("method").isString().stringValue());
            form.setName(jsonObj.get("name").isString().stringValue());
            form.setTaskId(jsonObj.get("taskId").isString().stringValue());
            form.setFormItems(decodeItems(jsonObj.get("formItems")));
            
            /* TODO implement
            form.setFormValidations(formValidations);
            form.setInputs(inputs);
            form.setOnLoadScripts(onLoadScripts);
            form.setOnSubmitScripts(onSubmitScripts);
            form.setOutputs(outputs); */
        }
        //TODO implement
        return form;
    }
    
    public Object decode(Map<String, Object> data) throws FormEncodingException {
        String className = (String) data.get("@className");
        Object obj = null;
        try {
            Class<?> clazz = ReflectionHelper.loadClass(className);
            obj = ReflectionHelper.newInstance(clazz);
            if (obj instanceof Mappable) {
                Mappable rep = (Mappable) obj;
                rep.setDataMap(data);
            } else {
                throw new FormEncodingException("Type " + obj.getClass().getName() + " cannot be casted to Mappable");
            }
        } catch (Exception e) {
            throw new FormEncodingException("Couldn't instantiate class " + className, e);
        }
        return obj;
    }
    
    public List<FormItemRepresentation> decodeItems(JSONValue json) throws FormEncodingException {
        List<FormItemRepresentation> retval = new ArrayList<FormItemRepresentation>();
        if (json.isArray() != null) {
            JSONArray array = json.isArray();
            for (int index = 0; index < array.size(); index++) {
                JSONValue elem = array.get(index);
                JSONObject jsonObj = elem.isObject();
                if (jsonObj != null) {
                    retval.add((FormItemRepresentation) decode(toMap(jsonObj)));
                }
            }
        }
        return retval;
    }

    private Map<String, Object> toMap(JSONObject jsonObj) {
        Map<String, Object> map = new HashMap<String, Object>();
        for (String key : jsonObj.keySet()) {
            map.put(key, toProperValue(jsonObj.get(key)));
        }
        return map;
    }
    
    private Object toProperValue(JSONValue jsonValue) {
        Object retval = null;
        if (jsonValue.isArray() != null) {
            List<Object> list = new ArrayList<Object>();
            JSONArray array = jsonValue.isArray();
            for (int index = 0; index < array.size(); index++) {
                list.add(toProperValue(array.get(index)));
            }
            retval = list;
        } else if (jsonValue.isBoolean() != null) {
            retval = Boolean.valueOf(jsonValue.isBoolean().booleanValue());
        } else if (jsonValue.isNumber() != null) {
            retval = Double.valueOf(jsonValue.isNumber().doubleValue());
        } else if (jsonValue.isObject() != null) {
            retval = toMap(jsonValue.isObject());
        } else if (jsonValue.isString() != null) {
            retval = jsonValue.isString().stringValue();
        }
        return retval;
    }
}
