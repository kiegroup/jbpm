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
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.FormBuilderException;
import org.jbpm.formbuilder.client.bus.FormDataPopulatedEvent;
import org.jbpm.formbuilder.client.bus.FormDataPopulatedHandler;
import org.jbpm.formbuilder.client.bus.GetFormRepresentationEvent;
import org.jbpm.formbuilder.client.bus.GetFormRepresentationHandler;
import org.jbpm.formbuilder.client.bus.GetFormRepresentationResponseEvent;
import org.jbpm.formbuilder.client.bus.RegisterLayoutEvent;
import org.jbpm.formbuilder.client.bus.RegisterLayoutHandler;
import org.jbpm.formbuilder.client.bus.UndoableEvent;
import org.jbpm.formbuilder.client.bus.UndoableHandler;
import org.jbpm.formbuilder.client.bus.ui.FormSavedEvent;
import org.jbpm.formbuilder.client.bus.ui.FormSavedHandler;
import org.jbpm.formbuilder.client.bus.ui.GetFormDisplayEvent;
import org.jbpm.formbuilder.client.bus.ui.GetFormDisplayHandler;
import org.jbpm.formbuilder.client.bus.ui.NotificationEvent;
import org.jbpm.formbuilder.client.bus.ui.NotificationEvent.Level;
import org.jbpm.formbuilder.client.bus.ui.TaskSelectedEvent;
import org.jbpm.formbuilder.client.bus.ui.TaskSelectedHandler;
import org.jbpm.formbuilder.client.bus.ui.UpdateFormViewEvent;
import org.jbpm.formbuilder.client.bus.ui.UpdateFormViewHandler;
import org.jbpm.formbuilder.client.command.DropFormItemController;
import org.jbpm.formbuilder.client.form.FBForm;
import org.jbpm.formbuilder.client.form.items.LayoutFormItem;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;
import org.jbpm.formbuilder.shared.rep.InputData;
import org.jbpm.formbuilder.shared.rep.OutputData;
import org.jbpm.formbuilder.shared.task.TaskPropertyRef;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.event.shared.EventBus;

/**
 * layout presenter.
 * 
 * Registers the dropController to work on it when
 * started and when layout form items are added, to 
 * work on said layout form items.
 * 
 * Exposes the form representation and display, and
 * populates both when they are loaded from the server,
 * changed by another view or saved.
 */
public class LayoutPresenter {

    private final LayoutView layoutView;
    private final EventBus bus;
    
    public LayoutPresenter(LayoutView view) {
        this.layoutView = view;
        this.bus = FormBuilderGlobals.getInstance().getEventBus();
        final PickupDragController dragController = FormBuilderGlobals.getInstance().getDragController();
        dragController.registerDropController(new DropFormItemController(layoutView, layoutView));
        
        this.bus.addHandler(RegisterLayoutEvent.TYPE, new RegisterLayoutHandler() {
            public void onEvent(RegisterLayoutEvent event) {
                LayoutFormItem item = event.getLayout();
                dragController.registerDropController(new DropFormItemController(item, layoutView));
            }
        });
        
        this.bus.addHandler(GetFormRepresentationEvent.TYPE, new GetFormRepresentationHandler() {
            public void onEvent(GetFormRepresentationEvent event) {
                FBForm formDisplay = layoutView.getFormDisplay();
                FormRepresentation rep = formDisplay.createRepresentation();
                bus.fireEvent(new GetFormRepresentationResponseEvent(rep, event.getSaveType()));
            }
        });
        
        this.bus.addHandler(GetFormDisplayEvent.TYPE, new GetFormDisplayHandler() {
            public void onEvent(GetFormDisplayEvent event) {
                event.setFormDisplay(layoutView.getFormDisplay());
            }
        });
        
        this.bus.addHandler(FormDataPopulatedEvent.TYPE, new FormDataPopulatedHandler() {
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
                bus.fireEvent(new UndoableEvent(dataSnapshot, new UndoableHandler() {
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
                dataSnapshot.put("oldTaskInputs", layoutView.getFormDisplay().getInputs());
                dataSnapshot.put("oldTaskOutputs", layoutView.getFormDisplay().getOutputs());
                if (event.getSelectedTask() != null) {
                    dataSnapshot.put("newTaskID", event.getSelectedTask().getTaskId());
                    Map<String, InputData> inputs = new HashMap<String, InputData>();
                    Map<String, OutputData> outputs = new HashMap<String, OutputData>();
                    if (event.getSelectedTask().getInputs() != null) {
                        for (TaskPropertyRef input : event.getSelectedTask().getInputs()) {
                            InputData in = new InputData();
                            in.setName(input.getName());
                            in.setValue(input.getSourceExpresion());
                            inputs.put(input.getName(), in);
                        }
                    }
                    if (event.getSelectedTask().getOutputs() != null) {
                        for (TaskPropertyRef output : event.getSelectedTask().getOutputs()) {
                            OutputData out = new OutputData();
                            out.setName(output.getName());
                            out.setValue(output.getSourceExpresion());
                            outputs.put(output.getName(), out);
                        }
                    }
                    dataSnapshot.put("newTaskInputs", inputs);
                    dataSnapshot.put("newTaskOutputs", outputs);
                }
                dataSnapshot.put("newTaskID", event.getSelectedTask() == null ? null : event.getSelectedTask().getTaskId());
                dataSnapshot.put("newTaskInputs", event.getSelectedTask() == null ? null : event.getSelectedTask().getInputs());
                dataSnapshot.put("newTaskOutputs", event.getSelectedTask() == null ? null : event.getSelectedTask().getOutputs());
                bus.fireEvent(new UndoableEvent(dataSnapshot, new UndoableHandler() {
                    public void onEvent(UndoableEvent event) { }
                    @SuppressWarnings("unchecked")
                    public void doAction(UndoableEvent event) {
                        String value = (String) event.getData("newTaskID");
                        List<TaskPropertyRef> inputs = (List<TaskPropertyRef>) event.getData("newTaskInputs");
                        List<TaskPropertyRef> outputs = (List<TaskPropertyRef>) event.getData("newTaskOutputs");
                        layoutView.getFormDisplay().setTaskId(value);
                        layoutView.getFormDisplay().setInputs(toInputs(inputs));
                        layoutView.getFormDisplay().setOutputs(toOutputs(outputs));
                    }
                    @SuppressWarnings("unchecked")
                    public void undoAction(UndoableEvent event) {
                        String value = (String) event.getData("oldTaskID");
                        List<TaskPropertyRef> inputs = (List<TaskPropertyRef>) event.getData("oldTaskInputs");
                        List<TaskPropertyRef> outputs = (List<TaskPropertyRef>) event.getData("oldTaskOutputs");
                        layoutView.getFormDisplay().setTaskId(value);
                        layoutView.getFormDisplay().setInputs(toInputs(inputs));
                        layoutView.getFormDisplay().setOutputs(toOutputs(outputs));
                    }
                }));
            }
        });
        
        bus.addHandler(FormSavedEvent.TYPE, new FormSavedHandler() {
            public void onEvent(FormSavedEvent event) {
                layoutView.getFormDisplay().setSaved(true);
            }
        });
        
        bus.addHandler(UpdateFormViewEvent.TYPE, new UpdateFormViewHandler() {
            public void onEvent(UpdateFormViewEvent event) {
                FormRepresentation form = event.getFormRepresentation();
                try {
                    layoutView.getFormDisplay().populate(form);
                } catch (FormBuilderException e) {
                    bus.fireEvent(new NotificationEvent(Level.ERROR, "Couldn't populate screen with form data", e));
                }
            }
        });
    }

    private Map<String, InputData> toInputs(List<TaskPropertyRef> inputs) {
        Map<String, InputData> retval = new HashMap<String, InputData>();
        if (inputs != null) {
            for (TaskPropertyRef ref : inputs) {
                InputData input = new InputData();
                input.setName(ref.getName());
                input.setValue(ref.getSourceExpresion());
                retval.put(ref.getName(), input);
            }
        }
        return retval;
    }
    
    private Map<String, OutputData> toOutputs(List<TaskPropertyRef> outputs) {
        Map<String, OutputData> retval = new HashMap<String, OutputData>();
        if (outputs != null) {
            for (TaskPropertyRef ref : outputs) {
                OutputData output = new OutputData();
                output.setName(ref.getName());
                output.setValue(ref.getSourceExpresion());
                retval.put(ref.getName(), output);
            }
        }
        return retval;
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
