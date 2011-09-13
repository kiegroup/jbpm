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
package org.jbpm.formdisplay.client;

import org.jbpm.formbuilder.client.FormBuilderException;
import org.jbpm.formbuilder.client.JsonLoadInput;
import org.jbpm.formbuilder.client.form.FBForm;
import org.jbpm.formbuilder.shared.form.FormEncodingException;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;

public class FormDisplayEntryPoint implements EntryPoint {

    @Override
    public void onModuleLoad() {
        RootPanel formInfo = RootPanel.get("formInfo");
        RootPanel formDisplay = RootPanel.get("formDisplay");
        String innerJson = formInfo.getElement().getInnerHTML();
        try {
            JsonLoadInput input = JsonLoadInput.parse(innerJson);
            if (input != null && input.getForm() != null) {
                FBForm formUI = new FBForm();
                formUI.populate(input.getForm());
                formDisplay.add(formUI.asFormPanel(input.getFormData()));
            }
        } catch (FormEncodingException e) {
            Window.alert("Couldn't interpretate form: " + e.getMessage());
            GWT.log("Couldn't interpretate form", e);
        } catch (FormBuilderException e) {
            Window.alert("Couldn't populate display: " + e.getMessage());
            GWT.log("Couldn't populate display", e);
        }
    }
}
