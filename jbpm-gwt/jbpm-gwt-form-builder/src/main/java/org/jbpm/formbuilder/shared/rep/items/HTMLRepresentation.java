package org.jbpm.formbuilder.shared.rep.items;

import java.util.Map;

import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;

public class HTMLRepresentation extends FormItemRepresentation {

    private String content;
    
    public HTMLRepresentation() {
        super("html");
    }
    
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    
    @Override
    public Map<String, Object> getDataMap() {
        Map<String, Object> data = super.getDataMap();
        data.put("content", this.content);
        return data;
    }
    
    @Override
    public void setDataMap(Map<String, Object> data) {
        super.setDataMap(data);
    	this.content = (String) data.get("content");
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) return false;
        if (!(obj instanceof HTMLRepresentation)) return false;
        HTMLRepresentation other = (HTMLRepresentation) obj;
        return (this.content == null && other.content == null) || (this.content != null && this.content.equals(other.content));
    }
    
    @Override
    public int hashCode() {
        int result = super.hashCode();
        int aux = this.content == null ? 0 : this.content.hashCode();
        result = 37 * result + aux;
        return result;
    }
}
