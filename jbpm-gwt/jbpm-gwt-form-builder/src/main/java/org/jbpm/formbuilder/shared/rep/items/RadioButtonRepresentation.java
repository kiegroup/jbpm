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
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public void setDataMap(Map<String, Object> data) {
        // TODO Auto-generated method stub
        
    }
}
