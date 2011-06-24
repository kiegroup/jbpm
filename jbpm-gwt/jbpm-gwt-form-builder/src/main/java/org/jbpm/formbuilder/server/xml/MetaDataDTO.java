package org.jbpm.formbuilder.server.xml;

import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType public class MetaDataDTO {

    @XmlAttribute private String key;
    @XmlAttribute private String value;
    
    public MetaDataDTO(Map.Entry<String, String> entry) {
        this.key = entry.getKey();
        this.value = entry.getValue();
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
