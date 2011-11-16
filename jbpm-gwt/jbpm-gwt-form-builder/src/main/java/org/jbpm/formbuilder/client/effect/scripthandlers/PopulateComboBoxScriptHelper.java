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

import org.jbpm.formbuilder.client.FormBuilderGlobals;
import org.jbpm.formbuilder.client.messages.I18NConstants;
import org.jbpm.formbuilder.shared.api.FBScript;
import org.jbpm.formbuilder.shared.api.FBScriptHelper;
import org.jbpm.formbuilder.shared.form.FormEncodingException;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.gwtent.reflection.client.Reflectable;

/**
 * 
 */
@Reflectable
public class PopulateComboBoxScriptHelper extends FlexTable implements FBScriptHelper {

    private final I18NConstants i18n = FormBuilderGlobals.getInstance().getI18n();
    
    private final TextBox url = new TextBox();
    private final ListBox method = new ListBox();
    private final ListBox resultStatus = new ListBox();
    private final ListBox responseLanguage = new ListBox();
    private final TextBox resultXPath = new TextBox();
    private final TextBox subPathForKeys = new TextBox();
    private final TextBox subPathForValues = new TextBox();
    private final TextBox checkBoxId = new TextBox();
    
    private final HeaderViewPanel headerViewPanel = new HeaderViewPanel();

    public PopulateComboBoxScriptHelper() {
        super();
        setWidget(0, 0, new Label(i18n.PopulateComboBoxScriptHelperUrl()));
        setWidget(0, 1, url);
        setWidget(1, 0, new Label(i18n.PopulateComboBoxScriptHelperMethod()));
        setWidget(1, 1, method);
        setWidget(2, 0, new Label(i18n.PopulateComboBoxScriptHelperResultStatus()));
        setWidget(2, 1, resultStatus);
        setWidget(3, 0, new Label(i18n.PopulateComboBoxScriptHelperResponseLanguage()));
        setWidget(3, 1, responseLanguage);
        setWidget(4, 0, new Label(i18n.PopulateComboBoxScriptHelperResultPath()));
        setWidget(4, 1, resultXPath);
        setWidget(5, 0, new Label(i18n.PopulateComboBoxScriptHelperSubPathForKeys()));
        setWidget(5, 1, subPathForKeys);
        setWidget(6, 0, new Label(i18n.PopulateComboBoxScriptHelperSubPathForValues()));
        setWidget(6, 1, subPathForValues);
        setWidget(7, 0, new Label(i18n.PopulateComboBoxScriptHelperSendHeaders()));
        setWidget(7, 1, new Button(i18n.PopulateComboBoxScriptHelperAddHeader(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                headerViewPanel.addHeaderRow("", "");
            }
        }));
        setWidget(8, 0, headerViewPanel);
        getFlexCellFormatter().setColSpan(8, 0, 2);
        setWidget(9, 0, new Label(i18n.PopulateComboBoxScriptHelperCheckBoxId()));
        setWidget(9, 1, checkBoxId);
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
    
    @Override
    public Map<String, Object> getDataMap() {
        String urlValue = this.url.getValue();
        String methodValue = this.method.getValue(this.method.getSelectedIndex());
        String resultStatusValue = this.method.getValue(this.resultStatus.getSelectedIndex());
        String resultPathValue = this.resultXPath.getValue();
        String subPathForKeysValue = this.subPathForKeys.getValue();
        String subPathForValuesValue = this.subPathForValues.getValue();
        String checkBoxIdValue = this.checkBoxId.getValue();
        String responseLanguageValue = this.responseLanguage.getValue(this.responseLanguage.getSelectedIndex());
        
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("@className", RestServiceScriptHelper.class.getName());
        map.put("urlValue", urlValue);
        map.put("methodValue", methodValue);
        map.put("resultStatusValue", resultStatusValue);
        map.put("resultPathValue", resultPathValue);
        map.put("subPathForKeysValue", subPathForKeysValue);
        map.put("subPathForValuesValue", subPathForValuesValue);
        map.put("checkBoxIdValue", checkBoxIdValue);
        map.put("responseLanguageValue", responseLanguageValue);
        Map<String, Object> headersMap = new HashMap<String, Object>();
        for (Map.Entry<String, String> entry : headerViewPanel.getHeaders()) {
            headersMap.put(entry.getKey(), entry.getValue());
        }
        map.put("headers", headersMap);
        return map;
    }

    @Override
    public void setDataMap(Map<String, Object> dataMap) throws FormEncodingException {
        String urlValue = (String) dataMap.get("urlValue");
        if (urlValue == null) urlValue = "";
        String methodValue = (String) dataMap.get("methodValue");
        if (methodValue == null) methodValue = "";
        String resultStatusValue = (String) dataMap.get("resultStatusValue");
        if (resultStatusValue == null) resultStatusValue = "";
        String resultPathValue = (String) dataMap.get("resultPathValue");
        if (resultPathValue == null) resultPathValue = "";
        String subPathForKeysValue = (String) dataMap.get("subPathForKeysValue");
        if (subPathForKeysValue == null) subPathForKeysValue = "";
        String subPathForValuesValue = (String) dataMap.get("subPathForValuesValue");
        if (subPathForValuesValue == null) subPathForValuesValue = "";
        String checkBoxIdValue = (String) dataMap.get("checkBoxIdValue");
        if (checkBoxIdValue == null) checkBoxIdValue = "";
        String responseLanguageValue = (String) dataMap.get("responseLanguageValue");
        if (responseLanguageValue == null) responseLanguageValue = "";
        @SuppressWarnings("unchecked")
        Map<String, Object> headerMap = (Map<String, Object>) dataMap.get("headers"); 

        this.url.setValue(urlValue);
        for (int index = 0; index < this.method.getItemCount(); index++) {
            if (this.method.getValue(index).equals(methodValue)) {
                this.method.setSelectedIndex(index);
                break;
            }
        }
        for (int index = 0; index < this.resultStatus.getItemCount(); index++) {
            if (this.resultStatus.getValue(index).equals(resultStatusValue)) {
                this.resultStatus.setSelectedIndex(index);
                break;
            }
        }
        this.resultXPath.setValue(resultPathValue);
        for (int index = 0; index < this.responseLanguage.getItemCount(); index++) {
            if (this.responseLanguage.getValue(index).equals(responseLanguageValue)) {
                this.responseLanguage.setSelectedIndex(index);
                break;
            }
        }
        headerViewPanel.clear();
        if (headerMap != null) {
            for (Map.Entry<String, Object> entry : headerMap.entrySet()) {
                headerViewPanel.addHeaderRow(entry.getKey(), (String) entry.getValue());
            }
        }
        this.subPathForKeys.setValue(subPathForKeysValue);
        this.subPathForValues.setValue(subPathForValuesValue);
        this.checkBoxId.setValue(checkBoxIdValue);
    }

    @Override
    public String asScriptContent() {
        long id = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();
        sb.append("var checkBoxRef" + id + " = document.getElementById('" + checkBoxId.getValue() + "');\n");
        sb.append("var url" + id + " = \"" + url.getValue() + "\";\n");
        sb.append("var method" + id + " = \"" + method.getValue(method.getSelectedIndex()) + "\";\n");
        sb.append("var xmlhttp" + id + ";\n");
        sb.append("if (window.XMLHttpRequest) {// code for IE7+, Firefox, Chrome, Opera, Safari\n");
        sb.append("   xmlhttp" + id + "=new XMLHttpRequest();\n");
        sb.append("} else {// code for IE6, IE5\n");
        sb.append("   xmlhttp" + id + "=new ActiveXObject(\"Microsoft.XMLHTTP\");\n");
        sb.append("}\n");
        sb.append("xmlhttp" + id + ".onreadystatechange=function() {\n");
        sb.append("   if (xmlhttp" + id + ".readyState==4 && xmlhttp" + id + ".status==" + resultStatus.getValue(resultStatus.getSelectedIndex()) + ") {\n");  
        sb.append("      var xmlDoc" + id + " = null;\n");
        sb.append("      if (window.ActiveXObject) { // code for IE\n");
        sb.append("         xmlDoc" + id + "=new ActiveXObject(\"Microsoft.XMLDOM\");\n");
        sb.append("         xmlDoc" + id + ".write(xmlhttp" + id + ".responseText);\n");
        sb.append("      } else if (document.implementation && document.implementation.createDocument) { // code for Mozilla, Firefox, Opera, etc.\n");
        sb.append("         xmlDoc" + id + "=document.implementation.createDocument(\"\",\"\",null);\n");
        sb.append("         xmlDoc" + id + ".write(xmlhttp" + id + ".responseText);\n");
        sb.append("      } else {\n");
        sb.append("         alert('Your browser cannot handle this script');\n");
        sb.append("      }\n");
        sb.append("      var xmlNodeList" + id + " = xmlDoc" + id + ".selectNodes(\"" + resultXPath.getValue() + "\");\n");
        sb.append("      checkBoxRef" + id + ".options.length = 0; //clears combobox\n");
        sb.append("      for (var idx = 0; idx < xmlNodeList" + id + ".length; idx++ ) {\n");
        sb.append("         var opt = document.createElement('option');\n");
        sb.append("         opt.value = xmlNodeList" + id + ".item(idx).getElementsByTagName('" + subPathForKeys.getValue() + "')[0].nodeValue;\n");
        sb.append("         opt.innerText = xmlNodeList" + id + ".item(idx).getElementsByTagName('" + subPathForValues.getValue() + "')[0].nodeValue;\n");
        sb.append("         checkBoxRef" + id + ".options.add(opt);\n");
        sb.append("      }\n");
        sb.append("   }\n");
        sb.append("}\n");
        for (Map.Entry<String, String> header : headerViewPanel.getHeaders()) {
            sb.append("xmlhttp" + id + ".setRequestHeader(\"" + header.getKey() + "\",\"" + header.getValue() + "\");\n");
        }
        sb.append("xmlhttp" + id + ".open(method" + id + ", url" + id + ", true);\n");
        sb.append("xmlhttp" + id + ".send();\n");
        return sb.toString();
    }

    @Override
    public Widget draw() {
        return this;
    }
    
    @Override
    public String getName() {
        return i18n.PopulateComboBoxScriptHelperName();
    }

}
