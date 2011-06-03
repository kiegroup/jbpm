package org.jbpm.formbuilder.client.form.items;

import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.items.AbsolutePanelRepresentation;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Panel;

public class AbsoluteLayoutFormItem extends LayoutFormItem {

    private AbsolutePanel panel = new AbsolutePanel();
    
    private String id;
    private String height;
    private String width;
    
    public AbsoluteLayoutFormItem(List<FBFormEffect> formEffects) {
        super(formEffects);
        panel.setSize("90px", "90px");
        add(panel);
    }

    @Override
    public Map<String, Object> getFormItemPropertiesMap() {
        Map<String, Object> map = super.getFormItemPropertiesMap();
        map.put("id", this.id);
        map.put("height", this.height);
        map.put("width", this.width);
        return map;
    }
    
    @Override
    public void saveValues(Map<String, Object> asPropertiesMap) {
        this.id = extractString(asPropertiesMap.get("id"));
        this.height = extractString(asPropertiesMap.get("height"));
        this.width = extractString(asPropertiesMap.get("width"));
        populate();
    }

    private void populate() {
        panel.setHeight(this.height);
        panel.setWidth(this.width);
    }

    @Override
    public FormItemRepresentation getRepresentation() {
        AbsolutePanelRepresentation rep = new AbsolutePanelRepresentation();
        rep.setWidth(this.width);
        rep.setHeight(this.height);
        rep.setId(this.id);
        for (FBFormItem item : getItems()) {
            rep.addItem(item.getRepresentation(), item.getDesiredX(), item.getDesiredY());
        }
        return rep;
    }

    @Override
    public FBFormItem cloneItem() {
        AbsoluteLayoutFormItem clone = new AbsoluteLayoutFormItem(getFormEffects());
        clone.height = this.height;
        clone.id = this.id;
        clone.width = this.width;
        clone.populate();
        for (FBFormItem item : getItems()) {
            clone.add(item.cloneItem());
        }
        return clone;
    }

    @Override
    public boolean add(FBFormItem item) {
        int left = item.getDesiredX();
        int top = item.getDesiredY();
        panel.add(item, left, top);
        return super.add(item);
    }
    
    @Override
    public Panel getPanel() {
        return this.panel;
    }

}
