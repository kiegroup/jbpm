package org.jbpm.formbuilder.client.form;

import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;

import com.google.gwt.user.client.ui.ListBox;

public class ComboBoxFormItem extends OptionsFormItem {

    private ListBox listBox = new ListBox();
    
    private Boolean multiple = null;
    private Integer visibleItems = null;
    private String title;
    private String width;
    private String height;
    
    public ComboBoxFormItem(List<FBFormEffect> formEffects) {
        super(formEffects);
        add(listBox);
    }

    @Override
    public String asCode(String type) {
        return null;
    }

    @Override
    public void saveValues(Map<String, Object> asPropertiesMap) {
        String s = (String) asPropertiesMap.get("multipleSelect");
        this.multiple = s == null ? null : Boolean.valueOf(s); 
        if (multiple != null) {
            listBox.setMultipleSelect(multiple);
        }
        visibleItems = extractInt(asPropertiesMap.get("verticalSize"));
        if (visibleItems != null && visibleItems > 0) {
            listBox.setVisibleItemCount(visibleItems);
        }
        title = asPropertiesMap.get("title").toString();
        listBox.setTitle(title);
        width = asPropertiesMap.get("width").toString();
        listBox.setWidth(width);
        width = asPropertiesMap.get("height").toString();
        listBox.setHeight(height);
    }
    
    @Override
    public Map<String, Object> getFormItemPropertiesMap() {
        Map<String, Object> itemPropertiesMap = super.getFormItemPropertiesMap();
        itemPropertiesMap.put("multipleSelect", this.multiple);
        itemPropertiesMap.put("verticalSize", this.visibleItems);
        itemPropertiesMap.put("title", this.title);
        itemPropertiesMap.put("width", this.width);
        itemPropertiesMap.put("height", this.height);
        return itemPropertiesMap;
    }

    @Override
    public void addItem(String label, String value) {
        if (value == null || "".equals(value)) {
            listBox.addItem(label);
        } else {
            listBox.addItem(label, value);
        }
    }
    
    @Override
    public FormItemRepresentation getRepresentation() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addEffect(FBFormEffect effect) {
        super.addEffect(effect);
        effect.setWidget(this.listBox);
    }
}
