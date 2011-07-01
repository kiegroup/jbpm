package org.jbpm.formbuilder.server.form;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.jbpm.formbuilder.server.xml.FormEffectDTO;

@XmlRootElement (name = "menuItem") public class SaveMenuItemDTO {

    private String _groupName;
    private String _name;
    private String _clone;
    private List<FormEffectDTO> _effect = new ArrayList<FormEffectDTO>();
    
    public SaveMenuItemDTO() {
        // jaxb needs a default constructor
    }
    
    @XmlElement
    public String getGroupName() {
        return _groupName;
    }
    
    public void setGroupName(String groupName) {
        this._groupName = groupName;
    }
    
    @XmlElement
    public String getName() {
        return _name;
    }
    
    public void setName(String name) {
        this._name = name;
    }
    
    @XmlElement
    public String getClone() {
        return _clone;
    }
    
    public void setClone(String clone) {
        this._clone = clone;
    }

    @XmlElement
    public List<FormEffectDTO> getEffect() {
        return _effect;
    }

    public void setEffect(List<FormEffectDTO> effect) {
        this._effect = effect;
    }
    
    
}
