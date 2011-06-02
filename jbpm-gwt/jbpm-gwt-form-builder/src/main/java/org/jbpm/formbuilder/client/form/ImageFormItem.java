package org.jbpm.formbuilder.client.form;

import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.items.ImageRepresentation;

import com.google.gwt.user.client.ui.Image;

public class ImageFormItem extends FBFormItem {

    private Image image = new Image();
    
    private String altText;
    private String height;
    private String width;
    private String url;
    private String id;
    
    public ImageFormItem(List<FBFormEffect> formEffects) {
        super(formEffects);
        image.setResource(FormBuilderResources.INSTANCE.defaultImage());
        add(image);
    }

    @Override
    public Map<String, Object> getFormItemPropertiesMap() {
        Map<String, Object> map = super.getFormItemPropertiesMap();
        map.put("altText", this.altText);
        map.put("height", this.height);
        map.put("width", this.width);
        map.put("url", this.url);
        map.put("id", this.id);
        return map;
    }
    
    @Override
    public void saveValues(Map<String, Object> asPropertiesMap) {
        this.altText = asPropertiesMap.get("altText").toString();
        this.height = asPropertiesMap.get("height").toString();
        this.width = asPropertiesMap.get("width").toString();
        this.url = asPropertiesMap.get("url").toString();
        this.id = asPropertiesMap.get("id").toString();
        
        populate();
    }

    private void populate() {
        image.setAltText(this.altText);
        image.setHeight(this.height);
        image.setWidth(this.width);
        if (this.url != null && !"".equals(this.url)) {
            image.setUrl(this.url);
        }
        image.setTitle(this.altText);
    }

    @Override
    public FormItemRepresentation getRepresentation() {
        ImageRepresentation rep = new ImageRepresentation();
        rep.setAltText(this.altText);
        rep.setHeight(this.height);
        rep.setWidth(this.width);
        rep.setUrl(this.url);
        rep.setId(this.id);
        return rep;
    }

    @Override
    public FBFormItem cloneItem() {
        ImageFormItem clone = new ImageFormItem(getFormEffects());
        clone.altText = this.altText;
        clone.height = this.height;
        clone.id = this.id;
        clone.url = this.url;
        clone.width = this.width;
        clone.populate();
        return clone;
    }
}
