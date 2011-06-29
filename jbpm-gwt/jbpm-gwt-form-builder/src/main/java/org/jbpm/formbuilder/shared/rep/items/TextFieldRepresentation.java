package org.jbpm.formbuilder.shared.rep.items;

import java.util.Map;

import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;

public class TextFieldRepresentation extends FormItemRepresentation {

    private String defaultValue;
    private String name;
    private String id;
    private Integer maxLength;
    
    public TextFieldRepresentation() {
        super("textField");
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    @Override
    public Map<String, Object> getDataMap() {
        Map<String, Object> data = super.getDataMap();
        data.put("defaultValue", this.defaultValue);
        data.put("name", this.name);
        data.put("id", this.id);
        data.put("maxLength", this.maxLength);
        return data;
    }
    
    @Override
    public void setDataMap(Map<String, Object> data) {
    	super.setDataMap(data);
    	this.defaultValue = (String) data.get("defaultValue");
    	this.name = (String) data.get("name");
    	this.id = (String) data.get("id");
    	Object obj = data.get("maxLength");
    	if (obj != null) {
    		this.maxLength = ((Number) obj).intValue();
    	}
    }
}
