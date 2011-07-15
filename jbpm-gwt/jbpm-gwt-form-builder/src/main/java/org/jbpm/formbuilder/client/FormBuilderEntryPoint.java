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

import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;

import com.google.gwt.core.client.EntryPoint;
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
     * 2 - Starts a client service resolver and registers it
     * 3 - Creates a FormBuilderView instance
     * 4 - Creates a FormBuilderController instance
     * 5 - adds the FormBuilderView instance to the main view
     */
    public void onModuleLoad() {
        //register event bus
        EventBus eventBus = new SimpleEventBus();
        FormBuilderGlobals.getInstance().registerEventBus(eventBus);
        //start model
        FormBuilderService server = new FormBuilderModel("fbapi");
        FormBuilderGlobals.getInstance().registerService(server);
        //start view and controller
        FormBuilderView view = new FormBuilderView();
        new FormBuilderController(server, view);
        RootPanel.get("formBuilder").add(view);
    }
}
