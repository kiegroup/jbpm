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
package org.jbpm.formbuilder.client.form;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.jbpm.formbuilder.client.FormBuilderException;
import org.jbpm.formbuilder.client.menu.FormDataPopupPanel;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.client.validation.FBValidationItem;
import org.jbpm.formbuilder.common.handler.RightClickEvent;
import org.jbpm.formbuilder.common.handler.RightClickHandler;
import org.jbpm.formbuilder.shared.rep.FBValidation;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;
import org.jbpm.formbuilder.shared.rep.InputData;
import org.jbpm.formbuilder.shared.rep.OutputData;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Display class for a {@link FormRepresentation}
 */
public class FBForm extends FlowPanel implements FBCompositeItem {

    private String name;
    private String taskId;
    private String method;
    private String enctype;
    private String action;
    private Map<String, InputData> inputs;
    private Map<String, OutputData> outputs;
    
    private List<FBFormItem> formItems = new ArrayList<FBFormItem>();
    private List<FBValidationItem> validationItems = new ArrayList<FBValidationItem>();
    
    private List<RightClickHandler> rclickHandlers = new ArrayList<RightClickHandler>();
    private List<ClickHandler> clickHandlers = new ArrayList<ClickHandler>();
    
    private final FormDataPopupPanel popup = new FormDataPopupPanel();
    
    private boolean saved = false;
    private long lastModified = 0L;
    
    public FBForm() {
        super();
        sinkEvents(Event.ONMOUSEUP | Event.ONDBLCLICK | Event.ONCONTEXTMENU | Event.ONKEYPRESS);
        addRightClickHandler(new RightClickHandler() {
            public void onRightClick(RightClickEvent event) {
                popup.setPopupPosition(event.getX(), event.getY());
                popup.show();
            }
        });
    }
    
    @Override
    public void onBrowserEvent(Event event) {
        switch (DOM.eventGetType(event)) {
        case Event.ONMOUSEUP:
            event.stopPropagation();
            event.preventDefault();
            if (DOM.eventGetButton(event) == Event.BUTTON_LEFT) {
                for (ClickHandler handler : clickHandlers) {
                    ClickEvent cevent = new ClickEvent() {
                        @Override
                        public Object getSource() {
                            return FBForm.this;
                        }
                    };
                    cevent.setNativeEvent(event);
                    handler.onClick(cevent);
                }
                super.onBrowserEvent(event);
            } else if (DOM.eventGetButton(event) == Event.BUTTON_RIGHT) {
                for (RightClickHandler handler : rclickHandlers) {
                    handler.onRightClick(new RightClickEvent(event));
                }
            }
            break;
        case Event.ONDBLCLICK:
            event.stopPropagation();
            event.preventDefault();
            break;
        case Event.ONCONTEXTMENU:
            event.stopPropagation();
            event.preventDefault();
            break;
        case Event.ONKEYPRESS:
            if (event.getCtrlKey()) {
                event.stopPropagation();
                event.preventDefault();
                switch (event.getCharCode()) {
                case 'v': case 'V': //paste
                    FormBuilderGlobals.getInstance().paste().append(null).execute();
                    break;
                default: 
                    super.onBrowserEvent(event);
                }
            } else {
                super.onBrowserEvent(event);
            }
            break;
        default:
            //Do nothing
        }//end switch
    }

    public HandlerRegistration addRightClickHandler(final RightClickHandler handler) {
        HandlerRegistration reg = new HandlerRegistration() {
            public void removeHandler() {
                FBForm.this.rclickHandlers.remove(handler);
            }
        };
        this.rclickHandlers.add(handler);
        return reg;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.popup.setName(name);
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
        this.popup.setTaskId(taskId);
    }
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
        this.popup.setAction(action);
    }
    
    public String getEnctype() {
        return enctype;
    }
    
    public void setEnctype(String enctype) {
        this.enctype = enctype;
        this.popup.setEnctype(enctype);
    }
    
    public String getMethod() {
        return method;
    }
    
    public void setMethod(String method) {
        this.method = method;
        this.popup.setMethod(method);
    }
    
    public List<FBFormItem> getItems() {
        return formItems;
    }

    public void setItems(List<FBFormItem> items) {
        this.formItems = items;
    }

    public List<FBValidationItem> getValidationItems() {
        return validationItems;
    }

    public void setValidationItems(List<FBValidationItem> validationItems) {
        this.validationItems = validationItems;
    }
    
    @Override
    public boolean remove(Widget w) {
        if (w instanceof FBFormItem) {
            FBFormItem item = (FBFormItem) w;
            this.formItems.remove(item);
        }
        return super.remove(w);
    }
    
    @Override
    public void add(Widget w) {
        if (w instanceof FBFormItem) {
            FBFormItem formItem = (FBFormItem) w;
            int index = getItemPosition(formItem);
            if (index == getWidgetCount()) {
                this.formItems.add(formItem);
                super.add(w);
            } else {
                this.formItems.set(index, formItem);
                super.insert(w, index);
            }
        } else {
            super.add(w);
        }
    }
    
    protected int getItemPosition(FBFormItem newItem) {
        int index = getWidgetCount();
        if (index == 0) {
            return index;
        }
        if (this.formItems.size() == 0) {
            return 0;
        }
        ListIterator<FBFormItem> it = this.formItems.listIterator(index - 1);
        while (it.hasPrevious() && index > 0) {
            FBFormItem item = it.previous();
            boolean leftOfItem = item.getAbsoluteLeft() > newItem.getDesiredX();
            boolean aboveItem = item.getAbsoluteTop() > newItem.getDesiredY();
            if (aboveItem || leftOfItem) {
                index--;
            }
        }
        return index;
    }
    
    public void addValidation(FBValidationItem item) {
        this.validationItems.add(item);
    }

    public void onFormLoad() {
        
    }
    
    public void onFormSubmit() {
        
    }
    
    public void setInputs(Map<String, InputData> inputs) {
        this.inputs = inputs;
    }
    
    public Map<String, InputData> getInputs() {
        return inputs;
    }
    
    public void setOutputs(Map<String, OutputData> outputs) {
        this.outputs = outputs;
    }
    
    public Map<String, OutputData> getOutputs() {
        return outputs;
    }
    
    public void setSaved(boolean saved) {
        this.saved = saved;
        this.lastModified = System.currentTimeMillis();
    }
    
    public FormRepresentation createRepresentation() {
        FormRepresentation rep = new FormRepresentation();
        rep.setName(name);
        rep.setTaskId(taskId);
        rep.setAction(action);
        rep.setMethod(method);
        rep.setEnctype(enctype);
        for (FBFormItem item : formItems) {
            rep.addFormItem(item.getRepresentation());
        }
        for (FBValidationItem item : validationItems) {
            rep.addFormValidation(item.createValidation());
        }
        rep.setInputs(inputs);
        rep.setOutputs(outputs);
        rep.setSaved(saved);
        rep.setLastModified(lastModified);
        /* TODO rep.setOnLoadScript(onLoadScript);
        rep.setOnSubmitScript(onSubmitScript); */
        return rep;
    }

    public void populate(FormRepresentation rep) throws FormBuilderException {
        setName(rep.getName());
        setTaskId(rep.getTaskId());
        setAction(rep.getAction());
        setMethod(rep.getMethod());
        setEnctype(rep.getEnctype());
        for (FBFormItem item : new ArrayList<FBFormItem>(formItems)) {
            item.removeFromParent();
        }
        
        for (FormItemRepresentation itemRep : rep.getFormItems()) {
            FBFormItem item = FBFormItem.createItem(itemRep);
            add(item);
        }
        for (FBValidation validationRep : rep.getFormValidations()) {
            FBValidationItem validation = FBValidationItem.createValidation(validationRep);
            addValidation(validation);
        }
        setInputs(rep.getInputs());
        setOutputs(rep.getOutputs());
        this.saved = rep.isSaved();
        this.lastModified = rep.getLastModified();
        /* TODO setOnLoadScript(rep.getOnLoadScript());
        setOnSubmitScript(rep.getOnSubmitScript()); */
    }
    
    public void addPhantom(int x, int y) {
        PhantomPanel phantom = new PhantomPanel();
        phantom.selfInsert(this, x, y, getItems());
    }
    
    public int clearPhantom() {
        return PhantomPanel.selfClear(this);
    }
}
