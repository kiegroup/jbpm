package org.jbpm.formbuilder.server.xml;

import javax.xml.bind.annotation.XmlAttribute;

public class FormPreviewParameterDTO {

    private String _key;
    private String _value;

    public FormPreviewParameterDTO() {
        // jaxb needs a default constructor
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
