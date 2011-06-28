package org.jbpm.formbuilder.server.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.jbpm.formbuilder.shared.menu.MenuItemDescription;

public class MenuGroupDTO {

    private List<MenuItemDTO> _menuItem = new ArrayList<MenuItemDTO>();
    private String _name;
    
    public MenuGroupDTO() {
        // jaxb needs default constructors
    }
    
    public MenuGroupDTO(String name, List<MenuItemDescription> items) {
        this._name = name;
        for (MenuItemDescription item : items) {
            _menuItem.add(new MenuItemDTO(item));
        }
    }

    @XmlElement
    public List<MenuItemDTO> getMenuItem() {
        return _menuItem;
    }

    public void setMenuItem(List<MenuItemDTO> menuItem) {
        this._menuItem = menuItem;
    }

    @XmlAttribute 
    public String getName() {
        return _name;
    }

    public void setName(String name) {
        this._name = name;
    }
}
