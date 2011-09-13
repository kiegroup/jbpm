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

import org.jbpm.formbuilder.client.RestyFormBuilderModel;
import org.jbpm.formbuilder.client.messages.I18NConstants;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.RootPanel;

public class FormDisplayEntryPoint implements EntryPoint {

    @Override
    public void onModuleLoad() {
      //register event bus
        FormBuilderGlobals.getInstance().registerEventBus(new SimpleEventBus());
        //register i18n module
        I18NConstants constants = GWT.create(I18NConstants.class);
        FormBuilderGlobals.getInstance().registerI18n(constants);
        //start model
        RestyFormBuilderModel server = new RestyFormBuilderModel("rest");
        FormBuilderGlobals.getInstance().registerService(server);
        
        //start view and controller
        RootPanel formInfo = RootPanel.get("formInfo");
        RootPanel formDisplay = RootPanel.get("formDisplay");
        new FormDisplayController(formInfo, formDisplay);
        
        
    }
}
