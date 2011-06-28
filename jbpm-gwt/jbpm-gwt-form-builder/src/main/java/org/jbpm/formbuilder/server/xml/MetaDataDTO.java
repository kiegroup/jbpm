package org.jbpm.formbuilder.server.xml;

import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;

public class MetaDataDTO {

    private String _key;
    private String _value;
    
    public MetaDataDTO() {
        // jaxb needs a default constructor
    }
    
    public MetaDataDTO(Map.Entry<String, String> entry) {
        this._key = entry.getKey();
        this._value = entry.getValue();
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
