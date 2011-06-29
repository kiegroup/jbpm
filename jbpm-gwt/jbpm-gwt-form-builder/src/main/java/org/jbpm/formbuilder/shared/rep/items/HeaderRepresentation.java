package org.jbpm.formbuilder.shared.rep.items;

import java.util.Map;

import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;

public class HeaderRepresentation extends FormItemRepresentation {

    private String value;
    private String styleClass;
    private String cssId;
    private String cssName;

    public HeaderRepresentation() {
        super("header");
    }
    
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getStyleClass() {
        return styleClass;
    }

    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }

    public String getCssId() {
        return cssId;
    }

    public void setCssId(String cssId) {
        this.cssId = cssId;
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
        data.put("styleClass", this.styleClass);
        data.put("cssId", this.cssId);
        data.put("cssName", this.cssName);
        return data;
    }
    
    @Override
    public void setData(Map<String, Object> data) {
        super.setData(data);
        this.value = (String) data.get("value");
        this.styleClass = (String) data.get("styleClass");
        this.cssId = (String) data.get("cssId");
        this.cssName = (String) data.get("cssName");
    }
}
