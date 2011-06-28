package org.jbpm.formbuilder.server.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.jbpm.formbuilder.shared.menu.FormEffectDescription;
import org.jbpm.formbuilder.shared.menu.MenuItemDescription;

public class MenuItemDTO {

    private String _className;
    private List<FormEffectDTO> _effect = new ArrayList<FormEffectDTO>();

    public MenuItemDTO() {
        // jaxb needs a default constructor
    }
    
    public MenuItemDTO(MenuItemDescription item) {
        this._className = item.getClassName();
        for (FormEffectDescription eff : item.getEffects()) {
            _effect.add(new FormEffectDTO(eff));
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
    public List<FormEffectDTO> getEffect() {
        return _effect;
    }

    public void setEffect(List<FormEffectDTO> effect) {
        this._effect = effect;
    }
}
