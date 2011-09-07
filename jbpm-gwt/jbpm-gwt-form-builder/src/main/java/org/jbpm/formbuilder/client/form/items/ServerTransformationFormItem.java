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
import org.jbpm.formbuilder.client.form.FBInplaceEditor;
import org.jbpm.formbuilder.client.form.editors.ServerScriptEditor;
import org.jbpm.formbuilder.shared.api.FormItemRepresentation;
import org.jbpm.formbuilder.shared.api.items.ServerTransformationRepresentation;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import com.gwtent.reflection.client.Reflectable;

/**
 * server-side form item. Represents a piece of server script
 */
@Reflectable
public class ServerTransformationFormItem extends FBFormItem {

    private Label scriptMarker = new Label("{ script }");
    
    private TextArea script = new TextArea();
    private String language;

    public ServerTransformationFormItem() {
        this(new ArrayList<FBFormEffect>());
    }
    
    public ServerTransformationFormItem(List<FBFormEffect> formEffects) {
        super(formEffects);
        scriptMarker.setStyleName("transformationBlockBorder");
        add(scriptMarker);
    }

    @Override
    public Map<String, Object> getFormItemPropertiesMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("language", this.language);
        return map;
    }

    @Override
    public void saveValues(Map<String, Object> asPropertiesMap) {
        this.language = extractString(asPropertiesMap.get("language"));
    }

    @Override
    public FormItemRepresentation getRepresentation() {
        ServerTransformationRepresentation rep = getRepresentation(new ServerTransformationRepresentation());
        rep.setScript(this.script.getValue());
        rep.setLanguage(this.language);
        return rep;
    }
    
    @Override
    public void populate(FormItemRepresentation rep) throws FormBuilderException {
        if (!(rep instanceof ServerTransformationRepresentation)) {
            throw new FormBuilderException(i18n.RepNotOfType(rep.getClass().getName(), "ServerTransformationRepresentation"));
        }
        super.populate(rep);
        ServerTransformationRepresentation srep = (ServerTransformationRepresentation) rep;
        this.setScriptContent(srep.getScript());
        srep.setLanguage(this.language);
    }

    @Override
    public FBFormItem cloneItem() {
        ServerTransformationFormItem clone = cloneItem(new ServerTransformationFormItem(getFormEffects()));
        clone.setScriptContent(this.getScriptContent());
        clone.language = this.language;
        return clone;
    }

    @Override
    public Widget cloneDisplay() {
        Label display = new Label(scriptMarker.getText());
        display.setHeight(getHeight());
        display.setWidth(getWidth());
        display.setStyleName("transformationBlockBorder");
        return display;
    }
    
    @Override
    public FBInplaceEditor createInplaceEditor() {
        return new ServerScriptEditor(this);
    }

    public void setScriptContent(String value) {
        script.setValue(value);
    }
    
    public String getScriptContent() {
        return script.getValue();
    }
}
