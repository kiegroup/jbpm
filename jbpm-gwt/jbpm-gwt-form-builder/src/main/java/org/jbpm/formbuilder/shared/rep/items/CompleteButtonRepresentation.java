package org.jbpm.formbuilder.shared.rep.items;

import java.util.Map;

import org.jbpm.formbuilder.client.form.FormEncodingClientFactory;
import org.jbpm.formbuilder.shared.form.FormEncodingException;
import org.jbpm.formbuilder.shared.form.FormRepresentationDecoder;
import org.jbpm.formbuilder.shared.rep.FBScript;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;

public class CompleteButtonRepresentation extends FormItemRepresentation {

    private String text;
    private String name;
    private String id;
    private FBScript onClickScript;
    
    public CompleteButtonRepresentation() {
        super("completeButton");
        this.onClickScript = new FBScript();
        this.onClickScript.setType("text/javascript");
        this.onClickScript.setContent("document.forms[0].submit();");
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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

    public FBScript getOnClickScript() {
        return onClickScript;
    }

    public void setOnClickScript(FBScript onClickScript) {
        this.onClickScript = onClickScript;
    }
    
    @Override
    public Map<String, Object> getDataMap() {
        Map<String, Object> data = super.getDataMap();
        data.put("text", this.text);
        data.put("name", this.name);
        data.put("id", this.id);
        data.put("onClickScript", this.onClickScript == null ? null : this.onClickScript.getDataMap());
        return data;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void setDataMap(Map<String, Object> data) {
        super.setDataMap(data);
        this.text = (String) data.get("text");
        this.name = (String) data.get("name");
        this.id = (String) data.get("id");
        FormRepresentationDecoder decoder = FormEncodingClientFactory.getDecoder();
        try {
            this.onClickScript = (FBScript) decoder.decode((Map<String, Object>) data.get("onClickScript"));
        } catch (FormEncodingException e) {
            this.onClickScript = new FBScript();
            this.onClickScript.setType("text/javascript");
            this.onClickScript.setContent("document.forms[0].submit();");
            //TODO see how to manage this error 
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) return false;
        if (!(obj instanceof CompleteButtonRepresentation)) return false;
        CompleteButtonRepresentation other = (CompleteButtonRepresentation) obj;
        boolean equals = (this.text == null && other.text == null) || (this.text != null && this.text.equals(other.text));
        if (!equals) return equals;
        equals = (this.name == null && other.name == null) || (this.name != null && this.name.equals(other.name));
        if (!equals) return equals;
        equals = (this.id == null && other.id == null) || (this.id != null && this.id.equals(other.id));
        if (!equals) return equals;
        equals = (this.onClickScript == null && other.onClickScript == null) || 
            (this.onClickScript != null && this.onClickScript.equals(other.onClickScript));
        return equals;
    }
    
    @Override
    public int hashCode() {
        int result = super.hashCode();
        int aux = this.text == null ? 0 : this.text.hashCode();
        result = 37 * result + aux;
        aux = this.name == null ? 0 : this.name.hashCode();
        result = 37 * result + aux;
        aux = this.id == null ? 0 : this.id.hashCode();
        result = 37 * result + aux;
        aux = this.onClickScript == null ? 0 : this.onClickScript.hashCode();
        result = 37 * result + aux;
        return result;
    }
}
