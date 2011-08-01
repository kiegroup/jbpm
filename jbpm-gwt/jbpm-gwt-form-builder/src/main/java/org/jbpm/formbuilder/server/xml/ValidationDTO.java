package org.jbpm.formbuilder.server.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.jbpm.formbuilder.shared.menu.ValidationDescription;

public class ValidationDTO {

    private String _className;
    private List<PropertiesItemDTO> _property;

    public ValidationDTO() {
        // jaxb needs a default constructor
    }
    
    public ValidationDTO(ValidationDescription desc) {
        this._className = desc.getClassName();
        if (desc.getProperties() != null) {
            for (Map.Entry<String, String> entry : desc.getProperties().entrySet()) {
                getProperty().add(new PropertiesItemDTO(entry.getKey(), entry.getValue()));
            }
        }
    }
    
    @XmlAttribute
    public String getClassName() {
        return _className;
    }

    public void setClassName(String className) {
        this._className = className;
    }

    @XmlElement
    public List<PropertiesItemDTO> getProperty() {
        if (_property == null) {
            _property = new ArrayList<PropertiesItemDTO>();
        }
        return _property;
    }

    public void setProperty(List<PropertiesItemDTO> property) {
        this._property = property;
    }
}
