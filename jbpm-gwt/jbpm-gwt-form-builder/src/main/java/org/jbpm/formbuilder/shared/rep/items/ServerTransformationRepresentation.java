package org.jbpm.formbuilder.shared.rep.items;

import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;

public class ServerTransformationRepresentation extends FormItemRepresentation {

    private String language;
    private String script;
    
    public ServerTransformationRepresentation() {
        super("serverTransformation");
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }
}
