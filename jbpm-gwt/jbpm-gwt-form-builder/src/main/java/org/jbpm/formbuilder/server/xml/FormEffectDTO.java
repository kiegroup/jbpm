package org.jbpm.formbuilder.server.xml;

import javax.xml.bind.annotation.XmlAttribute;

import org.jbpm.formbuilder.shared.menu.FormEffectDescription;

public class FormEffectDTO {

    private String _className;

    public FormEffectDTO() {
        // jaxb needs a default constructor
    }
    
    public FormEffectDTO(FormEffectDescription effect) {
        this._className = effect.getClassName();
    }

    @XmlAttribute 
    public String getClassName() {
        return _className;
    }

    public void setClassName(String className) {
        this._className = className;
    }
}
