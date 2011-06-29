package org.jbpm.formbuilder.shared.rep.items;

import java.util.Map;

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
    
    @Override
    public Map<String, Object> getData() {
        Map<String, Object> data = super.getData();
        data.put("script", this.script);
        data.put("language", this.language);
        return data;
    }
    
    @Override
    public void setData(Map<String, Object> data) {
        super.setData(data);
        this.script = (String) data.get("script");
        this.language = (String) data.get("language");
    }
}
