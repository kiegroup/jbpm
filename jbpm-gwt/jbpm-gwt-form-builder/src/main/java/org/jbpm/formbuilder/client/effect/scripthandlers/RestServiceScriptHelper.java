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

import java.util.Map;

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
public class RestServiceScriptHelper extends FlexTable implements FBScriptHelper {

    private final TextBox url = new TextBox();
    private final ListBox method = new ListBox();
    private final ListBox resultStatus = new ListBox();
    private final TextBox resultXPath = new TextBox();
    private final TextBox exportVariableName = new TextBox();
    private final ListBox responseLanguage = new ListBox();
    
    private final HeaderViewPanel headerViewPanel = new HeaderViewPanel();
    
    public RestServiceScriptHelper() {
        super();
        setWidget(0, 0, new Label("URL:")); //TODO i18n
        setWidget(0, 1, url);
        setWidget(1, 0, new Label("Method:")); //TODO i18n
        populateMethodList();
        setWidget(1, 1, method);
        setWidget(2, 0, new Label("Result status:")); //TODO i18n
        populateResultStatusList();
        setWidget(2, 1, resultStatus);
        setWidget(3, 0, new Label("Result path to obtain results:")); //TODO i18n
        setWidget(3, 1, resultXPath);
        setWidget(4, 0, new Label("Export data to variable:")); //TODO i18n
        setWidget(4, 1, exportVariableName);
        setWidget(5, 0, new Label("Response language:")); //TODO i18n
        populateResponseLanguageList();
        setWidget(5, 1, responseLanguage);
        setWidget(6, 0, new Label("Send headers:")); //TODO i18n
        setWidget(6, 1, new Button("Add header", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                headerViewPanel.addHeaderRow();
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
        script.setHelper(this);
    }
    
    @Override
    public Map<String, Object> getDataMap() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setDataMap(Map<String, Object> dataMap) throws FormEncodingException {
        // TODO Auto-generated method stub

    }

    @Override
    public String asScriptContent() {
        long id = System.currentTimeMillis();
        Object[] nullargs = new Object[] {};
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("var %s = null;\n", exportVariableName.getValue()));
        sb.append(String.format("var url%d = \"%s\";\n", id, url.getValue()));
        sb.append(String.format("var method%d = \"%s\";\n", id, method.getValue(method.getSelectedIndex())));
        sb.append(String.format("var xmlhttp%d;\n", id));
        sb.append(String.format("if (window.XMLHttpRequest) {// code for IE7+, Firefox, Chrome, Opera, Safari\n", nullargs));
        sb.append(String.format("   xmlhttp%d=new XMLHttpRequest();\n", id));
        sb.append(String.format("} else {// code for IE6, IE5\n", nullargs));
        sb.append(String.format("   xmlhttp%d=new ActiveXObject(\"Microsoft.XMLHTTP\");\n", id));
        sb.append(String.format("}\n", nullargs));
        sb.append(String.format("xmlhttp%d.onreadystatechange=function() {\n", id));
        sb.append(String.format("   if (xmlhttp%d.readyState==4 && xmlhttp%d.status==%s) {\n", id, id, resultStatus.getValue(resultStatus.getSelectedIndex())));  
        sb.append(String.format("      var xmlDoc%d = null;\n", id));
        sb.append(String.format("      if (window.ActiveXObject) { // code for IE\n", nullargs));
        sb.append(String.format("         xmlDoc%d=new ActiveXObject(\"Microsoft.XMLDOM\");\n", id));
        sb.append(String.format("         xmlDoc%d.write(xmlhttp%d.responseText);\n", id, id));
        sb.append(String.format("      } else if (document.implementation && document.implementation.createDocument) { // code for Mozilla, Firefox, Opera, etc.\n", nullargs));
        sb.append(String.format("         xmlDoc%d=document.implementation.createDocument(\"\",\"\",null);\n", id));
        sb.append(String.format("         xmlDoc%d.write(xmlhttp%d.responseText);\n", id, id));
        sb.append(String.format("      } else {\n", nullargs));
        sb.append(String.format("         alert('Your browser cannot handle this script');\n", nullargs));
        sb.append(String.format("      }\n", nullargs));
        sb.append(String.format("      var xmlNodeList%d = xmlDoc%d.selectNodes(\"%s\");\n", id, id, resultXPath.getValue()));
        sb.append(String.format("      %s = new Array();\n", exportVariableName.getValue()));
        sb.append(String.format("      for (var idx = 0; idx < xmlNodeList%d.length; idx++ ) {\n", id));
        sb.append(String.format("         %s[idx] = xmlNodeList%d.item(idx).text;\n", exportVariableName.getValue(), id));
        sb.append(String.format("      }", nullargs));
        sb.append(String.format("   }\n", nullargs));
        sb.append(String.format("}\n", nullargs));
        for (Map.Entry<String, String> header : headerViewPanel.getHeaders()) {
            sb.append(String.format("xmlhttp%d.setRequestHeader(\"%s\",\"%s\");\n", id, header.getKey(), header.getValue()));
        }
        sb.append(String.format("xmlhttp%d.open(method%d, url%d, true);\n", id, id, id));
        sb.append(String.format("xmlhttp%d.send();\n", id));
        return sb.toString();
    }

    @Override
    public Widget draw() {
        return this;
    }

    @Override
    public String getName() {
        return "rest service"; //TODO i18n
    }

}
