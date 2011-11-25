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
import org.jbpm.formapi.shared.form.FormEncodingException;
import org.jbpm.formbuilder.client.FormBuilderGlobals;
import org.jbpm.formbuilder.client.messages.I18NConstants;

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
public class RestServiceScriptHelper extends FlexTable implements FBScriptHelper {

    private final I18NConstants i18n = FormBuilderGlobals.getInstance().getI18n();
    
    private final TextBox url = new TextBox();
    private final ListBox method = new ListBox();
    private final ListBox resultStatus = new ListBox();
    private final TextBox resultXPath = new TextBox();
    private final TextBox exportVariableName = new TextBox();
    private final ListBox responseLanguage = new ListBox();
    
    private final HeaderViewPanel headerViewPanel = new HeaderViewPanel();
    
    public RestServiceScriptHelper() {
        super();
        setWidget(0, 0, new Label(i18n.RestServiceScriptHelperUrl()));
        setWidget(0, 1, url);
        setWidget(1, 0, new Label(i18n.RestServiceScriptHelperMethod()));
        populateMethodList();
        setWidget(1, 1, method);
        setWidget(2, 0, new Label(i18n.RestServiceScriptHelperResultStatus()));
        populateResultStatusList();
        setWidget(2, 1, resultStatus);
        setWidget(3, 0, new Label(i18n.RestServiceScriptHelperResultPath()));
        setWidget(3, 1, resultXPath);
        setWidget(4, 0, new Label(i18n.RestServiceScriptHelperExportVariable()));
        setWidget(4, 1, exportVariableName);
        setWidget(5, 0, new Label(i18n.RestServiceScriptHelperResponseLanguage()));
        populateResponseLanguageList();
        setWidget(5, 1, responseLanguage);
        setWidget(6, 0, new Label(i18n.RestServiceScriptHelperSendHeaders()));
        setWidget(6, 1, new Button(i18n.RestServiceScriptHelperAddHeader(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                headerViewPanel.addHeaderRow("", "");
            }
        }));
        setWidget(7, 0, headerViewPanel);
        getFlexCellFormatter().setColSpan(7, 0, 2);
    }
    
    private void populateResponseLanguageList() {
        responseLanguage.addItem("xml");
        responseLanguage.addItem("json");
    }
    
    private void populateResultStatusList() {
        resultStatus.addItem("200 - OK", "200");
        resultStatus.addItem("201 - Created", "201");
        resultStatus.addItem("404 - Not found", "404");
        resultStatus.addItem("500 - Server error", "500");
    }

    private void populateMethodList() {
        method.addItem("GET");
        method.addItem("POST");
        method.addItem("PUT");
        method.addItem("DELETE");
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
        String exportVariableNameValue = this.exportVariableName.getValue();
        String responseLanguageValue = this.responseLanguage.getValue(this.responseLanguage.getSelectedIndex());
        
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("@className", RestServiceScriptHelper.class.getName());
        map.put("urlValue", urlValue);
        map.put("methodValue", methodValue);
        map.put("resultStatusValue", resultStatusValue);
        map.put("resultPathValue", resultPathValue);
        map.put("exportVariableNameValue", exportVariableNameValue);
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
        String exportVariableNameValue = (String) dataMap.get("exportVariableNameValue");
        if (exportVariableNameValue == null) exportVariableNameValue = "";
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
        this.exportVariableName.setValue(exportVariableNameValue);
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
    }

    @Override
    public String asScriptContent() {
        long id = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();
        sb.append("var " + exportVariableName.getValue() + " = null;\n");
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
        sb.append("      " + exportVariableName.getValue() + " = new Array();\n");
        sb.append("      for (var idx = 0; idx < xmlNodeList" + id + ".length; idx++ ) {\n");
        sb.append("         " + exportVariableName.getValue() + "[idx] = xmlNodeList" + id + ".item(idx).text;\n");
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
        return i18n.RestServiceScriptHelperName();
    }

}
