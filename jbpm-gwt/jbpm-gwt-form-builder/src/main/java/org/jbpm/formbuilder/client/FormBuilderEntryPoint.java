/**

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
package org.jbpm.formbuilder.client;

import org.jbpm.formbuilder.client.messages.I18NConstants;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Main entry point of the form builder application
 */
public class FormBuilderEntryPoint implements EntryPoint {

    /**
     * Does the following steps to start the app:
     * 1 - Registers an event bus
     * 2-  Registers i18n modules
     * 3 - Starts a client service resolver and registers it
     * 4 - Creates a FormBuilderView instance
     * 5 - Creates a FormBuilderController instance that adds all to the main view
     */
    @Override
    public void onModuleLoad() {
        //register event bus
        EventBus eventBus = new SimpleEventBus();
        FormBuilderGlobals.getInstance().registerEventBus(eventBus);
        //register i18n module
        I18NConstants constants = GWT.create(I18NConstants.class);
        FormBuilderGlobals.getInstance().registerI18n(constants);
        //start model
        FormBuilderModel server = new FormBuilderModel("fbapi");
        FormBuilderGlobals.getInstance().registerService(server);
        //start view and controller
        FormBuilderView view = new FormBuilderView();
        RootPanel rootPanel = RootPanel.get("formBuilder");
        new FormBuilderController(rootPanel, server, view);
    }
}
