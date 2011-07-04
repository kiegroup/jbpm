package org.jbpm.formbuilder.shared.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.form.FormEncodingClientFactory;
import org.jbpm.formbuilder.shared.form.FormEncodingException;
import org.jbpm.formbuilder.shared.form.FormRepresentationDecoder;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.Mappable;

public class MenuItemDescription implements Mappable {

    private String className;
    private String name;
    private FormItemRepresentation itemRepresentation;
    private List<FormEffectDescription> effects = new ArrayList<FormEffectDescription>();
    
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
    
    public Map<String, Object> getDataMap() {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("className", this.className);
        data.put("name", this.name);
        data.put("itemRepresentation", itemRepresentation == null ? null : itemRepresentation.getDataMap());
        if (this.effects == null) {
            data.put("effects", null);
        } else {
            List<Object> effectsMap = new ArrayList<Object>();
            for (FormEffectDescription effect : this.effects) {
                effectsMap.add(effect.getDataMap());
            }
            data.put("effects", effectsMap);
        }
        return data;
    }

    @SuppressWarnings("unchecked")
    public void setDataMap(Map<String, Object> data) {
        this.className = (String) data.get("className");
        this.name = (String) data.get("name");
        List<Object> effectsMap = (List<Object>) data.get("effects");
        this.effects.clear();
        if (effectsMap != null) {
            for (Object objEffect : effectsMap) {
                Map<String, Object> effectDataMap = (Map<String, Object>) objEffect;
                FormEffectDescription effect = new FormEffectDescription();
                effect.setDataMap(effectDataMap);
                this.effects.add(effect);
            }
        }
        FormRepresentationDecoder decoder = FormEncodingClientFactory.getDecoder();
        try {
            this.itemRepresentation = (FormItemRepresentation) decoder.decode(
                    (Map<String, Object>) data.get("itemRepresentation"));
        } catch (FormEncodingException e) {
            //TODO see what to do with this error
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (!(obj instanceof MenuItemDescription)) return false;
        MenuItemDescription other = (MenuItemDescription) obj;
        boolean equals = (this.className == null && other.className == null) || (this.className != null && this.className.equals(other.className));
        if (!equals) return equals;
        equals = (this.name == null && other.name == null) || (this.name != null && this.name.equals(other.name));
        if (!equals) return equals;
        equals = (this.itemRepresentation == null && other.itemRepresentation == null) || 
            (this.itemRepresentation != null && this.itemRepresentation.equals(other.itemRepresentation));
        if (!equals) return equals;
        equals = (this.effects == null && other.effects == null) || (this.effects != null && this.effects.equals(other.effects));
        return equals;
    }
    
    @Override
    public int hashCode() {
        int result = super.hashCode();
        int aux = this.className == null ? 0 : this.className.hashCode();
        result = 37 * result + aux;
        aux = this.name == null ? 0 : this.name.hashCode();
        result = 37 * result + aux;
        aux = this.itemRepresentation == null ? 0 : this.itemRepresentation.hashCode();
        result = 37 * result + aux;
        aux = this.effects == null ? 0 : this.effects.hashCode();
        result = 37 * result + aux;
        return result;
    }
}
