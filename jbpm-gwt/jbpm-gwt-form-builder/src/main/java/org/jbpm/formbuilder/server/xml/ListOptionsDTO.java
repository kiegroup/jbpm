package org.jbpm.formbuilder.server.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;

import org.jbpm.formbuilder.client.options.MainMenuOption;

@XmlType (name = "menuOptions") public class ListOptionsDTO {

    @XmlElement @XmlList private List<MenuOptionDTO> menuOption = new ArrayList<MenuOptionDTO>();
    
    public ListOptionsDTO(List<MainMenuOption> options) {
        for (MainMenuOption option : options) {
            menuOption.add(new MenuOptionDTO(option));
        }
    }

    public List<MenuOptionDTO> getMenuOption() {
        return menuOption;
    }

    public void setMenuOption(List<MenuOptionDTO> menuOption) {
        this.menuOption = menuOption;
    }
}
