package org.jbpm.formbuilder.shared.rep.items;

import java.util.Map;

import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;

public class RadioButtonRepresentation extends FormItemRepresentation {

    private String name;
    private String id;
    private String value;
    private Boolean selected = Boolean.FALSE;
       
    public RadioButtonRepresentation() {
        super("radioButton");
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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }
    
    @Override
    public Map<String, Object> getDataMap() {
        Map<String, Object> data = super.getDataMap();
        data.put("name", this.name);
        data.put("id", this.id);
        data.put("value", this.value);
        data.put("selected", this.selected);
        return data;
    }
    
    @Override
    public void setDataMap(Map<String, Object> data) {
        super.setDataMap(data);
        this.name = (String) data.get("name");
        this.id = (String) data.get("id");
        this.value = (String) data.get("value");
        this.selected = (Boolean) data.get("selected");
    }
}
