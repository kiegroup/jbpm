/*
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
import org.jbpm.formbuilder.shared.api.FormItemRepresentation;
import org.jbpm.formbuilder.shared.api.items.ImageRolodexRepresentation;

import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.impl.ClippedImagePrototype;
import com.gwtent.reflection.client.Reflectable;
import com.yesmail.gwt.rolodex.client.RolodexCard;
import com.yesmail.gwt.rolodex.client.RolodexCardBundle;
import com.yesmail.gwt.rolodex.client.RolodexPanel;

@Reflectable
public class ImageRolodexFormItem extends FBFormItem {

    private final RolodexPanel panel = new RolodexPanel(new RolodexCardBundle() {
        @Override
        public RolodexCard[] getRolodexCards() {
            ClippedImagePrototype expanded = new ClippedImagePrototype(FormBuilderResources.INSTANCE.defaultImage().getURL(), 0, 0, 300, 200);
            ClippedImagePrototype collapseLeft = new ClippedImagePrototype(FormBuilderResources.INSTANCE.defaultImage().getURL(), 0, 0, 100, 100);
            ClippedImagePrototype collapseRight = new ClippedImagePrototype(FormBuilderResources.INSTANCE.defaultImage().getURL(), 0, 0, 100, 100);
            return new RolodexCard[] {
                    new RolodexCard(expanded, collapseLeft, collapseRight, 300, 100, 0),
                    new RolodexCard(expanded, collapseLeft, collapseRight, 300, 100, 0),
                    new RolodexCard(expanded, collapseLeft, collapseRight, 300, 100, 0)
            };
        }
        
        @Override
        public int getMaxHeight() {
            return 400;
        }
    });
    
    public ImageRolodexFormItem() {
        this(new ArrayList<FBFormEffect>());
    }
    
    public ImageRolodexFormItem(List<FBFormEffect> formEffects) {
        super(formEffects);
        panel.setSize("400px", "180px");
        setSize("400px", "180px");
        add(panel);
    }

    @Override
    public Map<String, Object> getFormItemPropertiesMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        // TODO Auto-generated method stub
        return map;
    }

    @Override
    public void saveValues(Map<String, Object> asPropertiesMap) {
        // TODO Auto-generated method stub

    }
    
    private void populate(RolodexPanel panel) {
        //TODO implement
        /*panel.setHeight(height);
        panel.setWidth(width);
        int startIndex = 0;
        List<ImageFormItem> subItems = new ArrayList<ImageFormItem>();
        for (ImageFormItem subItem : subItems) {
            RolodexCard card = new RolodexCard(expanded, collapsedLeft, collapsedRight, expandedWidth, collapsedWidth, heightOffset)
        }
        panel.setSelectedCard(selectedCard);
        panel.setStyleName(style);*/

    }

    @Override
    public FormItemRepresentation getRepresentation() {
        ImageRolodexRepresentation irrep = new ImageRolodexRepresentation();
        // TODO Auto-generated method stub
        return irrep;
    }

    @Override
    public void populate(FormItemRepresentation rep) throws FormBuilderException {
        if (!(rep instanceof ImageRolodexRepresentation)) {
            throw new FormBuilderException(i18n.RepNotOfType(rep.getClass().getName(), "ImageRolodexRepresentation"));
        }
        super.populate(rep);
        ImageRolodexRepresentation irep = (ImageRolodexRepresentation) rep;
        // TODO Auto-generated method stub
        populate(this.panel);
        super.populate(rep);
    }
    
    @Override
    public FBFormItem cloneItem() {
        ImageRolodexFormItem item = super.cloneItem(new ImageRolodexFormItem());
        // TODO Auto-generated method stub
        return item;
    }

    @Override
    public Widget cloneDisplay(Map<String, Object> formData) {
        // TODO Auto-generated method stub
        return null;
    }

}
