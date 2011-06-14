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
package org.jbpm.formbuilder.client.layout;

import java.util.HashMap; 
import java.util.Map;

import org.jbpm.formbuilder.client.bus.FormDataPopulatedEvent;
import org.jbpm.formbuilder.client.bus.FormDataPopulatedEventHandler;
import org.jbpm.formbuilder.client.bus.GetFormRepresentationEvent;
import org.jbpm.formbuilder.client.bus.GetFormRepresentationEventHandler;
import org.jbpm.formbuilder.client.bus.RegisterLayoutEvent;
import org.jbpm.formbuilder.client.bus.RegisterLayoutEventHandler;
import org.jbpm.formbuilder.client.bus.SaveFormRepresentationEvent;
import org.jbpm.formbuilder.client.bus.TaskSelectedEvent;
import org.jbpm.formbuilder.client.bus.TaskSelectedHandler;
import org.jbpm.formbuilder.client.bus.UndoableEvent;
import org.jbpm.formbuilder.client.bus.UndoableEventHandler;
import org.jbpm.formbuilder.client.command.DropFormItemController;
import org.jbpm.formbuilder.client.form.FBForm;
import org.jbpm.formbuilder.client.form.items.LayoutFormItem;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.event.shared.EventBus;

public class LayoutPresenter {

    private final LayoutView layoutView;
    private final EventBus bus;
    
    public LayoutPresenter(LayoutView view) {
        this.layoutView = view;
        this.bus = FormBuilderGlobals.getInstance().getEventBus();
        final PickupDragController dragController = FormBuilderGlobals.getInstance().getDragController();
        dragController.registerDropController(new DropFormItemController(layoutView, layoutView));
        
        this.bus.addHandler(RegisterLayoutEvent.TYPE, new RegisterLayoutEventHandler() {
            public void onEvent(RegisterLayoutEvent event) {
                LayoutFormItem item = event.getLayout();
                dragController.registerDropController(new DropFormItemController(item, layoutView));
            }
        });
        
        this.bus.addHandler(GetFormRepresentationEvent.TYPE, new GetFormRepresentationEventHandler() {
            public void onEvent(GetFormRepresentationEvent event) {
                FBForm formDisplay = layoutView.getFormDisplay();
                FormRepresentation rep = formDisplay.createRepresentation();
                bus.fireEvent(new SaveFormRepresentationEvent(rep, event.getSaveType()));
            }
        });
        
        this.bus.addHandler(FormDataPopulatedEvent.TYPE, new FormDataPopulatedEventHandler() {
            public void onEvent(FormDataPopulatedEvent event) {
                Map<String, Object> dataSnapshot = new HashMap<String, Object>();
                dataSnapshot.put("oldName", layoutView.getFormDisplay().getName());
                dataSnapshot.put("oldAction", layoutView.getFormDisplay().getAction());
                dataSnapshot.put("oldTaskId", layoutView.getFormDisplay().getTaskId());
                dataSnapshot.put("oldMethod", layoutView.getFormDisplay().getMethod());
                dataSnapshot.put("oldEnctype", layoutView.getFormDisplay().getEnctype());
                dataSnapshot.put("newName", event.getName());
                dataSnapshot.put("newAction", event.getAction());
                dataSnapshot.put("newTaskId", event.getTaskId());
                dataSnapshot.put("newMehtod", event.getMethod());
                dataSnapshot.put("newEnctype", event.getEnctype());
                bus.fireEvent(new UndoableEvent(dataSnapshot, new UndoableEventHandler() {
                    public void onEvent(UndoableEvent event) {  }
                    public void undoAction(UndoableEvent event) {
                        String name = (String) event.getData("oldName");
                        String action = (String) event.getData("oldAction");
                        String taskId = (String) event.getData("oldTaskId");
                        String method = (String) event.getData("oldMethod");
                        String enctype = (String) event.getData("oldEnctype");
                        populateFormData(action, taskId, name, method, enctype);
                    }
                    public void doAction(UndoableEvent event) {
                        String name = (String) event.getData("newName");
                        String action = (String) event.getData("newAction");
                        String taskId = (String) event.getData("newTaskId");
                        String method = (String) event.getData("newMethod");
                        String enctype = (String) event.getData("newEnctype");
                        populateFormData(action, taskId, name, method, enctype);
                    }
                }));
            }
        });
        this.bus.addHandler(TaskSelectedEvent.TYPE, new TaskSelectedHandler() {
            public void onSelectedTask(TaskSelectedEvent event) {
                Map<String, Object> dataSnapshot = new HashMap<String, Object>();
                dataSnapshot.put("oldTaskID", layoutView.getFormDisplay().getTaskId());
                dataSnapshot.put("newTaskID", event.getSelectedTask() == null ? null : event.getSelectedTask().getTaskId());
                bus.fireEvent(new UndoableEvent(dataSnapshot, new UndoableEventHandler() {
                    public void onEvent(UndoableEvent event) { }
                    public void doAction(UndoableEvent event) {
                        String value = (String) event.getData("newTaskID");
                        layoutView.getFormDisplay().setTaskId(value);
                    }
                    public void undoAction(UndoableEvent event) {
                        String value = (String) event.getData("oldTaskID");
                        layoutView.getFormDisplay().setTaskId(value);
                    }
                }));
            }
        });
    }

    private void populateFormData(String action, String taskId,
            String name, String method, String enctype) {
        if (action != null && !"".equals(action)) {
            layoutView.getFormDisplay().setAction(action);
        }
        if (taskId != null && !"".equals(taskId)) {
            layoutView.getFormDisplay().setTaskId(taskId);
        }
        if (name != null && !"".equals(name)) {
            layoutView.getFormDisplay().setName(name);
        }
        layoutView.getFormDisplay().setMethod(method);
        layoutView.getFormDisplay().setEnctype(enctype);
    }
}
