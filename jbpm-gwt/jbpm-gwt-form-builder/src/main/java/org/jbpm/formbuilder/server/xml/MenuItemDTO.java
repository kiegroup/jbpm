package org.jbpm.formbuilder.server.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.jbpm.formbuilder.server.form.FormEncodingServerFactory;
import org.jbpm.formbuilder.shared.form.FormEncodingException;
import org.jbpm.formbuilder.shared.menu.FormEffectDescription;
import org.jbpm.formbuilder.shared.menu.MenuItemDescription;

public class MenuItemDTO {

    private String _className;
    private String _optionName;
    private String _itemJson;
    private List<FormEffectDTO> _effect = new ArrayList<FormEffectDTO>();

    public MenuItemDTO() {
        // jaxb needs a default constructor
    }
    
    public MenuItemDTO(MenuItemDescription item) {
        this._className = item.getClassName();
        for (FormEffectDescription eff : item.getEffects()) {
            _effect.add(new FormEffectDTO(eff));
        }
        try {
            String json = FormEncodingServerFactory.getEncoder().encode(item.getItemRepresentation());
            this._itemJson = json;
        } catch (FormEncodingException e) {
            
        }
    }

    @XmlElement 
    public String getItemJson() {
        return _itemJson;
    }

    public void setItemJson(String itemJson) {
        this._itemJson = itemJson;
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

    @XmlElement
    public String getOptionName() {
        return _optionName;
    }
    
    public void setOptionName(String optionName) {
        this._optionName = optionName;
    }
}
