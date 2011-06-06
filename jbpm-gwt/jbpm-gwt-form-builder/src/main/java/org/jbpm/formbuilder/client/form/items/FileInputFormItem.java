package org.jbpm.formbuilder.client.form.items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.items.FileInputRepresentation;

import com.google.gwt.user.client.ui.FileUpload;

public class FileInputFormItem extends FBFormItem {

    private FileUpload fileUpload = new FileUpload();
    
    private String name;
    private String id;
    private String width;
    private String height;
    private String accept;
    
    public FileInputFormItem(List<FBFormEffect> formEffects) {
        super(formEffects);
        add(fileUpload);
    }

    @Override
    public Map<String, Object> getFormItemPropertiesMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", this.name);
        map.put("id", this.id);
        map.put("width", this.width);
        map.put("height", this.height);
        map.put("accept", this.accept);
        return map;
    }
    
    @Override
    public void saveValues(Map<String, Object> asPropertiesMap) {
        this.name = asPropertiesMap.get("name").toString();
        this.width = asPropertiesMap.get("width").toString();
        this.height = asPropertiesMap.get("height").toString();
        this.id = asPropertiesMap.get("id").toString();
        this.accept = asPropertiesMap.get("accept").toString();

        populate();
    }

    private void populate() {
        fileUpload.setName(this.name);
        fileUpload.setWidth(this.width);
        fileUpload.setHeight(this.height);
    }

    @Override
    public FormItemRepresentation getRepresentation() {
        FileInputRepresentation rep = new FileInputRepresentation();
        rep.setHeight(this.height);
        rep.setId(this.id);
        rep.setName(this.name);
        rep.setWidth(this.width);
        rep.setAccept(this.accept);
        return rep;
    }

    @Override
    public FBFormItem cloneItem() {
        FileInputFormItem clone = new FileInputFormItem(getFormEffects());
        clone.accept = this.accept;
        clone.height = this.height;
        clone.id = this.id;
        clone.name = this.name;
        clone.width = this.width;
        clone.populate();
        return clone;
    }
}
