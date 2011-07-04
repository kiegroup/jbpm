package org.jbpm.formbuilder.client.form;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.shared.form.FormEncodingException;
import org.jbpm.formbuilder.shared.form.FormRepresentationEncoder;
import org.jbpm.formbuilder.shared.menu.MenuItemDescription;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;
import org.jbpm.formbuilder.shared.rep.InputData;
import org.jbpm.formbuilder.shared.rep.Mappable;
import org.jbpm.formbuilder.shared.rep.OutputData;

import com.google.gwt.user.client.rpc.impl.ReflectionHelper;

public class FormRepresentationEncoderClient implements FormRepresentationEncoder {

    public String encode(FormRepresentation form) throws FormEncodingException {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        builder.append("  \"name\": \"").append(form.getName()).append("\",\n");
        builder.append("  \"action\": \"").append(form.getAction()).append("\",\n");
        builder.append("  \"taskId\": \"").append(form.getTaskId()).append("\",\n");
        builder.append("  \"documentation\": \"").append(form.getDocumentation()).append("\",\n");
        builder.append("  \"enctype\": \"").append(form.getEnctype()).append("\",\n");
        builder.append("  \"lastModified\": \"").append(form.getLastModified()).append("\",\n");
        builder.append("  \"method\": \"").append(form.getMethod()).append("\",\n");
        builder.append("  \"formItems\": ").append(encodeList(form.getFormItems())).append(",\n");
        builder.append("  \"formValidations\": ").append(encodeList(form.getFormValidations())).append(",\n");
        builder.append("  \"inputs\": ").append(encodeInputs(form.getInputs())).append(",\n");
        builder.append("  \"outputs\": ").append(encodeOutputs(form.getOutputs())).append(",\n");
        builder.append("  \"onLoadScripts\": ").append(encodeList(form.getOnLoadScripts())).append(",\n");
        builder.append("  \"onSubmitScripts\": ").append(encodeList(form.getOnSubmitScripts())).append("\n");
        builder.append("}\n");
        return builder.toString();
    }
    
    public String encodeMenuItemsMap(Map<String, List<MenuItemDescription>> items) throws FormEncodingException {
        Map<String, Object> dataMap = new HashMap<String, Object>();
        if (items == null) {
            return "null";
        }
        for (Map.Entry<String, List<MenuItemDescription>> entry : items.entrySet()) {
            List<Map<String, Object>> itemMap = new ArrayList<Map<String, Object>>();
            for (MenuItemDescription desc : entry.getValue()) {
                itemMap.add(desc.getDataMap());
            }
            dataMap.put(entry.getKey(), itemMap);
        }
        return jsonFromMap(dataMap);
    }
    
    public String encode(FormItemRepresentation item) throws FormEncodingException {
        if (item == null) { 
            return "null";
        }
        Map<String, Object> dataMap = new HashMap<String, Object>();
        return jsonFromMap(dataMap);
    }
    
    public String encodeList(List<? extends Mappable> list) {
        StringBuilder builder = new StringBuilder();
        if (list == null) {
            builder.append("null");
        } else {
            builder.append("[");
            Iterator<? extends Mappable> iter = list.iterator();
            while(iter.hasNext()) {
                Mappable mappable = iter.next();
                builder.append(toJson(mappable));
                if (iter.hasNext()) {
                    builder.append(", \n");
                }
            }
            builder.append("]");
        }
        return builder.toString();
    }

    public String encodeInputs(Map<String, InputData> inputs) {
        StringBuilder builder = new StringBuilder();
        if (inputs == null) {
            builder.append("null");
        } else {
            builder.append("{");
            Iterator<Map.Entry<String, InputData>> iter = inputs.entrySet().iterator();
            while(iter.hasNext()) {
                Map.Entry<String, InputData> input = iter.next();
                builder.append("\"").append(input.getKey()).append("\": ");
                builder.append(asJsonValue(input.getValue()));
                if (iter.hasNext()) {
                    builder.append(", \n");
                }
            }
            builder.append("}");
        }
        return builder.toString();
    }
    
    public String encodeOutputs(Map<String, OutputData> outputs) {
        StringBuilder builder = new StringBuilder();
        if (outputs == null) {
            builder.append("null");
        } else {
            builder.append("{");
            Iterator<Map.Entry<String, OutputData>> iter = outputs.entrySet().iterator();
            while(iter.hasNext()) {
                Map.Entry<String, OutputData> output = iter.next();
                builder.append("\"").append(output.getKey()).append("\": ");
                builder.append(asJsonValue(output.getValue()));
                if (iter.hasNext()) {
                    builder.append(", \n");
                }
            }
            builder.append("}");
        }
        return builder.toString();
    }
    
    private String toJson(Mappable obj) {
        StringBuilder builder = new StringBuilder();
        if (obj == null) {
            builder.append("null");
        } else {
            Map<String, Object> data = obj.getDataMap();
            if (data != null) {
                builder.append(jsonFromMap(data));
            }
        }
        return builder.toString();
    }

    private String jsonFromMap(Map<String, Object> data) {
        StringBuilder builder = new StringBuilder();
        if (data == null) {
            builder.append("null");
        } else {
            builder.append("{\n");
            Iterator<Map.Entry<String, Object>> iter = data.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, Object> entry = iter.next();
                builder.append("\"").append(entry.getKey()).append("\": ").append(asJsonValue(entry.getValue()));
                if (iter.hasNext()) {
                    builder.append(",");
                }
                builder.append("\n");
            }
            builder.append("}\n");
        }
        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    private String asJsonValue(Object obj) {
        StringBuilder builder = new StringBuilder();
        if (obj == null) {
            builder.append("null");
        } else if (obj instanceof Mappable) {
            builder.append(jsonFromMap(((Mappable) obj).getDataMap()));
        } else if (obj instanceof Map) {
            builder.append(jsonFromMap((Map<String, Object>) obj));
        } else if (obj instanceof List) {
            builder.append(jsonFromList((List<?>) obj));
        } else {
            builder.append(jsonFromValue(obj));
        }
        return builder.toString();
    }
    
    private String jsonFromValue(Object obj) {
        StringBuilder builder = new StringBuilder();
        if (obj instanceof String) {
            builder.append("\"").append(obj).append("\"");
        } else if (obj instanceof Date) {
            builder.append("\"").append(formatDate((Date) obj)).append("\"");
        } else {
            builder.append(obj);
        }
        return builder.toString();
    }
    
    private String jsonFromList(List<?> list) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        if (list != null) {
            for (Iterator<?> iter = list.iterator(); iter.hasNext(); ) {
                Object obj = iter.next();
                builder.append(asJsonValue(obj));
                if (iter.hasNext()) {
                    builder.append(",");
                }
                builder.append("\n");
            }
            builder.append("]\n");
        }
        return builder.toString();
    }
    
    public Object fromMap(Map<String, Object> map) {
        Object objClassName = map.get("@className");
        if (objClassName == null) {
            return null;
        }
        String className = (String) objClassName;
        try {
            Class<?> klass = ReflectionHelper.loadClass(className);
            return ReflectionHelper.newInstance(klass);
        } catch (Exception e) {
            return null;
        }
    }
    
    private String formatDate(Date date) {
        return "null"; //TODO see how to manage dates later
    }
}
