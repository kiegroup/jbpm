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
    public Map<String, Object> getData() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public void setData(Map<String, Object> data) {
        // TODO Auto-generated method stub
        
    }
}
