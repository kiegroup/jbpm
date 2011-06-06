package org.jbpm.formbuilder.client.form.items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.items.CompleteButtonRepresentation;

import com.google.gwt.user.client.ui.Button;

public class CompleteButtonFormItem extends FBFormItem {

    private Button button = new Button("Complete");

    private String height;
    private String width;
    private String innerText;
    private String name;
    private String id;
    private String cssStyleName;
    
    public CompleteButtonFormItem(List<FBFormEffect> formEffects) {
        super(formEffects);
        add(button);
    }
    
    @Override
    public void saveValues(Map<String, Object> asPropertiesMap) {
        this.height = asPropertiesMap.get("height").toString();
        this.width = asPropertiesMap.get("width").toString();
        this.name = asPropertiesMap.get("name").toString();
        this.id = asPropertiesMap.get("id").toString();
        this.innerText = asPropertiesMap.get("innerText").toString();
        this.cssStyleName = asPropertiesMap.get("cssStyleName").toString();
        
        populate();
    }

    private void populate() {
        if (this.height != null) {
            button.setHeight(this.height);
        }
        if (this.width != null) {
            button.setWidth(this.width);
        }
        if (this.innerText != null) {
            button.setText(this.innerText);
        }
        if (this.cssStyleName != null) {
            button.setStyleName(this.cssStyleName);
        }
    }

    @Override
    public Map<String, Object> getFormItemPropertiesMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("height", this.height);
        map.put("width", this.width);
        map.put("innerText", this.innerText);
        map.put("cssStyleName", this.cssStyleName);
        map.put("name", this.name);
        map.put("id", this.id);
        return map;
    }
    
    @Override
    public FormItemRepresentation getRepresentation() {
        CompleteButtonRepresentation rep = new CompleteButtonRepresentation();
        rep.setText(this.innerText);
        rep.setName(this.name);
        rep.setId(this.id);
        return rep;
    }
    
    @Override
    public FBFormItem cloneItem() {
        CompleteButtonFormItem clone = new CompleteButtonFormItem(getFormEffects());
        clone.cssStyleName = this.cssStyleName;
        clone.height = this.height;
        clone.id = this.id;
        clone.innerText = this.innerText;
        clone.name = this.name;
        clone.width = this.width;
        clone.populate();
        return clone;
    }
}
