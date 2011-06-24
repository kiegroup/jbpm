package org.jbpm.formbuilder.server.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;

import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.menu.FBMenuItem;

@XmlType (name="menuItem") public class MenuItemDTO {

    @XmlAttribute private String className;
    @XmlElement @XmlList private List<FormEffectDTO> effect = new ArrayList<FormEffectDTO>();
    
    public MenuItemDTO(FBMenuItem item) {
        this.className = item.getClass().getName();
        for (FBFormEffect eff : item.getFormEffects()) {
            effect.add(new FormEffectDTO(eff));
        }
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<FormEffectDTO> getEffect() {
        return effect;
    }

    public void setEffect(List<FormEffectDTO> effect) {
        this.effect = effect;
    }
}
