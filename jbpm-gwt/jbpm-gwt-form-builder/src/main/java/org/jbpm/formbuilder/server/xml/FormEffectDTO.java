package org.jbpm.formbuilder.server.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import org.jbpm.formbuilder.client.effect.FBFormEffect;

@XmlType (name="effect") public class FormEffectDTO {

    @XmlAttribute private String className;
    
    public FormEffectDTO(FBFormEffect effect) {
        this.className = effect.getClass().getName();
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
