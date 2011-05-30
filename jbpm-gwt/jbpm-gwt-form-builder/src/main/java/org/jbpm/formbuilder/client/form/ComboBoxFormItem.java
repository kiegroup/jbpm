package org.jbpm.formbuilder.client.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.items.ComboBoxRepresentation;
import org.jbpm.formbuilder.shared.rep.items.OptionRepresentation;

import com.google.gwt.user.client.ui.ListBox;

public class ComboBoxFormItem extends OptionsFormItem {

    private ListBox listBox = new ListBox();
    
    Map<String, String> items = new HashMap<String, String>();
    
    private Boolean multiple = null;
    private Integer visibleItems = null;
    private String title;
    private String width;
    private String height;
    private String name;
    private String id;
    
    public ComboBoxFormItem(List<FBFormEffect> formEffects) {
        super(formEffects);
        add(listBox);
    }

    @Override
    public void saveValues(Map<String, Object> asPropertiesMap) {
        String s = (String) asPropertiesMap.get("multipleSelect");
        this.multiple = s == null ? null : Boolean.valueOf(s); 
        if (this.multiple != null) {
            this.listBox.setMultipleSelect(this.multiple);
        }
        this.visibleItems = extractInt(asPropertiesMap.get("verticalSize"));
        if (this.visibleItems != null && this.visibleItems > 0) {
            this.listBox.setVisibleItemCount(this.visibleItems);
        }
        this.title = asPropertiesMap.get("title").toString();
        this.listBox.setTitle(title);
        this.width = asPropertiesMap.get("width").toString();
        this.listBox.setWidth(width);
        this.width = asPropertiesMap.get("height").toString();
        this.listBox.setHeight(height);
        this.name = asPropertiesMap.get("name").toString();
        this.id = asPropertiesMap.get("id").toString();
    }
    
    @Override
    public Map<String, Object> getFormItemPropertiesMap() {
        Map<String, Object> itemPropertiesMap = super.getFormItemPropertiesMap();
        itemPropertiesMap.put("multipleSelect", this.multiple);
        itemPropertiesMap.put("verticalSize", this.visibleItems);
        itemPropertiesMap.put("title", this.title);
        itemPropertiesMap.put("width", this.width);
        itemPropertiesMap.put("height", this.height);
        itemPropertiesMap.put("name", this.name);
        itemPropertiesMap.put("id", this.id);
        return itemPropertiesMap;
    }

    @Override
    public void addItem(String label, String value) {
        if (value == null || "".equals(value)) {
            listBox.addItem(label);
            items.put(label, label);
        } else {
            listBox.addItem(label, value);
            items.put(label, value);
        }
    }
    
    @Override
    public void deleteItem(String label) {
        if (label != null) {
            items.remove(label);
            int size = 0;
            do {
                size = listBox.getItemCount();
                for (int index = 0; index < listBox.getItemCount(); index++) {
                    if (listBox.getItemText(index).equals(label)) {
                        listBox.removeItem(index);
                        break;
                    }
                }
            } while (size != listBox.getItemCount());
        }
    }
    
    @Override
    public FormItemRepresentation getRepresentation() {
        ComboBoxRepresentation rep = new ComboBoxRepresentation();
        List<OptionRepresentation> elements = new ArrayList<OptionRepresentation>();
        for (String label : this.items.keySet()) {
            OptionRepresentation opt = new OptionRepresentation();
            opt.setLabel(label);
            opt.setValue(this.items.get(label));
        }
        rep.setElements(elements);
        rep.setName(this.name);
        rep.setId(this.id);
        return rep;
    }

    @Override
    public void addEffect(FBFormEffect effect) {
        super.addEffect(effect);
        effect.setWidget(this.listBox);
    }
}
