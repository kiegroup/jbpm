package org.jbpm.formbuilder.server.menu;

import java.util.List;

public class MenuOptionDescription {
    private String html;
    private List<MenuOptionDescription> subMenu;
    private String commandClass;

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public List<MenuOptionDescription> getSubMenu() {
        return subMenu;
    }

    public void setSubMenu(List<MenuOptionDescription> subMenu) {
        this.subMenu = subMenu;
    }

    public String getCommandClass() {
        return commandClass;
    }

    public void setCommandClass(String commandClass) {
        this.commandClass = commandClass;
    }
}
