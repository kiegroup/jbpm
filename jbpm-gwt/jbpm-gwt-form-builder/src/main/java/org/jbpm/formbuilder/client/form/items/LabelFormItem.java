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
import org.jbpm.formbuilder.client.form.FBInplaceEditor;
import org.jbpm.formbuilder.client.form.editors.LabelInplaceEditor;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.items.LabelRepresentation;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.gwtent.reflection.client.Reflectable;

/**
 * UI form item. Represents a label
 */
@Reflectable
public class LabelFormItem extends FBFormItem {

    private final Label label = new Label("Label");
    
    private String id;
    private String cssClassName;

    public LabelFormItem() {
        this(new ArrayList<FBFormEffect>());
    }
    
    public LabelFormItem(List<FBFormEffect> formEffects) {
        super(formEffects);
        setWidth("200px");
        add(getLabel());
    }
    
    @Override
    public Map<String, Object> getFormItemPropertiesMap() {
        Map<String, Object> formItemPropertiesMap = new HashMap<String, Object>();
        formItemPropertiesMap.put("id", id);
        formItemPropertiesMap.put("width", getWidth());
        formItemPropertiesMap.put("height", getHeight());
        formItemPropertiesMap.put("cssClassName", cssClassName);
        return formItemPropertiesMap;
    }

    @Override
    public FBInplaceEditor createInplaceEditor() {
        return new LabelInplaceEditor(this);
    }

    @Override
    public void saveValues(Map<String, Object> propertiesMap) {
        this.id = extractString(propertiesMap.get("id"));
        this.setWidth(extractString(propertiesMap.get("width")));
        this.setHeight(extractString(propertiesMap.get("height")));
        this.cssClassName = extractString(propertiesMap.get("cssClassName"));
        populate(getLabel());
    }

    private void populate(Label label) {
        if (this.getWidth() != null) {
            label.setWidth(this.getWidth());
        }
        if (this.getHeight() != null) {
            label.setHeight(this.getHeight());
        }
        if (this.cssClassName != null) {
            label.setStyleName(this.cssClassName);
        }
    }
    
    public Label getLabel() {
        return this.label;
    }
    
    @Override
    public void addEffect(FBFormEffect effect) {
        super.addEffect(effect);
        effect.setWidget(this.label);
    }
    
    @Override
    public FormItemRepresentation getRepresentation() {
        LabelRepresentation rep = super.getRepresentation(new LabelRepresentation());
        rep.setValue(this.label.getText());
        rep.setCssName(this.cssClassName);
        rep.setId(this.id);
        return rep;
    }
    
    @Override
    public void populate(FormItemRepresentation rep) throws FormBuilderException {
        if (!(rep instanceof LabelRepresentation)) {
            throw new FormBuilderException(i18n.RepNotOfType(rep.getClass().getName(), "LabelRepresentation"));
        }
        super.populate(rep);
        LabelRepresentation lrep = (LabelRepresentation) rep;
        this.label.setText(lrep.getValue());
        this.cssClassName = lrep.getCssName();
        this.id = lrep.getId();
        if (lrep.getWidth() != null && !"".equals(lrep.getWidth())) {
            setWidth(lrep.getWidth());
        }
        if (lrep.getHeight() != null && !"".equals(lrep.getHeight())) {
            setHeight(lrep.getHeight());
        }
        populate(this.label);
    }
    
    @Override
    public FBFormItem cloneItem() {
        LabelFormItem clone = new LabelFormItem(getFormEffects());
        clone.cssClassName = this.cssClassName;
        clone.setHeight(this.getHeight());
        clone.id = this.id;
        clone.setWidth(this.getWidth());
        clone.getLabel().setText(this.label.getText());
        clone.populate(clone.label);
        return clone;
    }
    
    @Override
    public Widget cloneDisplay() {
        Label lb = new Label();
        populate(lb);
        return lb;
    }
}
