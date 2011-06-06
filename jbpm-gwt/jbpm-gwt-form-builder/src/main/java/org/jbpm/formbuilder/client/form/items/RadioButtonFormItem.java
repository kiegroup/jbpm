package org.jbpm.formbuilder.client.form.items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.items.RadioButtonRepresentation;

import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;

public class RadioButtonFormItem extends FBFormItem {

    private RadioButton button = new RadioButton("");
    
    private String name;
    private String id;
    private String value;
    private Boolean selected = Boolean.FALSE;
    
    public RadioButtonFormItem(List<FBFormEffect> formEffects) {
        super(formEffects);
        add(button);
    }

    @Override
    public Map<String, Object> getFormItemPropertiesMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", this.name);
        map.put("id", this.id);
        map.put("value", this.value);
        map.put("selected", this.selected);
        return map;
    }
    
    @Override
    public void saveValues(Map<String, Object> asPropertiesMap) {
        this.name = asPropertiesMap.get("name").toString();
        this.id = asPropertiesMap.get("id").toString();
        this.value = asPropertiesMap.get("value").toString();
        this.selected = extractBoolean(asPropertiesMap.get("selected"));

        populate(this.button);
    }

    private void populate(RadioButton button) {
        if (this.name != null) {
            button.setName(this.name);
        }
        if (this.value != null) {
            button.setFormValue(this.value);
        }
        if (this.selected != null) {
            button.setValue(this.selected);
        }
    }

    @Override
    public FormItemRepresentation getRepresentation() {
        RadioButtonRepresentation rep = new RadioButtonRepresentation();
        rep.setId(this.id);
        rep.setName(this.name);
        rep.setSelected(this.selected);
        rep.setValue(this.value); //TODO setInput
        return rep;
    }
    
    @Override
    public FBFormItem cloneItem() {
        RadioButtonFormItem clone = new RadioButtonFormItem(getFormEffects());
        clone.id = this.id;
        clone.name = this.name;
        clone.selected = this.selected;
        clone.value = this.value;
        clone.populate(clone.button);
        return clone;
    }
    
    @Override
    public Widget cloneDisplay() {
        RadioButton rb = new RadioButton("");
        populate(rb);
        return rb;
    }
}
