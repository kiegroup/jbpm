package org.jbpm.formbuilder.server.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;

import org.jbpm.formbuilder.client.menu.FBMenuItem;

@XmlType (name ="menuGroups") public class ListMenuItemsDTO {

    @XmlElement @XmlList private List<MenuGroupDTO> menuGroup = new ArrayList<MenuGroupDTO>();
    
    public ListMenuItemsDTO(Map<String, List<FBMenuItem>> items) {
        for (String group : items.keySet()) {
            menuGroup.add(new MenuGroupDTO(group, items.get(group)));
        }
    }

    public List<MenuGroupDTO> getMenuGroup() {
        return menuGroup;
    }

    public void setMenuGroup(List<MenuGroupDTO> menuGroup) {
        this.menuGroup = menuGroup;
    }
}
