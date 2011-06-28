package org.jbpm.formbuilder.shared.rep.items;

import java.util.HashMap;
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
    public Map<String, Object> getData() {
        Map<String, Object> data = new HashMap<String, Object>();//TODO super.getData();
        data.put("defaultValue", this.defaultValue);
        data.put("name", this.name);
        data.put("id", this.id);
        data.put("maxLength", this.maxLength);
        return data;
    }
    
    @Override
    public void setData(Map<String, Object> data) {
        // TODO Auto-generated method stub
    }
}
