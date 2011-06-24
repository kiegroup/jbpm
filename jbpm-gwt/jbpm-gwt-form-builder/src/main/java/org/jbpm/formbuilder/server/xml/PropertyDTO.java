package org.jbpm.formbuilder.server.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import org.jbpm.formbuilder.shared.task.TaskPropertyRef;

@XmlType public class PropertyDTO {

    @XmlAttribute private String name;
    @XmlAttribute private String source;
    
    public PropertyDTO(TaskPropertyRef ref) {
        this.name = ref.getName();
        this.source = ref.getSourceExpresion();
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
