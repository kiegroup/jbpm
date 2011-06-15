package org.jbpm.formbuilder.server.menu;

import java.util.List;

public class MenuItemDescription {

    private String className;
    private List<FormEffectDescription> effects;
    
    public String getClassName() {
        return className;
    }
    
    public void setClassName(String className) {
        this.className = className;
    }
    
    public List<FormEffectDescription> getEffects() {
        return effects;
    }
    
    public void setEffects(List<FormEffectDescription> effects) {
        this.effects = effects;
    }
}
