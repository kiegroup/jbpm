package org.jbpm.formbuilder.shared.rep.items;

import java.util.Map;

import org.jbpm.formbuilder.shared.form.FormEncodingException;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;

public class FileInputRepresentation extends FormItemRepresentation {

    private String name;
    private String id;
    private String accept;
    
    public FileInputRepresentation() {
        super("fileInput");
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

    public String getAccept() {
        return accept;
    }

    public void setAccept(String accept) {
        this.accept = accept;
    }
    
    @Override
    public Map<String, Object> getDataMap() {
    	Map<String, Object> data = super.getDataMap();
    	data.put("name", this.name);
    	data.put("id", this.id);
    	data.put("accept", this.accept);
    	return data;
    }
    
    @Override
    public void setDataMap(Map<String, Object> data) throws FormEncodingException {
    	super.setDataMap(data);
    	this.name = (String) data.get("name");
    	this.id = (String) data.get("id");
    	this.accept = (String) data.get("accept");
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) return false;
        if (!(obj instanceof FileInputRepresentation)) return false;
        FileInputRepresentation other = (FileInputRepresentation) obj;
        boolean equals = (this.name == null && other.name == null) || (this.name != null && this.name.equals(other.name));
        if (!equals) return equals;
        equals = (this.id == null && other.id == null) || (this.id != null && this.id.equals(other.id));
        if (!equals) return equals;
        equals = (this.accept == null && other.accept == null) || (this.accept != null && this.accept.equals(other.accept));
        return equals;
    }
    
    @Override
    public int hashCode() {
        int result = super.hashCode();
        int aux = this.name == null ? 0 : this.name.hashCode();
        result = 37 * result + aux;
        aux = this.id == null ? 0 : this.id.hashCode();
        result = 37 * result + aux;
        aux = this.accept == null ? 0 : this.accept.hashCode();
        result = 37 * result + aux;
        return result;
    }
}
