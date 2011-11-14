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
package org.jbpm.formbuilder.client.effect.scripthandlers;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.formbuilder.shared.api.FBScript;
import org.jbpm.formbuilder.shared.api.FBScriptHelper;
import org.jbpm.formbuilder.shared.form.FormEncodingException;

import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import com.gwtent.reflection.client.Reflectable;

/**
 * 
 */
@Reflectable
public class PlainTextScriptHelper implements FBScriptHelper {

    private final TextArea scriptPanel = new TextArea();
    
    public PlainTextScriptHelper() {
        scriptPanel.setCharacterWidth(50);
        scriptPanel.setVisibleLines(15);
    }
    
    @Override
    public void setScript(FBScript script) {
        if (script != null) {
            this.scriptPanel.setValue(script.getContent());
        }
    }
    
    @Override
    public Map<String, Object> getDataMap() {
        Map<String, Object> dataMap = new HashMap<String, Object>();
        String value = this.scriptPanel.getValue();
        dataMap.put("@className", PlainTextScriptHelper.class.getName());
        dataMap.put("scriptPanel", value);
        return dataMap;
    }

    @Override
    public void setDataMap(Map<String, Object> dataMap) throws FormEncodingException {
        String value = (String) dataMap.get("scriptPanel");
        if (value == null) {
            this.scriptPanel.setValue("");
        } else {
            this.scriptPanel.setValue(value);
        }
    }

    @Override
    public String asScriptContent() {
        return this.scriptPanel.getValue();
    }

    @Override
    public Widget draw() {
        return this.scriptPanel;
    }

    /* (non-Javadoc)
     * @see org.jbpm.formbuilder.shared.api.FBScriptHelper#getName()
     */
    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return "text";
    }

}
