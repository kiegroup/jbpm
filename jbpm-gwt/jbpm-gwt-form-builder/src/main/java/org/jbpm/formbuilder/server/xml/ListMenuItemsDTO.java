package org.jbpm.formbuilder.server.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.jbpm.formbuilder.shared.menu.MenuItemDescription;

@XmlRootElement (name ="menuGroups") public class ListMenuItemsDTO {

    private List<MenuGroupDTO> _menuGroup = new ArrayList<MenuGroupDTO>();
    
    public ListMenuItemsDTO() {
        // jaxb needs a default constructor
    }
    
    public ListMenuItemsDTO(Map<String, List<MenuItemDescription>> items) {
        for (String group : items.keySet()) {
            _menuGroup.add(new MenuGroupDTO(group, items.get(group)));
        }
    }

    @XmlElement
    public List<MenuGroupDTO> getMenuGroup() {
        return _menuGroup;
    }

    public void setMenuGroup(List<MenuGroupDTO> menuGroup) {
        this._menuGroup = menuGroup;
    }
}
