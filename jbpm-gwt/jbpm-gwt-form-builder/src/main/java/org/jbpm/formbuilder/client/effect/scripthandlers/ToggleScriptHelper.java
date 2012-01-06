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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formapi.shared.api.FBScript;
import org.jbpm.formapi.shared.api.FBScriptHelper;
import org.jbpm.formbuilder.client.FormBuilderGlobals;
import org.jbpm.formbuilder.client.messages.I18NConstants;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.gwtent.reflection.client.Reflectable;

@Reflectable
public class ToggleScriptHelper extends FlexTable implements FBScriptHelper {

	private static final String TOGGLE = "toggle";
	private static final String SHOW = "show";
	private static final String HIDE = "hide";
	
	private final TextBox idField = new TextBox();
	private final ListBox actionOnEvent = new ListBox();
	private final ListBox hidingStrategy = new ListBox();
	
	private final I18NConstants i18n = FormBuilderGlobals.getInstance().getI18n();
	
	public ToggleScriptHelper() {
		super();
        setWidget(0, 0, new Label(i18n.ToggleScriptHelperIdField()));
        setWidget(0, 1, idField);
        setWidget(1, 0, new Label(i18n.ToggleScriptHelperActionOnEvent()));
        populateActionOnEventList();
        setWidget(1, 1, actionOnEvent);
        setWidget(2, 0, new Label(i18n.ToggleScriptHelperHidingStrategy()));
        populateHidingStrategyList();
        setWidget(2, 1, hidingStrategy);
	}
	
	private void populateActionOnEventList() {
		actionOnEvent.addItem(i18n.ToggleScriptHelperToggleAction(), TOGGLE);
		actionOnEvent.addItem(i18n.ToggleScriptHelperHideAction(), HIDE);
		actionOnEvent.addItem(i18n.ToggleScriptHelperShowAction(), SHOW);
		actionOnEvent.setSelectedIndex(0);
	}
	
	private void populateHidingStrategyList() {
		hidingStrategy.addItem(i18n.ToggleScriptHelperHiddenStrategy(), "hidden");
		hidingStrategy.addItem(i18n.ToggleScriptHelperCollapseStrategy(), "collapse");
		hidingStrategy.setSelectedIndex(0);
	}
	
	@Override
	public Map<String, Object> getDataMap() {
        String idFieldValue = this.idField.getValue();
        String actionOnEventValue = this.actionOnEvent.getValue(this.actionOnEvent.getSelectedIndex());
        String hidingStrategyValue = this.hidingStrategy.getValue(this.hidingStrategy.getSelectedIndex());
        
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("@className", ToggleScriptHelper.class.getName());
        map.put("idField", idFieldValue);
        map.put("actionOnEvent", actionOnEventValue);
        map.put("hidingStrategy", hidingStrategyValue);
        return map;
	}

	@Override
	public void setDataMap(Map<String, Object> dataMap) {
		String idFieldValue = (String) dataMap.get("idField");
		String actionOnEventValue = (String) dataMap.get("actionOnEvent");
		String hidingStrategyValue = (String) dataMap.get("hidingStrategy");
		
		this.idField.setValue(idFieldValue);
		for (int index = 0; index < this.actionOnEvent.getItemCount(); index++) {
            if (this.actionOnEvent.getValue(index).equals(actionOnEventValue)) {
                this.actionOnEvent.setSelectedIndex(index);
                break;
            }
        }
		for (int index = 0; index < this.hidingStrategy.getItemCount(); index++) {
            if (this.hidingStrategy.getValue(index).equals(hidingStrategyValue)) {
                this.hidingStrategy.setSelectedIndex(index);
                break;
            }
        }

	}

	@Override
	public String asScriptContent() {
		long id = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();
        String actionValue = actionOnEvent.getValue(actionOnEvent.getSelectedIndex());
        String strategy = hidingStrategy.getValue(hidingStrategy.getSelectedIndex());
        sb.append("var elementToggle" + id + " = document.getElementById('" + idField.getValue() + "');\n");
        sb.append("if (elementToggle" + id + " != null) {\n");
        if (actionValue.equals(HIDE)) {
        	//hide script
        	sb.append("   elementToggle" + id + ".style.visibility = '" + strategy + "';\n");
        }
        if (actionValue.equals(SHOW)) {
        	//show script
        	sb.append("   elementToggle" + id + ".style.visibility = 'visible';\n");
        }
        if (actionValue.equals(SHOW)) {
        	//show if not visible, hide if visible script
            sb.append("   if (elementToggle" + id + ".style.visibility == 'visible') {\n");
        	sb.append("      elementToggle" + id + ".style.visibility = '" + strategy + "';\n");
            sb.append("   } else {// code for IE6, IE5\n");
            sb.append("      elementToggle" + id + ".style.visibility = 'visible';\n");
            sb.append("   }\n");
        }
        sb.append("}\n");
        return sb.toString();
	}

	@Override
	public Widget draw() {
		return this;
	}

	@Override
	public String getName() {
		return i18n.ToggleScriptHelperName();
	}

    @Override
    public void setScript(FBScript script) {
        List<FBScriptHelper> helpers = script.getHelpers();
        if (helpers == null) {
            helpers = new ArrayList<FBScriptHelper>();
        }
        if (!helpers.contains(this)) {
            helpers.add(this);
        }
        script.setHelpers(helpers);
    }
}
