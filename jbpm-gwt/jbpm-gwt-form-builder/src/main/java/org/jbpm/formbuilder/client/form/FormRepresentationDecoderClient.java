/**
 * Copyright 2011 JBoss Inc 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.formbuilder.client.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.shared.form.FormEncodingException;
import org.jbpm.formbuilder.shared.form.FormRepresentationDecoder;
import org.jbpm.formbuilder.shared.menu.MenuItemDescription;
import org.jbpm.formbuilder.shared.rep.FBScript;
import org.jbpm.formbuilder.shared.rep.FBValidation;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;
import org.jbpm.formbuilder.shared.rep.InputData;
import org.jbpm.formbuilder.shared.rep.Mappable;
import org.jbpm.formbuilder.shared.rep.OutputData;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.rpc.impl.ReflectionHelper;

/**
 * Decodes json to different objects on the client side
 */
public class FormRepresentationDecoderClient implements FormRepresentationDecoder {

    public Object decode(Map<String, Object> data) throws FormEncodingException {
        if (data == null) {
            return null;
        }
        String className = (String) data.get("@className");
        Object obj = null;
        try {
            Class<?> clazz = ReflectionHelper.loadClass(className);
            obj = ReflectionHelper.newInstance(clazz);
            if (obj instanceof Mappable) {
                Mappable item = (Mappable) obj;
                item.setDataMap(data);
            } else {
                throw new FormEncodingException("Type " + obj.getClass().getName() + " cannot be casted to FormItemRepresentation");
            }
        } catch (Exception e) {
            throw new FormEncodingException("Couldn't instantiate class " + className, e);
        }
        return obj;
    }
    
    public FormRepresentation decode(String code) throws FormEncodingException {
        FormRepresentation form = new FormRepresentation();
        JSONValue json = JSONParser.parseLenient(code);
        if (json.isObject() != null) {
            JSONObject jsonObj = json.isObject();
            if (jsonObj.get("action").isString() != null) {
                form.setAction(jsonObj.get("action").isString().stringValue());
            }
            if (jsonObj.get("documentation").isString() != null) {
                form.setDocumentation(jsonObj.get("documentation").isString().stringValue());
            }
            if (jsonObj.get("enctype").isString() != null) {
                form.setEnctype(jsonObj.get("enctype").isString().stringValue());
            }
            if (jsonObj.get("lastModified").isNumber() != null) {
                form.setLastModified(Double.valueOf(jsonObj.get("lastModified").isNumber().doubleValue()).longValue());
            } else if (jsonObj.get("lastModified").isString() != null) {
                form.setLastModified(Double.valueOf(jsonObj.get("lastModified").isString().stringValue()).longValue());
            }
            if (jsonObj.get("method").isString() != null) {
                form.setMethod(jsonObj.get("method").isString().stringValue());
            }
            if (jsonObj.get("name").isString() != null) {
                form.setName(jsonObj.get("name").isString().stringValue());
            }
            if (jsonObj.get("taskId").isString() != null) {
                form.setTaskId(jsonObj.get("taskId").isString().stringValue());
            }
            form.setFormItems(decodeItems(jsonObj.get("formItems")));
            form.setFormValidations(decodeValidations(jsonObj.get("formValidations")));
            form.setInputs(decodeInputs(jsonObj.get("inputs")));
            form.setOutputs(decodeOutputs(jsonObj.get("outputs")));
            form.setOnLoadScripts(decodeScripts(jsonObj.get("onLoadScripts")));
            form.setOnSubmitScripts(decodeScripts(jsonObj.get("onSubmitScripts")));
        }
        return form;
    }
    
    @SuppressWarnings("unchecked")
    public Map<String, List<MenuItemDescription>> decodeMenuItemsMap(String json) throws FormEncodingException {
    	JSONObject jsonObj = JSONParser.parseLenient(json).isObject();
    	Map<String, Object> dataMap = asMap(jsonObj);
    	Map<String, List<MenuItemDescription>> retval = null;
    	if (dataMap != null) {
    		retval = new HashMap<String, List<MenuItemDescription>>();
    		for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
    			List<MenuItemDescription> itemsList = new ArrayList<MenuItemDescription>();
    			String key = entry.getKey();
    			Object obj = entry.getValue();
    			if (obj != null) {
    				List<Object> itemsMapList = (List<Object>) obj;
    				for (Object itemObj : itemsMapList) {
    					Map<String, Object> itemDescMap = (Map<String, Object>) itemObj;
    					MenuItemDescription desc = new MenuItemDescription();
    					desc.setDataMap(itemDescMap);
    					itemsList.add(desc);
    				}
    			}
				retval.put(key, itemsList);
    		}
    	}
    	return retval;
    }

    
    public FormItemRepresentation decodeItem(String json) throws FormEncodingException {
        JSONValue jsonValue = JSONParser.parseLenient(json);
        if (jsonValue.isObject() != null) {
            JSONObject jsonObj = jsonValue.isObject();
            Map<String, Object> dataMap = asMap(jsonObj);
            return (FormItemRepresentation) decode(dataMap);
        } else {
            throw new FormEncodingException("Expected json object but found " + jsonValue);
        }
    }
    
    public Map<String, InputData> decodeInputs(JSONValue json) throws FormEncodingException {
        Map<String, InputData> retval = new HashMap<String, InputData>();
        if (json != null && json.isObject() != null) {
            for (String key : json.isObject().keySet()) {
                JSONValue value = json.isObject().get(key);
                if (value.isObject() != null) {
                    JSONObject jsonObj = value.isObject();
                    retval.put(key, (InputData) decode(asMap(jsonObj)));
                }
            }
        }
        return retval;
    }
    
    public Map<String, OutputData> decodeOutputs(JSONValue json) throws FormEncodingException {
        Map<String, OutputData> retval = new HashMap<String, OutputData>();
        if (json != null && json.isObject() != null) {
            for (String key : json.isObject().keySet()) {
                JSONValue value = json.isObject().get(key);
                if (value.isObject() != null) {
                    JSONObject jsonObj = value.isObject();
                    retval.put(key, (OutputData) decode(asMap(jsonObj)));
                }
            }
        }
        return retval;
    }

    public List<FBScript> decodeScripts(JSONValue json) throws FormEncodingException {
        List<FBScript> retval = new ArrayList<FBScript>();
        if (json != null && json.isArray() != null) {
            JSONArray array = json.isArray();
            for (int index = 0; index < array.size(); index++) {
                JSONValue elem = array.get(index);
                JSONObject jsonObj = elem.isObject();
                retval.add((FBScript) decode(asMap(jsonObj)));
            }
        }
        return retval;
    }

    public List<FormItemRepresentation> decodeItems(JSONValue json) throws FormEncodingException {
        List<FormItemRepresentation> retval = new ArrayList<FormItemRepresentation>();
        if (json != null && json.isArray() != null) {
            JSONArray array = json.isArray();
            for (int index = 0; index < array.size(); index++) {
                JSONValue elem = array.get(index);
                JSONObject jsonObj = elem.isObject();
                retval.add((FormItemRepresentation) decode(asMap(jsonObj)));
            }
        }
        return retval;
    }
    public List<FBValidation> decodeValidations(JSONValue json) throws FormEncodingException {
        List<FBValidation> retval = new ArrayList<FBValidation>();
        if (json != null && json.isArray() != null) {
            JSONArray array = json.isArray();
            for (int index = 0; index < array.size(); index++) {
                JSONValue elem = array.get(index);
                JSONObject jsonObj = elem.isObject();
                retval.add((FBValidation) decode(asMap(jsonObj)));
            }
        }
        return retval;
    }

    private Map<String, Object> asMap(JSONObject jsonObj) {
        Map<String, Object> retval = new HashMap<String, Object>();
        for (String key : jsonObj.keySet()) {
            JSONValue value = jsonObj.get(key); 
            retval.put(key, fromJsonValue(value));
        }
        return retval;
    }
    
    private List<Object> asList(JSONArray array) {
        List<Object> retval = new ArrayList<Object>();
        if (array != null) {
            for (int index = 0; index < array.size(); index++) {
                retval.add(fromJsonValue(array.get(index)));
            }
        }
        return retval;
    }
    
    private Object fromJsonValue(JSONValue elem) {
        if (elem.isString() != null) {
            return elem.isString().stringValue();
        } else if (elem.isNumber() != null) {
            return elem.isNumber().doubleValue();
        } else if (elem.isArray() != null) {
            return asList(elem.isArray());
        } else if (elem.isNull() != null) {
            return null;
        } else if (elem.isObject() != null) {
            return asMap(elem.isObject());
        } else {
            return "";
        }
    }
    
    
    /*
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
    }*/
}
