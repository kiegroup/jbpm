package org.jbpm.formbuilder.shared.rep.items;

import java.util.Map;

import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;

public class HiddenRepresentation extends FormItemRepresentation {

    private String id;
    private String name;
    private String value;
    
    public HiddenRepresentation() {
        super("hidden");
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    @Override
    public Map<String, Object> getDataMap() {
    	Map<String, Object> data = super.getDataMap();
    	data.put("id", this.id);
        data.put("name", this.name);
        data.put("value", this.value);
        return data;
    }
    
    @Override
    public void setDataMap(Map<String, Object> data) {
    	super.setDataMap(data);
        this.id = (String) data.get("id");
        this.name = (String) data.get("name");
        this.value = (String) data.get("value");
        
    }
}
