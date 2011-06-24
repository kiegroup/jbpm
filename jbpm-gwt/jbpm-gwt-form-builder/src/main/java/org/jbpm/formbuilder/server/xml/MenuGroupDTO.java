package org.jbpm.formbuilder.server.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;

import org.jbpm.formbuilder.client.menu.FBMenuItem;

@XmlType public class MenuGroupDTO {

    @XmlElement @XmlList private List<MenuItemDTO> menuItem = new ArrayList<MenuItemDTO>();
    @XmlAttribute private String name;
    
    public MenuGroupDTO(String name, List<FBMenuItem> items) {
        this.name = name;
        for (FBMenuItem item : items) {
            menuItem.add(new MenuItemDTO(item));
        }
    }

    public List<MenuItemDTO> getMenuItem() {
        return menuItem;
    }

    public void setMenuItem(List<MenuItemDTO> menuItem) {
        this.menuItem = menuItem;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
