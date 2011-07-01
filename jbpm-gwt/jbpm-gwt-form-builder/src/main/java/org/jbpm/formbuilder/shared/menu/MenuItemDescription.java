package org.jbpm.formbuilder.shared.menu;

import java.util.List;

import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;

public class MenuItemDescription {

    private String className;
    private String name;
    private FormItemRepresentation itemRepresentation;
    private List<FormEffectDescription> effects;
    
    public String getClassName() {
        return className;
    }
    
    public void setClassName(String className) {
        this.className = className;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FormItemRepresentation getItemRepresentation() {
        return itemRepresentation;
    }

    public void setItemRepresentation(FormItemRepresentation itemRepresentation) {
        this.itemRepresentation = itemRepresentation;
    }

    public List<FormEffectDescription> getEffects() {
        return effects;
    }
    
    public void setEffects(List<FormEffectDescription> effects) {
        this.effects = effects;
    }
}
