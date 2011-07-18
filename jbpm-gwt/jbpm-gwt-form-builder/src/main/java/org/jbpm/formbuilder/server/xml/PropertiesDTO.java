package org.jbpm.formbuilder.server.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import com.sun.xml.txw2.annotation.XmlElement;

@XmlRootElement (name ="properties") public class PropertiesDTO {

    private List<PropertiesItemDTO> _property = new ArrayList<PropertiesItemDTO>();

    public PropertiesDTO() {
        // jaxb needs a default constructor
    }
    
    public PropertiesDTO(Map<String, String> props) {
        for (Map.Entry<String, String> entry : props.entrySet()) {
            _property.add(new PropertiesItemDTO(entry.getKey(), entry.getValue()));
        }
    }

    @XmlElement
    public List<PropertiesItemDTO> getProperty() {
        return _property;
    }

    public void setProperty(List<PropertiesItemDTO> property) {
        this._property = property;
    }
}
