package org.jbpm.formbuilder.server.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.jbpm.formbuilder.shared.menu.MenuOptionDescription;

@XmlRootElement (name = "menuOptions") public class ListOptionsDTO {

    private List<MenuOptionDTO> _menuOption = new ArrayList<MenuOptionDTO>();
    
    public ListOptionsDTO() {
        // jaxb needs a default constructor
    }
    
    public ListOptionsDTO(List<MenuOptionDescription> options) {
        for (MenuOptionDescription option : options) {
            _menuOption.add(new MenuOptionDTO(option));
        }
    }

    @XmlElement 
    public List<MenuOptionDTO> getMenuOption() {
        return _menuOption;
    }

    public void setMenuOption(List<MenuOptionDTO> menuOption) {
        this._menuOption = menuOption;
    }
}
