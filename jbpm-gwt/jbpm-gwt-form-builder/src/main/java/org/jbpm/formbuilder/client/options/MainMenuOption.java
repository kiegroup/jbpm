package org.jbpm.formbuilder.client.options;

import java.util.List;

import org.jbpm.formbuilder.client.command.BaseCommand;

public class MainMenuOption {

    private String html;
    private BaseCommand command;
    private List<MainMenuOption> subMenu;
    private boolean enabled = true;

    public String getHtml() {
        return html;
    }
    
    public void setHtml(String html) {
        this.html = html;
    }
    
    public BaseCommand getCommand() {
        return command;
    }
    
    public void setCommand(BaseCommand command) {
        this.command = command;
    }
    
    public List<MainMenuOption> getSubMenu() {
        return subMenu;
    }
    
    public void setSubMenu(List<MainMenuOption> subMenu) {
        this.subMenu = subMenu;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
