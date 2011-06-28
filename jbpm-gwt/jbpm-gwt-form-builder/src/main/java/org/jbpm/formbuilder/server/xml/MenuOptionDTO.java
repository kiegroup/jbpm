package org.jbpm.formbuilder.server.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.jbpm.formbuilder.shared.menu.MenuOptionDescription;

public class MenuOptionDTO {
    
    private String _name;
    private String _commandClass;
    private List<MenuOptionDTO> _menuOption = new ArrayList<MenuOptionDTO>();

    public MenuOptionDTO() {
        // jaxb needs a default constructor
    }
    
    public MenuOptionDTO(MenuOptionDescription option) {
        this._name = option.getHtml();
        if (option.getCommandClass() != null) {
            this._commandClass = option.getCommandClass();
        }
        if (option.getSubMenu() != null) {
            for (MenuOptionDescription opt : option.getSubMenu()) {
                _menuOption.add(new MenuOptionDTO(opt));
            }
        }
    }

    @XmlAttribute
    public String getName() {
        return _name;
    }

    public void setName(String name) {
        this._name = name;
    }

    @XmlAttribute
    public String getCommandClass() {
        return _commandClass;
    }

    public void setCommandClass(String commandClass) {
        this._commandClass = commandClass;
    }

    @XmlElement 
    public List<MenuOptionDTO> getMenuOption() {
        return _menuOption;
    }

    public void setMenuOption(List<MenuOptionDTO> menuOption) {
        this._menuOption = menuOption;
    }
}
