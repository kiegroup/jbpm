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

import org.jbpm.formbuilder.client.bus.FormDataPopulatedEvent;
import org.jbpm.formbuilder.client.bus.FormDataPopulatedEventHandler;
import org.jbpm.formbuilder.client.bus.GetFormRepresentationEvent;
import org.jbpm.formbuilder.client.bus.GetFormRepresentationEventHandler;
import org.jbpm.formbuilder.client.bus.RegisterLayoutEvent;
import org.jbpm.formbuilder.client.bus.RegisterLayoutEventHandler;
import org.jbpm.formbuilder.client.bus.SaveFormRepresentationEvent;
import org.jbpm.formbuilder.client.command.DropFormItemController;
import org.jbpm.formbuilder.client.form.FBForm;
import org.jbpm.formbuilder.client.form.LayoutFormItem;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.event.shared.EventBus;

public class LayoutController {

    private final FormBuilderModel model;
    private final LayoutView layoutView;
    private final EventBus bus;
    
    public LayoutController(FormBuilderModel model, LayoutView layoutView) {
        this.model = model;
        this.layoutView = layoutView;
        this.bus = FormBuilderGlobals.getInstance().getEventBus();
        final PickupDragController dragController = FormBuilderGlobals.getInstance().getDragController();
        dragController.registerDropController(new DropFormItemController(layoutView, layoutView));
        
        this.bus.addHandler(RegisterLayoutEvent.TYPE, new RegisterLayoutEventHandler() {
            public void onEvent(RegisterLayoutEvent event) {
                LayoutFormItem item = event.getLayout();
                dragController.registerDropController(new DropFormItemController(item, LayoutController.this.layoutView));
            }
        });
        
        this.bus.addHandler(GetFormRepresentationEvent.TYPE, new GetFormRepresentationEventHandler() {
            public void onEvent(GetFormRepresentationEvent event) {
                FBForm formDisplay = LayoutController.this.layoutView.getFormDisplay();
                FormRepresentation rep = formDisplay.createRepresentation();
                bus.fireEvent(new SaveFormRepresentationEvent(rep, event.getSaveType()));
            }
        });
        
        this.bus.addHandler(FormDataPopulatedEvent.TYPE, new FormDataPopulatedEventHandler() {
            public void onEvent(FormDataPopulatedEvent event) {
                String action = event.getAction();
                String taskId = event.getTaskId();
                String name = event.getName();
                if (action != null && !"".equals(action)) {
                    LayoutController.this.layoutView.getFormDisplay().setAction(action);
                }
                if (taskId != null && !"".equals(taskId)) {
                    LayoutController.this.layoutView.getFormDisplay().setTaskId(taskId);
                }
                if (name != null && !"".equals(name)) {
                    LayoutController.this.layoutView.getFormDisplay().setName(name);
                }
                LayoutController.this.layoutView.getFormDisplay().setMethod(event.getMethod());
                LayoutController.this.layoutView.getFormDisplay().setEnctype(event.getEnctype());
                
            }
        });

    }

    /*
     * if at the given position, a layout exists, then add the 
     * LayoutHolder component to that layout position. If it doesn't, 
     * then wherever it fits
     */
    private LayoutHolder createLayoutHolder(int x, int y) {
        return new LayoutHolder(); // TODO wherever it fits on current version
    }

}
