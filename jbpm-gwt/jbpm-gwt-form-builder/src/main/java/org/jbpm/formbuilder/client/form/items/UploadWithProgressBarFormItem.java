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

import gwtupload.client.SingleUploader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.FormBuilderException;
import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.shared.api.FormItemRepresentation;
import org.jbpm.formbuilder.shared.api.items.UploadWithProgressBarRepresentation;

import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.Widget;
import com.gwtent.reflection.client.Reflectable;

@Reflectable
public class UploadWithProgressBarFormItem extends FBFormItem {

    private final SingleUploader uploader = new SingleUploader();
    
    public UploadWithProgressBarFormItem() {
        this(new ArrayList<FBFormEffect>());
    }
    
    public UploadWithProgressBarFormItem(List<FBFormEffect> formEffects) {
        super(formEffects);
        String uploadAction = FormBuilderGlobals.getInstance().getService().getUploadActionURL();
        uploader.setServletPath(uploadAction);
        add(uploader);
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

    @Override
    public FormItemRepresentation getRepresentation() {
        UploadWithProgressBarRepresentation urep = new UploadWithProgressBarRepresentation(); 
        // TODO Auto-generated method stub
        return urep;
    }
    
    private void populate(SingleUploader uploader) {
        //TODO implement
    }

    @Override
    public void populate(FormItemRepresentation rep) throws FormBuilderException {
        // TODO Auto-generated method stub
        super.populate(rep);
    }
    
    @Override
    public FBFormItem cloneItem() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Widget cloneDisplay(Map<String, Object> formData) {
        SingleUploader uploader = new SingleUploader();
        uploader.setServletPath("upload");
        populate(uploader);
        if (getOutput() != null && getOutput().getName() != null) {
            uploader.getFileInput().setName(getOutput().getName());
        }
        super.populateActions(((FileUpload) uploader.getFileInput()).getElement());
        return uploader;
    }

}
