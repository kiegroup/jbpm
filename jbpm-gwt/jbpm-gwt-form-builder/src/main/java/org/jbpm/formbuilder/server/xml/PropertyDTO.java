package org.jbpm.formbuilder.server.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import org.jbpm.formbuilder.shared.task.TaskPropertyRef;

@XmlType public class PropertyDTO {

    private String _name;
    private String _source;
    
    public PropertyDTO() {
        // jaxb needs a default constructor
    }
    
    public PropertyDTO(TaskPropertyRef ref) {
        this._name = ref.getName();
        this._source = ref.getSourceExpresion();
    }
    
    @XmlAttribute 
    public String getName() {
        return _name;
    }
    
    public void setName(String name) {
        this._name = name;
    }
    
    @XmlAttribute 
    public String getSource() {
        return _source;
    }

    public void setSource(String source) {
        this._source = source;
    }
}
