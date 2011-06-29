package org.jbpm.formbuilder.server.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.shared.form.FormEncodingException;
import org.jbpm.formbuilder.shared.form.FormRepresentationDecoder;
import org.jbpm.formbuilder.shared.rep.FBScript;
import org.jbpm.formbuilder.shared.rep.FBValidation;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;
import org.jbpm.formbuilder.shared.rep.InputData;
import org.jbpm.formbuilder.shared.rep.Mappable;
import org.jbpm.formbuilder.shared.rep.OutputData;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class FormRepresentationDecoderImpl implements FormRepresentationDecoder {
    
    public FormRepresentationDecoderImpl() {
    }
    
    public Object decode(Map<String, Object> data) throws FormEncodingException {
        if (data == null) {
            return null;
        }
        String className = (String) data.get("@className");
        Object obj = null;
        try {
            obj = Class.forName(className).newInstance();
            if (obj instanceof Mappable) {
                Mappable item = (Mappable) obj;
                item.setDataMap(data);
            } else {
                throw new FormEncodingException("Type " + obj.getClass().getName() + " cannot be casted to FormItemRepresentation");
            }
        } catch (InstantiationException e) {
            throw new FormEncodingException("Couldn't instantiate class " + className, e);
        } catch (IllegalAccessException e) {
            throw new FormEncodingException("Couldn't access constructor of class " + className, e);
        } catch (ClassNotFoundException e) {
            throw new FormEncodingException("Couldn't find class " + className, e);
        }
        return obj;
    }
    
    public FormRepresentation decode(String code) throws FormEncodingException {
        FormRepresentation form = new FormRepresentation();
        JsonElement json = new JsonParser().parse(code);
        if (json.isJsonObject()) {
            JsonObject jsonObj = json.getAsJsonObject();
            form.setAction(jsonObj.get("action").getAsString());
            form.setDocumentation(jsonObj.get("documentation").getAsString());
            form.setEnctype(jsonObj.get("enctype").getAsString());
            form.setLastModified(jsonObj.get("lastModified").getAsLong());
            form.setMethod(jsonObj.get("method").getAsString());
            form.setName(jsonObj.get("name").getAsString());
            form.setTaskId(jsonObj.get("taskId").getAsString());
            form.setFormItems(decodeList(jsonObj.get("formItems"), FormItemRepresentation.class));
            form.setFormValidations(decodeList(jsonObj.get("formValidations"), FBValidation.class));
            form.setInputs(decodeStringIndexedMap(jsonObj.get("inputs"), InputData.class));
            form.setOutputs(decodeStringIndexedMap(jsonObj.get("outputs"), OutputData.class));
            form.setOnLoadScripts(decodeList(jsonObj.get("onLoadScripts"), FBScript.class));
            form.setOnSubmitScripts(decodeList(jsonObj.get("onSubmitScripts"), FBScript.class));
        }
        return form;
    }

    @SuppressWarnings("unchecked")
    public <V> Map<String, V> decodeStringIndexedMap(JsonElement json, Class<V> valueType) throws FormEncodingException {
        Map<String, V> retval = new HashMap<String, V>();
        if (json != null && json.isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
                if (entry.getValue().isJsonObject()) {
                    JsonObject jsonObj = entry.getValue().getAsJsonObject();
                    retval.put(entry.getKey(), (V) decode(asMap(jsonObj)));
                }
            }
        }
        return retval;
    }
    
    @SuppressWarnings("unchecked")
    public <T> List<T> decodeList(JsonElement json, Class<T> elemType) throws FormEncodingException {
        List<T> retval = new ArrayList<T>();
        if (json != null && json.isJsonArray()) {
            JsonArray array = json.getAsJsonArray();
            for (int index = 0; index < array.size(); index++) {
                JsonElement elem = array.get(index);
                JsonObject jsonObj = elem.getAsJsonObject();
                retval.add((T) decode(asMap(jsonObj)));
            }
        }
        return retval;
    }
    
    private Map<String, Object> asMap(JsonObject jsonObj) {
        Map<String, Object> retval = new HashMap<String, Object>();
        for (Map.Entry<String, JsonElement> entry : jsonObj.entrySet()) {
            retval.put(entry.getKey(), fromJsonValue(entry.getValue()));
        }
        return retval;
    }
    
    private List<Object> asList(JsonArray array) {
        List<Object> retval = new ArrayList<Object>();
        if (array != null) {
            for (JsonElement elem : array) {
                retval.add(fromJsonValue(elem));
            }
        }
        return retval;
    }
    
    private Object fromJsonValue(JsonElement elem) {
        if (elem.isJsonPrimitive() && elem.getAsJsonPrimitive().isString()) {
            return elem.getAsString();
        } else if (elem.isJsonPrimitive() && elem.getAsJsonPrimitive().isNumber()) {
            return elem.getAsNumber();
        } else if (elem.isJsonArray()) {
            return asList(elem.getAsJsonArray());
        } else if (elem.isJsonNull()) {
            return null;
        } else if (elem.isJsonObject()) {
            return asMap(elem.getAsJsonObject());
        } else {
            return "";
        }
    }
}
