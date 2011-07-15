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
package org.jbpm.formbuilder.client.options;

import java.util.List;

import org.jbpm.formbuilder.client.bus.MenuOptionAddedEvent;
import org.jbpm.formbuilder.client.bus.MenuOptionAddedHandler;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;

import com.google.gwt.event.shared.EventBus;

/**
 * Options presenter. Hears valid options from
 * the server and populates the view
 */
public class OptionsPresenter {

    private final OptionsView view;
    private final EventBus bus;
    
    public OptionsPresenter(List<MainMenuOption> menuOptions, OptionsView view) {
        this.view = view;
        this.bus = FormBuilderGlobals.getInstance().getEventBus();
        this.view.addItems(menuOptions);
        
        bus.addHandler(MenuOptionAddedEvent.TYPE, new MenuOptionAddedHandler() {
            public void onEvent(MenuOptionAddedEvent event) {
                OptionsPresenter.this.view.addItem(event.getOption());
            }
        });
    }
}
