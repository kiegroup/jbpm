package org.jbpm.formbuilder.shared.rep.items;

import java.util.Map;

import org.jbpm.formbuilder.shared.form.FormEncodingException;
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
    public Map<String, Object> getDataMap() {
        Map<String, Object> data = super.getDataMap();
        data.put("script", this.script);
        data.put("language", this.language);
        return data;
    }
    
    @Override
    public void setDataMap(Map<String, Object> data) throws FormEncodingException {
        super.setDataMap(data);
        this.script = (String) data.get("script");
        this.language = (String) data.get("language");
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) return false;
        if (!(obj instanceof ServerTransformationRepresentation)) return false;
        ServerTransformationRepresentation other = (ServerTransformationRepresentation) obj;
        boolean equals = (this.script == null && other.script == null) || (this.script != null && this.script.equals(other.script));
        if (!equals) return equals;
        equals = (this.language == null && other.language == null) || (this.language != null && this.language.equals(other.language));
        return equals;
    }
    
    @Override
    public int hashCode() {
        int result = super.hashCode();
        int aux = this.script == null ? 0 : this.script.hashCode();
        result = 37 * result + aux;
        aux = this.language == null ? 0 : this.language.hashCode();
        result = 37 * result + aux;
        return result;
    }
}
