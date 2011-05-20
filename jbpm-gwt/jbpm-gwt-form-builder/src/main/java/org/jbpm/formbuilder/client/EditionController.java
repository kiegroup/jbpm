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

import org.jbpm.formbuilder.client.bus.FormItemSelectionEvent;
import org.jbpm.formbuilder.client.bus.FormItemSelectionEventHandler;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.event.shared.EventBus;

public class EditionController {

    private final FormBuilderModel model;
    private final EditionView editView;
    private final EventBus bus;
    
    public EditionController(FormBuilderModel model, EditionView editView) {
        super();
        this.model = model;
        this.editView = editView;
        this.bus = FormBuilderGlobals.getInstance().getEventBus();
        PickupDragController dragController = FormBuilderGlobals.getInstance().getDragController();
        dragController.registerDropController(new DisposeDropController(this.editView));
        
        bus.addHandler(FormItemSelectionEvent.TYPE, new FormItemSelectionEventHandler() {
            public void onEvent(FormItemSelectionEvent event) {
                if (event.isSelected()) {
                    EditionController.this.editView.populate(event.getFormItemSelected());
                } else {
                    EditionController.this.editView.clear();
                }
            }
        });
    }
}
