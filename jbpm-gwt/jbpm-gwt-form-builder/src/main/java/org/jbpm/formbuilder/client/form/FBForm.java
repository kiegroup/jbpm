package org.jbpm.formbuilder.client.form;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.jbpm.formbuilder.client.FormBuilderException;
import org.jbpm.formbuilder.client.bus.ui.FormItemAddedEvent;
import org.jbpm.formbuilder.client.bus.ui.FormItemRemovedEvent;
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
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

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
            EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
            FBFormItem item = (FBFormItem) w;
            this.formItems.remove(item);
            bus.fireEvent(new FormItemRemovedEvent(item));
        }
        return super.remove(w);
    }
    
    @Override
    public void add(Widget w) {
        if (w instanceof FBFormItem) {
            EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
            FBFormItem formItem = (FBFormItem) w;
            int index = getItemPosition(formItem);
            if (index == getWidgetCount()) {
                this.formItems.add(formItem);
                super.add(w);
            } else {
                this.formItems.set(index, formItem);
                super.insert(w, index);
            }
            bus.fireEvent(new FormItemAddedEvent(formItem, this));
        } else {
            super.add(w);
        }
    }
    
    protected int getItemPosition(FBFormItem newItem) {
        int index = getWidgetCount();
        if (index == 0) {
            return index;
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
        rep.setInputs(inputs);
        rep.setOutputs(outputs);
        /* TODO setOnLoadScript(rep.getOnLoadScript());
        setOnSubmitScript(rep.getOnSubmitScript()); */
    }
}
