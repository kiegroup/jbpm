package org.jbpm.formbuilder.client.form.items;

import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.items.CheckBoxRepresentation;

import com.google.gwt.user.client.ui.CheckBox;

public class CheckBoxFormItem extends FBFormItem {

    private CheckBox checkBox = new CheckBox();
    
    private String formValue;
    private Boolean checked;
    private String name;
    private String id;
    
    public CheckBoxFormItem(List<FBFormEffect> formEffects) {
        super(formEffects);
        add(checkBox);
    }

    @Override
    public Map<String, Object> getFormItemPropertiesMap() {
        Map<String, Object> map = super.getFormItemPropertiesMap();
        map.put("formValue", this.formValue);
        map.put("checked", this.checked);
        map.put("name", this.name);
        map.put("id", this.id);
        return map;
    }
    
    @Override
    public void saveValues(Map<String, Object> asPropertiesMap) {
        this.formValue = asPropertiesMap.get("formValue").toString();
        this.name = asPropertiesMap.get("name").toString();
        this.id = asPropertiesMap.get("id").toString();
        this.checked = extractBoolean(asPropertiesMap.get("checked"));
        
        populate();
    }

    private void populate() {
        checkBox.setFormValue(formValue);
        checkBox.setName(name);
        checkBox.setValue(checked);
    }

    @Override
    public FormItemRepresentation getRepresentation() {
        CheckBoxRepresentation rep = new CheckBoxRepresentation();
        rep.setFormValue(formValue);
        rep.setName(name);
        rep.setId(id);
        rep.setChecked(checked);
        return rep;
    }

    @Override
    public FBFormItem cloneItem() {
        CheckBoxFormItem clone = new CheckBoxFormItem(getFormEffects());
        clone.checked = this.checked;
        clone.formValue = this.formValue;
        clone.id = this.id;
        clone.name = this.name;
        clone.populate();
        return clone;
    }
}
