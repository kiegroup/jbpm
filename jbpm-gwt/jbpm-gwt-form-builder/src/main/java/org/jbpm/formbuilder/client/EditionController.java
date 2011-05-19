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

import org.jbpm.formbuilder.client.bus.FormItemDeselectedEvent;
import org.jbpm.formbuilder.client.bus.FormItemDeselectedEventHandler;
import org.jbpm.formbuilder.client.bus.FormItemSelectedEvent;
import org.jbpm.formbuilder.client.bus.FormItemSelectedEventHandler;

import com.google.gwt.event.shared.EventBus;

public class EditionController {

    private final EditionModel editModel;
    private final EditionView editView;
    private final EventBus bus;
    
    public EditionController(EditionModel editModel,
            EditionView editView, EventBus bus) {
        super();
        this.editModel = editModel;
        this.editView = editView;
        this.bus = bus;
        
        bus.addHandler(FormItemSelectedEvent.TYPE, new FormItemSelectedEventHandler() {
            public void onEvent(FormItemSelectedEvent event) {
                EditionController.this.editView.populate(event.getFormItemSelected());
            }
        });
        
        bus.addHandler(FormItemDeselectedEvent.TYPE, new FormItemDeselectedEventHandler() {
            public void onEvent(FormItemDeselectedEvent event) {
                EditionController.this.editView.clear();
            }
        });
    }
}
