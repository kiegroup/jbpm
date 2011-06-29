package org.jbpm.formbuilder.shared.rep.items;

import java.util.Map;

import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;

public class LabelRepresentation extends FormItemRepresentation {

    private String value;
    private String id;
    private String cssName;

    public LabelRepresentation() {
        super("label");
    }
    
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCssName() {
        return cssName;
    }

    public void setCssName(String cssName) {
        this.cssName = cssName;
    }
    
    @Override
    public Map<String, Object> getData() {
    	Map<String, Object> data = super.getData();
    	data.put("value", this.value);
    	data.put("id", this.id);
    	data.put("cssName", this.cssName);
        return data;
    }
    
    @Override
    public void setData(Map<String, Object> data) {
        super.setData(data);
        this.value = (String) data.get("value");
        this.id = (String) data.get("id");
        this.cssName = (String) data.get("cssName");
    }
}
