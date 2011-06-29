package org.jbpm.formbuilder.shared.rep.items;

import java.util.Map;

import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;

public class PasswordFieldRepresentation extends FormItemRepresentation {
    
    private String defaultValue;
    private String name;
    private String id;
    private Integer maxLength;
    
    public PasswordFieldRepresentation() {
        super("passwordField");
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }
    
    @Override
    public Map<String, Object> getData() {
    	Map<String, Object> data = super.getData();
    	data.put("defaultValue", this.defaultValue);
    	data.put("name", this.name);
        data.put("id", this.id);
        data.put("maxLength", this.maxLength);
        return data;
    }
    
    @Override
    public void setData(Map<String, Object> data) {
    	super.setData(data);
    	this.defaultValue = (String) data.get("defaultValue");
    	this.name = (String) data.get("name");
    	this.id = (String) data.get("id");
    	Object obj = data.get("maxLength");
    	if (obj != null) {
    		this.maxLength = ((Number) obj).intValue();
    	}
    }
}
