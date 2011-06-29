package org.jbpm.formbuilder.shared.rep.items;

import java.util.Map;

import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;

public class TextAreaRepresentation extends FormItemRepresentation {

    private String name;
    private int rows;
    private int cols;
    private String value;
    private String id;

    public TextAreaRepresentation() {
        super("textArea");
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getCols() {
        return cols;
    }

    public void setCols(int cols) {
        this.cols = cols;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    @Override
    public Map<String, Object> getDataMap() {
        Map<String, Object> data = super.getDataMap();
        data.put("name", this.name);
        data.put("rows", this.rows);
        data.put("cols", this.cols);
        data.put("value", this.value);
        data.put("id", this.id);
        return data;
    }
    
    @Override
    public void setDataMap(Map<String, Object> data) {
    	super.setDataMap(data);
    	this.name = (String) data.get("name");
    	this.rows = (data.get("rows") == null ? 0 : ((Number) data.get("rows")).intValue());
    	this.cols = (data.get("cols") == null ? 0 : ((Number) data.get("cols")).intValue());
    	this.value = (String) data.get("value");
    	this.id = (String) data.get("id");
    }
}
