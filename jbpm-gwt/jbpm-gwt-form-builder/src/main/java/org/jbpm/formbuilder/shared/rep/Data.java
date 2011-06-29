package org.jbpm.formbuilder.shared.rep;

import java.util.HashMap;
import java.util.Map;

public abstract class Data {
	
    private String mimeType; 
    private String name;
    private String value;
    private Formatter formatter;
    
    public String getMimeType() {
        return mimeType;
    }
    
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
    
    public Formatter getFormatter() {
        return formatter;
    }
    
    public void setFormatter(Formatter formatter) {
        this.formatter = formatter;
    }
    
    public String getJsonCode() {
        StringBuilder builder = new StringBuilder("{");
        Map<String, Object> dataMap = getDataMap();
        for (Map.Entry<String, Object> data : dataMap.entrySet()) {
        	builder.append("'").append(data.getKey()).append("': ");
        }
        String mimeType = this.mimeType;
        if (mimeType == null) mimeType = "";
        builder.append("'mimeType': ").append("'" + mimeType + "', ");
        String name = this.name;
        if (name == null) name = "";
        builder.append("'name': ").append("'" + name + "', ");
        String value = this.value;
        if (value == null) value = "";
        builder.append("'value': ").append("'" + value + "', ");
        builder.append("'formatter': ").append(formatter == null ? "null" : formatter.getJsonCode());
        return builder.append("} ").toString();
    }
    
    public Map<String, Object> getDataMap() {
    	Map<String, Object> data = new HashMap<String, Object>();
    	data.put("mimeType", this.mimeType);
    	data.put("name", this.name);
    	data.put("value", this.value);
    	data.put("formatter", this.formatter == null ? null : this.formatter.getDataMap());
        return data;
    }

    @SuppressWarnings("unchecked")
    public void setDataMap(Map<String, Object> dataMap) {
    	this.mimeType = (String) dataMap.get("mimeType");
    	this.name = (String) dataMap.get("name");
    	this.value = (String) dataMap.get("value");
    	this.formatter = (Formatter) JsonUtil.fromMap((Map<String, Object>) dataMap.get("formatter"));
    }
}
