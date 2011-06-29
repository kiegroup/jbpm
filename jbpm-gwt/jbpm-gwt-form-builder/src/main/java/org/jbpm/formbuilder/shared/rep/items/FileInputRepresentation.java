package org.jbpm.formbuilder.shared.rep.items;

import java.util.Map;

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
    public Map<String, Object> getData() {
    	Map<String, Object> data = super.getData();
    	data.put("name", this.name);
    	data.put("id", this.id);
    	data.put("accept", this.accept);
    	return data;
    }
    
    @Override
    public void setData(Map<String, Object> data) {
    	super.setData(data);
    	this.name = (String) data.get("name");
    	this.id = (String) data.get("id");
    	this.accept = (String) data.get("accept");
    }
}
