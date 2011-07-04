package org.jbpm.formbuilder.client.form.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.FormBuilderException;
import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.items.TextFieldRepresentation;

import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class TextFieldFormItem extends FBFormItem {

    private final TextBox textBox = new TextBox();
    
    private String defaultContent = null;
    private String name = null;
    private String id = null;
    private String title = null;
    private Integer maxlength = null;
    
    public TextFieldFormItem() {
        this(new ArrayList<FBFormEffect>());
    }
    
    public TextFieldFormItem(List<FBFormEffect> formEffects) {
        super(formEffects);
        add(textBox);
        setWidth("100px");
        setHeight("21px");
        textBox.setWidth(getWidth());
        textBox.setHeight(getHeight());
    }
    
    @Override
    public Map<String, Object> getFormItemPropertiesMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("inputDefaultContent", this.defaultContent);
        map.put("name", this.name);
        map.put("height", getHeight());
        map.put("width", getWidth());
        map.put("maxlength", this.maxlength);
        map.put("title", this.title);
        map.put("id", this.id);
        return map;
    }
    
    @Override
    public void saveValues(Map<String, Object> asPropertiesMap) {
        this.defaultContent = extractString(asPropertiesMap.get("inputDefaultContent"));
        this.name = extractString(asPropertiesMap.get("name"));
        this.setHeight(extractString(asPropertiesMap.get("height")));
        this.setWidth(extractString(asPropertiesMap.get("width")));
        this.title = extractString(asPropertiesMap.get("title"));
        this.maxlength = extractInt(asPropertiesMap.get("maxlength"));
        this.id = extractString(asPropertiesMap.get("id"));
        
        populate(this.textBox);
    }

    private void populate(TextBox textBox) {
        if (this.defaultContent != null) {
            textBox.setValue(this.defaultContent);
        }
        if (this.name != null) {
            textBox.setName(this.name);
        }
        if (getHeight() != null) {
            textBox.setHeight(getHeight());
        }
        if (getWidth() != null) {
            textBox.setWidth(getWidth());
        }
        if (this.title != null) {
            textBox.setTitle(this.title);
        }
        if (this.maxlength != null) {
            textBox.setMaxLength(this.maxlength);
        }
    }
    
    @Override
    public FormItemRepresentation getRepresentation() {
        TextFieldRepresentation rep = super.getRepresentation(new TextFieldRepresentation());
        rep.setDefaultValue(this.defaultContent);
        rep.setName(this.name);
        rep.setId(this.id);
        rep.setMaxLength(this.maxlength);
        return rep;
    }
    
    @Override
    public void populate(FormItemRepresentation rep) throws FormBuilderException {
        if (!(rep instanceof TextFieldRepresentation)) {
            throw new FormBuilderException("rep should be of type TextFieldRepresentation but is of type " + rep.getClass().getName());
        }
        super.populate(rep);
        TextFieldRepresentation trep = (TextFieldRepresentation) rep;
        this.defaultContent = trep.getDefaultValue();
        this.name = trep.getName();
        this.id = trep.getId();
        this.maxlength = trep.getMaxLength();
        populate(this.textBox);
    }

    @Override
    public FBFormItem cloneItem() {
        TextFieldFormItem clone = new TextFieldFormItem(getFormEffects());
        clone.defaultContent = this.defaultContent;
        clone.setHeight(this.getHeight());
        clone.id = this.id;
        clone.maxlength = this.maxlength;
        clone.name = this.name;
        clone.title = this.title;
        clone.setWidth(this.getWidth());
        clone.populate(clone.textBox);
        return clone;
    }
    
    @Override
    public Widget cloneDisplay() {
        TextBox tb = new TextBox();
        populate(tb);
        return tb;
    }
}
