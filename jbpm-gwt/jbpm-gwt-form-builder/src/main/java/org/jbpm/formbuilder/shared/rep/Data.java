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
}
