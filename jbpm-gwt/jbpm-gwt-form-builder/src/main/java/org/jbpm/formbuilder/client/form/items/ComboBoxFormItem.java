package org.jbpm.formbuilder.client.form.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.form.OptionsFormItem;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.items.ComboBoxRepresentation;
import org.jbpm.formbuilder.shared.rep.items.OptionRepresentation;

import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

public class ComboBoxFormItem extends OptionsFormItem {

    private ListBox listBox = new ListBox();
    
    Map<String, String> items = new HashMap<String, String>();
    
    private Boolean multiple = null;
    private Integer visibleItems = null;
    private String title;
    private String name;
    private String id;
    
    public ComboBoxFormItem(List<FBFormEffect> formEffects) {
        super(formEffects);
        add(listBox);
        setWidth("30px");
        setHeight("15px");
        listBox.setSize(getWidth(), getHeight());
    }

    @Override
    public void saveValues(Map<String, Object> asPropertiesMap) {
        this.multiple = extractBoolean(asPropertiesMap.get("multipleSelect")); 
        this.visibleItems = extractInt(asPropertiesMap.get("verticalSize"));
        this.title = extractString(asPropertiesMap.get("title"));
        this.setWidth(extractString(asPropertiesMap.get("width")));
        this.setHeight(extractString(asPropertiesMap.get("height")));
        this.name = asPropertiesMap.get("name").toString();
        this.id = asPropertiesMap.get("id").toString();
        populate(this.listBox);
    }

    private void populate(ListBox listBox) {
        if (this.multiple != null) {
            this.listBox.setMultipleSelect(this.multiple);
        }
        if (this.visibleItems != null && this.visibleItems > 0) {
            this.listBox.setVisibleItemCount(this.visibleItems);
        }
        if (title != null) {
            this.listBox.setTitle(title);
        }
        if (getWidth() != null) {
            this.listBox.setWidth(getWidth());
        }
        if (getHeight() != null) {
            this.listBox.setHeight(getHeight());
        }
    }
    
    @Override
    public Map<String, Object> getFormItemPropertiesMap() {
        Map<String, Object> itemPropertiesMap = new HashMap<String, Object>();
        itemPropertiesMap.put("multipleSelect", this.multiple);
        itemPropertiesMap.put("verticalSize", this.visibleItems);
        itemPropertiesMap.put("title", this.title);
        itemPropertiesMap.put("width", this.getWidth());
        itemPropertiesMap.put("height", this.getHeight());
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
        ComboBoxRepresentation rep = super.getRepresentation(new ComboBoxRepresentation());
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
    
    @Override
    public Map<String, String> getItems() {
        Map<String, String> items = new HashMap<String, String>();
        for (int index = 0; index < listBox.getItemCount(); index++) {
            items.put(listBox.getItemText(index), listBox.getValue(index));
        }
        return items;
    }
    
    public void addItems(Map<String, String> items, ListBox listBox) {
        for (Map.Entry<String, String> entry : items.entrySet()) {
            listBox.addItem(entry.getKey(), entry.getValue());
        }
    }
    
    @Override
    public FBFormItem cloneItem() {
        ComboBoxFormItem clone = new ComboBoxFormItem(getFormEffects());
        clone.setHeight(this.getHeight());
        clone.id = this.id;
        clone.multiple = this.multiple;
        clone.name = this.name;
        clone.title = this.title;
        clone.visibleItems = this.visibleItems;
        clone.setWidth(this.getWidth());
        clone.populate(clone.listBox);
        clone.addItems(this.getItems(), clone.listBox);
        return clone;
    }
    
    @Override
    public Widget cloneDisplay() {
        ListBox lb = new ListBox();
        populate(lb);
        addItems(getItems(), lb);
        return lb;
    }
}
