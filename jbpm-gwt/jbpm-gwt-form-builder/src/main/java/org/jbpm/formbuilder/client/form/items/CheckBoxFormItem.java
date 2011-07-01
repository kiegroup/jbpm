package org.jbpm.formbuilder.client.form.items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.FormBuilderException;
import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.items.CheckBoxRepresentation;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Widget;

public class CheckBoxFormItem extends FBFormItem {

    private CheckBox checkBox = new CheckBox();
    
    private String formValue;
    private Boolean checked;
    private String name;
    private String id;
    
    public CheckBoxFormItem(List<FBFormEffect> formEffects) {
        super(formEffects);
        add(checkBox);
        setWidth("15px");
        setHeight("15px");
        checkBox.setSize(getWidth(), getHeight());
    }

    @Override
    public Map<String, Object> getFormItemPropertiesMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("formValue", this.formValue);
        map.put("checked", this.checked);
        map.put("name", this.name);
        map.put("id", this.id);
        map.put("height", getHeight());
        map.put("width", getWidth());
        return map;
    }
    
    @Override
    public void saveValues(Map<String, Object> asPropertiesMap) {
        this.formValue = asPropertiesMap.get("formValue").toString();
        this.name = asPropertiesMap.get("name").toString();
        this.id = asPropertiesMap.get("id").toString();
        this.checked = extractBoolean(asPropertiesMap.get("checked"));
        setHeight(extractString(asPropertiesMap.get("height")));
        setWidth(extractString(asPropertiesMap.get("width")));
        populate(this.checkBox);
    }

    private void populate(CheckBox checkBox) {
        if (this.formValue != null) {
            checkBox.setFormValue(formValue);
        }
        if (this.name != null) {
            checkBox.setName(name);
        }
        if (this.checked != null) {
            checkBox.setValue(checked);
        }
        if (getWidth() != null) {
            checkBox.setWidth(getWidth());
        }
        if (getHeight() != null) {
            checkBox.setHeight(getHeight());
        }
    }

    @Override
    public FormItemRepresentation getRepresentation() {
        CheckBoxRepresentation rep = super.getRepresentation(new CheckBoxRepresentation());
        rep.setFormValue(formValue);
        rep.setName(name);
        rep.setId(id);
        rep.setChecked(checked);
        return rep;
    }
    
    @Override
    public void populate(FormItemRepresentation rep) throws FormBuilderException {
        if (!(rep instanceof CheckBoxRepresentation)) {
            throw new FormBuilderException("rep should be of type CheckBoxRepresentation but is of type " + rep.getClass().getName());
        }
        super.populate(rep);
        CheckBoxRepresentation crep = (CheckBoxRepresentation) rep;
        this.formValue = crep.getFormValue();
        this.name = crep.getName();
        this.id = crep.getId();
        this.checked = crep.getChecked();
        populate(this.checkBox);
    }

    @Override
    public FBFormItem cloneItem() {
        CheckBoxFormItem clone = new CheckBoxFormItem(getFormEffects());
        clone.setWidth(getWidth());
        clone.setHeight(getHeight());
        clone.checked = this.checked;
        clone.formValue = this.formValue;
        clone.id = this.id;
        clone.name = this.name;
        clone.populate(clone.checkBox);
        return clone;
    }
    
    @Override
    public Widget cloneDisplay() {
        CheckBox cb = new CheckBox();
        populate(cb);
        return cb;
    }
}
