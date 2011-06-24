package org.jbpm.formbuilder.server.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;

import org.jbpm.formbuilder.client.options.MainMenuOption;

@XmlType (name = "menuOption") public class MenuOptionDTO {
    
    @XmlAttribute private String name;
    @XmlAttribute private String commandClass;
    @XmlElement @XmlList private List<MenuOptionDTO> menuOption = new ArrayList<MenuOptionDTO>();

    public MenuOptionDTO(MainMenuOption option) {
        this.name = option.getHtml();
        if (option.getCommand() != null) {
            this.commandClass = option.getCommand().getClass().getName();
        }
        if (option.getSubMenu() != null) {
            for (MainMenuOption opt : option.getSubMenu()) {
                menuOption.add(new MenuOptionDTO(opt));
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCommandClass() {
        return commandClass;
    }

    public void setCommandClass(String commandClass) {
        this.commandClass = commandClass;
    }

    public List<MenuOptionDTO> getMenuOption() {
        return menuOption;
    }

    public void setMenuOption(List<MenuOptionDTO> menuOption) {
        this.menuOption = menuOption;
    }
}
