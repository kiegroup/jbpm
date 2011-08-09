/**
 * Copyright 2011 JBoss Inc 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.formbuilder.client.form.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.FormBuilderException;
import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.items.ImageRepresentation;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.gwtent.reflection.client.Reflectable;

/**
 * UI form item. Represents an image
 */
@Reflectable
public class ImageFormItem extends FBFormItem {

    private Image image = new Image();
    
    private String altText;
    private String url;
    private String id;

    public ImageFormItem() {
        this(new ArrayList<FBFormEffect>());
    }
    
    public ImageFormItem(List<FBFormEffect> formEffects) {
        super(formEffects);
        image.setResource(FormBuilderResources.INSTANCE.defaultImage());
        add(image);
        setWidth("200px");
        setHeight("150px");
    }

    @Override
    public Map<String, Object> getFormItemPropertiesMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("altText", this.altText);
        map.put("height", this.getHeight());
        map.put("width", this.getWidth());
        map.put("url", this.url);
        map.put("id", this.id);
        return map;
    }
    
    @Override
    public void saveValues(Map<String, Object> asPropertiesMap) {
        this.altText = extractString(asPropertiesMap.get("altText"));
        this.setHeight(extractString(asPropertiesMap.get("height")));
        this.setWidth(extractString(asPropertiesMap.get("width")));
        this.url = extractString(asPropertiesMap.get("url"));
        this.id = extractString(asPropertiesMap.get("id"));
        populate(this.image);
    }

    private void populate(Image image) {
        if (this.altText != null) {
            image.setAltText(this.altText);
            image.setTitle(this.altText);
        }
        if (this.getHeight() != null) {
            image.setHeight(this.getHeight());
        }
        if (this.getWidth() != null) {
            image.setWidth(this.getWidth());
        }
        if (this.url != null && !"".equals(this.url)) {
            image.setUrl(this.url);
        }
    }

    @Override
    public FormItemRepresentation getRepresentation() {
        ImageRepresentation rep = super.getRepresentation(new ImageRepresentation());
        rep.setAltText(this.altText);
        rep.setUrl(this.url);
        rep.setId(this.id);
        return rep;
    }
    
    @Override
    public void populate(FormItemRepresentation rep) throws FormBuilderException {
        if (!(rep instanceof ImageRepresentation)) {
            throw new FormBuilderException(i18n.RepNotOfType(rep.getClass().getName(), "ImageRepresentation"));
        }
        super.populate(rep);
        ImageRepresentation irep = (ImageRepresentation) rep;
        this.altText = irep.getAltText();
        this.url = irep.getUrl();
        this.id = irep.getId();
        populate(this.image);
    }

    @Override
    public FBFormItem cloneItem() {
        ImageFormItem clone = new ImageFormItem(getFormEffects());
        clone.altText = this.altText;
        clone.setHeight(this.getHeight());
        clone.id = this.id;
        clone.url = this.url;
        clone.setWidth(this.getWidth());
        clone.populate(clone.image);
        return clone;
    }
    
    @Override
    public Widget cloneDisplay() {
        Image im = new Image();
        populate(im);
        return im;
    }
}
