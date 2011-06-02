package org.jbpm.formbuilder.client.options;

import java.util.List;

import com.google.gwt.user.client.Command;

public class MainMenuOption {

    private String html;
    private Command command;
    private List<MainMenuOption> subMenu;

    public String getHtml() {
        return html;
    }
    
    public void setHtml(String html) {
        this.html = html;
    }
    
    public Command getCommand() {
        return command;
    }
    
    public void setCommand(Command command) {
        this.command = command;
    }
    
    public List<MainMenuOption> getSubMenu() {
        return subMenu;
    }
    
    public void setSubMenu(List<MainMenuOption> subMenu) {
        this.subMenu = subMenu;
    }
}
