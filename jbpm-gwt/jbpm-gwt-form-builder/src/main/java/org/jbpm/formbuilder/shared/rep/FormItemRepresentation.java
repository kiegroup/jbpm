package org.jbpm.formbuilder.shared.rep;

import java.util.ArrayList;
import java.util.HashMap;
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
    	return JsonUtil.getJsonCode(getData());
    }
    
    
    public Map<String, Object> getData() {
    	Map<String, Object> data = new HashMap<String, Object>();
    	
    	data.put("@className", getClass().getName());
    	
        List<Map<String, String>> validationsMap = new ArrayList<Map<String, String>>();
        if (this.itemValidations != null) {
	        for (FBValidation valid : this.itemValidations) {
	        	Map<String, String> map = valid.getPropertiesMap();
	        	validationsMap.add(map);
	        }
        }
        data.put("itemValidations", validationsMap);
        data.put("output", this.output.getDataMap());
        data.put("input", this.input.getDataMap());
        data.put("width", this.width);
        data.put("height", this.height);
        data.put("typeId", this.typeId);
    	return data;
    }
    
    @SuppressWarnings("unchecked")
    public void setData(Map<String, Object> data) {
        List<Map<String, String>> validationsMap = new ArrayList<Map<String, String>>();
        if (this.itemValidations != null) {
	        for (FBValidation valid : this.itemValidations) {
	        	Map<String, String> map = valid.getPropertiesMap();
	        	validationsMap.add(map);
	        }
        }
        data.put("itemValidations", validationsMap);
        this.output.setDataMap((Map<String, Object>) data.get("output"));
        this.input.setDataMap((Map<String, Object>) data.get("input"));
        this.width = (String) data.get("width");
        this.height = (String) data.get("height");
    }
}
