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
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.shared.api.FormItemRepresentation;

import com.google.gwt.user.client.ui.Widget;

/**
 * 
 */
public class RichTextEditorFormItem extends FBFormItem {

    /**
     * 
     */
    public RichTextEditorFormItem() {
        this(new ArrayList<FBFormEffect>());
    }
    
    /**
     * @param formEffects
     */
    public RichTextEditorFormItem(List<FBFormEffect> formEffects) {
        super(formEffects);
    }

    /* (non-Javadoc)
     * @see org.jbpm.formbuilder.client.form.FBFormItem#getFormItemPropertiesMap()
     */
    @Override
    public Map<String, Object> getFormItemPropertiesMap() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.jbpm.formbuilder.client.form.FBFormItem#saveValues(java.util.Map)
     */
    @Override
    public void saveValues(Map<String, Object> asPropertiesMap) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.jbpm.formbuilder.client.form.FBFormItem#getRepresentation()
     */
    @Override
    public FormItemRepresentation getRepresentation() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.jbpm.formbuilder.client.form.FBFormItem#cloneItem()
     */
    @Override
    public FBFormItem cloneItem() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.jbpm.formbuilder.client.form.FBFormItem#cloneDisplay()
     */
    @Override
    public Widget cloneDisplay() {
        // TODO Auto-generated method stub
        return null;
    }

}
