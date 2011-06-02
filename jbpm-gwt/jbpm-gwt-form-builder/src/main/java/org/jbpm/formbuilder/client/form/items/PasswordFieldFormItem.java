package org.jbpm.formbuilder.client.form.items;

import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.items.PasswordFieldRepresentation;

import com.google.gwt.user.client.ui.PasswordTextBox;

public class PasswordFieldFormItem extends FBFormItem {

    private final PasswordTextBox textBox = new PasswordTextBox();
    
    private String defaultContent = null;
    private String name = null;
    private String id = null;
    private String height = null;
    private String width = null;
    private String title = null;
    private Integer maxlength = null;
    
    public PasswordFieldFormItem(List<FBFormEffect> formEffects) {
        super(formEffects);
        add(textBox);
    }
    
    @Override
    public Map<String, Object> getFormItemPropertiesMap() {
        Map<String, Object> map = super.getFormItemPropertiesMap();
        map.put("inputDefaultContent", this.defaultContent);
        map.put("name", this.name);
        map.put("id", this.id);
        map.put("height", this.height);
        map.put("width", this.width);
        map.put("maxlength", this.maxlength);
        map.put("title", this.title);
        return map;
    }

    @Override
    public void saveValues(Map<String, Object> asPropertiesMap) {
        this.defaultContent = asPropertiesMap.get("inputDefaultContent").toString();
        this.name = asPropertiesMap.get("name").toString();
        this.id = asPropertiesMap.get("id").toString();
        this.height = asPropertiesMap.get("height").toString();
        this.width = asPropertiesMap.get("width").toString();
        this.title = asPropertiesMap.get("title").toString();
        this.maxlength = extractInt(asPropertiesMap.get("maxlength"));
        
        populate();
    }

    private void populate() {
        textBox.setValue(this.defaultContent);
        textBox.setName(this.name);
        textBox.setHeight(this.height);
        textBox.setWidth(this.width);
        textBox.setTitle(this.title);
        if (this.maxlength != null) {
            textBox.setMaxLength(this.maxlength);
        }
    }
    
    @Override
    public FormItemRepresentation getRepresentation() {
        PasswordFieldRepresentation rep = new PasswordFieldRepresentation();
        rep.setDefaultValue(this.defaultContent);
        rep.setName(this.name);
        rep.setId(this.id);
        rep.setMaxLength(this.maxlength);
        return rep;
    }
    
    @Override
    public FBFormItem cloneItem() {
        PasswordFieldFormItem clone = new PasswordFieldFormItem(getFormEffects());
        clone.defaultContent = this.defaultContent;
        clone.height = this.height;
        clone.id = this.id;
        clone.maxlength = this.maxlength;
        clone.name = this.name;
        clone.title = this.title;
        clone.width = this.width;
        clone.populate();
        return clone;
    }
}
