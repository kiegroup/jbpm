package org.jbpm.formbuilder.client.form.items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.items.HiddenRepresentation;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.Widget;

public class HiddenFormItem extends FBFormItem {

    private Hidden hidden = new Hidden();
    
    private String id;
    private String name;
    private String value;
    
    public HiddenFormItem(List<FBFormEffect> formEffects) {
        super(formEffects);
        Grid border = new Grid(1, 1);
        border.setSize("100px", "20px");
        border.setBorderWidth(1);
        border.setWidget(0, 0, hidden);
        add(border);
        setSize("100px", "20px");
    }

    @Override
    public Map<String, Object> getFormItemPropertiesMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("value", this.value);
        map.put("name", this.name);
        map.put("id", this.id);
        return map;
    }
    
    @Override
    public void saveValues(Map<String, Object> asPropertiesMap) {
        this.value = asPropertiesMap.get("value").toString();
        this.name = asPropertiesMap.get("name").toString();
        this.id = asPropertiesMap.get("id").toString();
        populate(this.hidden);
    }

    private void populate(Hidden hidden) {
        if (this.id != null) {
            hidden.setID(id);
        }
        if (this.name != null) {
            hidden.setName(name);
        }
        if (this.value != null) {
            hidden.setValue(value);
        }
    }

    @Override
    public FormItemRepresentation getRepresentation() {
        HiddenRepresentation rep = super.getRepresentation(new HiddenRepresentation());
        rep.setId(id);
        rep.setName(name);
        rep.setValue(value);
        return rep;
    }

    @Override
    public FBFormItem cloneItem() {
        HiddenFormItem clone = new HiddenFormItem(getFormEffects());
        clone.id = this.id;
        clone.name = this.name;
        clone.value = this.value;
        clone.populate(clone.hidden);
        return clone;
    }
    
    @Override
    public Widget cloneDisplay() {
        Hidden hi = new Hidden();
        populate(hi);
        return hi;
    }
}
