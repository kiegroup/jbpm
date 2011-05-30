package org.jbpm.formbuilder.shared.rep.items;

import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;

public class PasswordFieldRepresentation extends FormItemRepresentation {
    
    private String defaultValue;
    private String name;
    private String id;
    private Integer maxLength;
    
    public PasswordFieldRepresentation() {
        super();
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
}
