package org.jbpm.formbuilder.shared.rep;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public abstract class FormItemRepresentation {
    
    private List<FBValidation> itemValidations = new ArrayList<FBValidation>();
    private OutputData output;
    private InputData input;
    
    private String width;
    private String height;
    
    private final String typeId;
    
    public FormItemRepresentation(String typeId) {
        this.typeId = typeId;
    }

    public List<FBValidation> getItemValidations() {
        return itemValidations;
    }

    public void setItemValidations(List<FBValidation> itemValidations) {
        this.itemValidations = itemValidations;
    }

    public OutputData getOutput() {
        return output;
    }

    public void setOutput(OutputData output) {
        this.output = output;
    }

    public InputData getInput() {
        return input;
    }

    public void setInput(InputData input) {
        this.input = input;
    }
    
    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getTypeId() {
        return typeId;
    }
    
    public final String getJsonCode() {
        Map<String, Object> data = getData();
        StringBuilder builder = new StringBuilder();
        if (data == null) {
            builder.append("null");
        } else {
            builder.append("{");
            builder.append(jsonFromMap(data));
            builder.append("}");
        }
        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    private String jsonFromMap(Map<String, Object> data) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            builder.append("'").append(entry.getKey()).append("': ");
            Object obj = entry.getValue();
            if (obj == null) {
                builder.append("null");
            } else if (obj instanceof Map) {
                builder.append(jsonFromMap((Map<String, Object>) obj));
            } else if (obj instanceof String) {
                builder.append("'").append(obj).append("'");
            } else if (obj instanceof Date) {
                builder.append("'").append(formatDate((Date) obj)).append("'");
            } else {
                builder.append(obj);
            }
        }
        return builder.toString();
    }
    
    private String formatDate(Date date) {
        return "null"; //TODO see how to manage dates later
    }
    
    
    public abstract Map<String, Object> getData();
    
    public abstract void setData(Map<String, Object> data);
}
