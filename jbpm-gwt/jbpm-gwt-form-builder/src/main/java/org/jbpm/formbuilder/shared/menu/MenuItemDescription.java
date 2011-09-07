/*
 * Copyright 2011 JBoss Inc 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.formbuilder.shared.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.shared.api.FormItemRepresentation;
import org.jbpm.formbuilder.shared.api.Mappable;
import org.jbpm.formbuilder.shared.form.FormEncodingException;
import org.jbpm.formbuilder.shared.form.FormEncodingFactory;
import org.jbpm.formbuilder.shared.form.FormRepresentationDecoder;

import com.gwtent.reflection.client.Reflectable;

@Reflectable
public class MenuItemDescription implements Mappable {

    private String className;
    private String name;
    private FormItemRepresentation itemRepresentation;
    private List<FormEffectDescription> effects = new ArrayList<FormEffectDescription>();
    private List<String> allowedEvents = new ArrayList<String>();
    
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
    
    public List<String> getAllowedEvents() {
        return allowedEvents;
    }
    
    public void setAllowedEvents(List<String> allowedEvents) {
        this.allowedEvents = allowedEvents;
    }
    
    @Override
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
        data.put("allowedEvents", this.allowedEvents);
        return data;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setDataMap(Map<String, Object> data) throws FormEncodingException {
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
        List<Object> allowedEventsList = (List<Object>) data.get("allowedEvents");
        if (allowedEventsList != null) {
            this.allowedEvents.clear();
            for (Object obj : allowedEventsList) {
                this.allowedEvents.add(obj.toString());
            }
        }
        FormRepresentationDecoder decoder = FormEncodingFactory.getDecoder();
        Map<String, Object> itemMap = (Map<String, Object>) data.get("itemRepresentation");
        this.itemRepresentation = (FormItemRepresentation) decoder.decode(itemMap);
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
        if (!equals) return equals;
        equals = (this.allowedEvents == null && other.allowedEvents == null) || 
            (this.allowedEvents != null && this.allowedEvents.equals(other.allowedEvents));
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
        aux = this.allowedEvents == null ? 0 : this.allowedEvents.hashCode();
        result = 37 * result + aux;
        return result;
    }
}
