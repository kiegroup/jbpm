package org.jbpm.formbuilder.server.form;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.jbpm.formbuilder.shared.form.FormEncodingException;
import org.jbpm.formbuilder.shared.form.FormRepresentationDecoder;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;

public class FormRepresentationDecoderImpl implements FormRepresentationDecoder {
    
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    protected FormRepresentationDecoderImpl() {
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
    
    public List<FormItemRepresentation> decodeItems(JSONValue json) throws FormEncodingException {
        List<FormItemRepresentation> retval = new ArrayList<FormItemRepresentation>();
        if (json.isArray() != null) {
            JSONArray array = json.isArray();
            for (int index = 0; index < array.size(); index++) {
                JSONValue elem = array.get(index);
                JSONObject jsonObj = elem.isObject();
                if (jsonObj != null) {
                    String typeId = jsonObj.get("typeId").isString().stringValue();
                    String className = __getItemClassName(typeId);
                    Object obj;
                    try {
                        obj = Class.forName(className).newInstance();
                        if (obj instanceof FormItemRepresentation) {
                            for (Field field : obj.getClass().getFields()) {
                                setValueFromJson(obj, field, jsonObj);
                            }
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
                }
            }
        }
        return retval;
    }

    private void setValueFromJson(Object obj, Field field, JSONObject jsonObj) throws FormEncodingException {
        try {
            Class<?> fieldClass = field.getType();
            if (fieldClass.isAssignableFrom(String.class)) {
                PropertyUtils.setProperty(obj, field.getName(), jsonObj.get(field.getName()).isString().stringValue());
            } else if (fieldClass.isAssignableFrom(Long.class)) {
                double dValue = jsonObj.get(field.getName()).isNumber().doubleValue();
                PropertyUtils.setProperty(obj, field.getName(), Double.valueOf(dValue).longValue());
            } else if (fieldClass.isAssignableFrom(Integer.class)) {
                double dValue = jsonObj.get(field.getName()).isNumber().doubleValue();
                PropertyUtils.setProperty(obj, field.getName(), Double.valueOf(dValue).intValue());
            } else if (fieldClass.isAssignableFrom(Double.class)) {
                double dValue = jsonObj.get(field.getName()).isNumber().doubleValue();
                PropertyUtils.setProperty(obj, field.getName(), dValue);
            } else if (fieldClass.isAssignableFrom(Date.class)) {
                Date date = FORMAT.parse(jsonObj.get(field.getName()).isString().stringValue());
                PropertyUtils.setProperty(obj, field.getName(), date);
            } else if (fieldClass.isAssignableFrom(List.class)) {
                JSONArray fieldArray = jsonObj.get(field.getName()).isArray();
                if (field.getName().equals("formItems")) {
                    for (int jndex = 0; jndex < fieldArray.size(); jndex++) {
                        PropertyUtils.setProperty(obj, field.getName(), decodeItems(fieldArray.get(jndex)));
                    }
                } //TODO implement other base lists
            }
        } catch (InvocationTargetException e) {
            throw new FormEncodingException("Couldn't invoke setter of field " + field.getName(), e);
        } catch (NoSuchMethodException e) {
            throw new FormEncodingException("Couldn't find setter of field " + field.getName(), e);
        } catch (IllegalAccessException e) {
            throw new FormEncodingException("Couldn't access setter of field " + field.getName(), e);
        } catch (ParseException e) {
            throw new FormEncodingException("Couldn't parse date value for field " + field.getName(), e);
        }
    }
    
    private String __getItemClassName(String typeId) {
        return ""; //TODO implement
    }
}
