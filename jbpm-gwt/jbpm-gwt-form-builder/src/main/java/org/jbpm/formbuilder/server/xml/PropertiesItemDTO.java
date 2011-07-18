package org.jbpm.formbuilder.server.xml;

import javax.xml.bind.annotation.XmlAttribute;

public class PropertiesItemDTO {

    private String _key;
    private String _value;
    
    public PropertiesItemDTO() {
        // jaxb needs a default constructor
    }
    
    public PropertiesItemDTO(String key, String value) {
        this._key = key;
        this._value = value;
    }

    @XmlAttribute
    public String getKey() {
        return _key;
    }
    
    public void setKey(String key) {
        this._key = key;
    }
    
    @XmlAttribute
    public String getValue() {
        return _value;
    }
    
    public void setValue(String value) {
        this._value = value;
    }
}
