package org.jbpm.formbuilder.shared.rep.items;

import java.util.Map;

import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;

public class OptionRepresentation extends FormItemRepresentation {

    private String label;
    private String value;

    public OptionRepresentation() {
        super("option");
    }
    
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    @Override
    public Map<String, Object> getData() {
        Map<String, Object> data = super.getData();
        data.put("label", this.label);
        data.put("value", this.value);
        return data;
    }
    
    @Override
    public void setData(Map<String, Object> data) {
    	super.setData(data);
    	this.label = (String) data.get("label");
    	this.value = (String) data.get("value");
    }
}
