package org.jbpm.formbuilder.shared.rep.items;

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
}
