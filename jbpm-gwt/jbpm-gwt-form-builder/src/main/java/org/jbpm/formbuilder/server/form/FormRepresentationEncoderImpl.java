package org.jbpm.formbuilder.server.form;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.jbpm.formbuilder.shared.form.FormEncodingException;
import org.jbpm.formbuilder.shared.form.FormRepresentationEncoder;
import org.jbpm.formbuilder.shared.rep.FBScript;
import org.jbpm.formbuilder.shared.rep.FBValidation;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;
import org.jbpm.formbuilder.shared.rep.InputData;
import org.jbpm.formbuilder.shared.rep.OutputData;

public class FormRepresentationEncoderImpl implements FormRepresentationEncoder {

    protected FormRepresentationEncoderImpl() {
    }
    
    public String encode(FormRepresentation rep) throws FormEncodingException {
        StringBuilder builder = new StringBuilder();
        builder.append("form {\n");
        builder.append("  \"name\": \"").append(rep.getName()).append("\",\n");
        builder.append("  \"action\": \"").append(rep.getAction()).append("\",\n");
        builder.append("  \"taskId\": \"").append(rep.getTaskId()).append("\",\n");
        builder.append("  \"documentation\": \"").append(rep.getDocumentation()).append("\",\n");
        builder.append("  \"enctype\": \"").append(rep.getEnctype()).append("\",\n");
        builder.append("  \"lastModified\": \"").append(rep.getLastModified()).append("\",\n");
        builder.append("  \"method\": \"").append(rep.getMethod()).append("\",\n");
        builder.append("  \"formItems\": ").append(encodeItems(rep.getFormItems())).append(",\n");
        builder.append("  \"formValidations\": ").append(encodeValidations(rep.getFormValidations())).append(",\n");
        builder.append("  \"inputs\": ").append(encodeInputs(rep.getInputs())).append(",\n");
        builder.append("  \"outputs\": ").append(encodeOutputs(rep.getOutputs())).append(",\n");
        builder.append("  \"onLoadScripts\": ").append(encodeScripts(rep.getOnLoadScripts())).append(",\n");
        builder.append("  \"onSubmitScripts\": ").append(encodeScripts(rep.getOnSubmitScripts())).append(",\n");
        builder.append("}\n");
        return builder.toString();
    }
    
    public String encodeScripts(List<FBScript> scripts) throws FormEncodingException {
        StringBuilder builder = new StringBuilder();
        if (scripts != null) {
            for (FBScript script : scripts) {
                //TODO implement
            }
        }
        return builder.toString();
    }
    
    public String encodeInputs(Map<String, InputData> inputs) throws FormEncodingException {
        StringBuilder builder = new StringBuilder();
        if (inputs != null) {
            for (Map.Entry<String, InputData> input : inputs.entrySet()) {
                //TODO implement
            }
        }
        return builder.toString();
    }

    public String encodeOutputs(Map<String, OutputData> outputs) throws FormEncodingException {
        StringBuilder builder = new StringBuilder();
        if (outputs != null) {
            for (Map.Entry<String, OutputData> output : outputs.entrySet()) {
                //TODO implement
            }
        }
        return builder.toString();
    }

    public String encodeValidations(List<FBValidation> formValidations) throws FormEncodingException {
        StringBuilder builder = new StringBuilder(); 
        if (formValidations != null) {
            for (FBValidation validation : formValidations) {
                //TODO implement
            }
        }
        return builder.toString();
    }

    public String encodeItems(List<FormItemRepresentation> formItems) throws FormEncodingException {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        if (formItems != null) {
            for (Iterator<FormItemRepresentation> iter = formItems.iterator(); iter.hasNext(); ) {
                FormItemRepresentation item = iter.next();
                builder.append(item.getTypeId());
                Field[] fields = item.getClass().getFields();
                for (int i = 0; i < fields.length; i++) {
                    Field field = fields[i];
                    builder.append("\"").append(field.getName()).append("\": ");
                    if (field.getType().isAssignableFrom(List.class)) {
                        try {
                            List<?> propValue = (List<?>) PropertyUtils.getProperty(item, field.getName());
                            if (!propValue.isEmpty()) {
                                Object obj = propValue.iterator().next();
                                if (obj.getClass().isAssignableFrom(FormItemRepresentation.class)) {
                                    List<FormItemRepresentation> subItems = (List<FormItemRepresentation>) propValue;
                                    builder.append(encodeItems(subItems));
                                }
                            } else {
                                builder.append("[]");
                            }
                        } catch (IllegalAccessException e) {
                            throw new FormEncodingException("All item properties should be exposed", e);
                        } catch (InvocationTargetException e) {
                            throw new FormEncodingException("All item properties should be exposed", e);
                        } catch (NoSuchMethodException e) {
                            throw new FormEncodingException("All item properties should be exposed", e);
                        }
                    } else {
                        try {
                            builder.append(extractValue(item, field.getName()));
                        } catch (IllegalAccessException e) {
                            throw new FormEncodingException("All item properties should be exposed", e);
                        } catch (InvocationTargetException e) {
                            throw new FormEncodingException("All item properties should be exposed", e);
                        } catch (NoSuchMethodException e) {
                            throw new FormEncodingException("All item properties should be exposed", e);
                        }
                    }
                    if (i < fields.length) {
                        builder.append(",");
                    }
                    builder.append("\n");
                }
                if (iter.hasNext()) {
                    builder.append(",");
                }
                builder.append("\n");
            }
            builder.append("]\n");
        }
        return builder.substring(1).toString();
    }

    private String extractValue(FormItemRepresentation item, String fieldName)
            throws IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {
        StringBuilder builder = new StringBuilder();
        Object propValue = PropertyUtils.getProperty(item, fieldName);
        if (propValue.getClass().isAssignableFrom(String.class)) {
            builder.append("\"").append(propValue).append("\"");
        } else {
            builder.append(propValue);
        }
        return builder.toString();
    }
}
