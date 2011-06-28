package org.jbpm.formbuilder.shared.rep;

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
}
