package org.jbpm.formbuilder.client.form;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.formbuilder.client.menu.FormDataPopupPanel;
import org.jbpm.formbuilder.client.validation.FBValidationItem;
import org.jbpm.formbuilder.common.handler.RightClickEvent;
import org.jbpm.formbuilder.common.handler.RightClickHandler;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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
    
    private List<FBFormItem> formItems = new ArrayList<FBFormItem>();
    private List<FBValidationItem> validationItems = new ArrayList<FBValidationItem>();
    
    private List<RightClickHandler> rclickHandlers = new ArrayList<RightClickHandler>();
    private List<ClickHandler> clickHandlers = new ArrayList<ClickHandler>();
    
    private final FormDataPopupPanel popup = new FormDataPopupPanel(this);
    
    public FBForm() {
        super();
        sinkEvents(Event.ONMOUSEUP | Event.ONDBLCLICK | Event.ONCONTEXTMENU);
        addRightClickHandler(new RightClickHandler() {
            public void onRightClick(RightClickEvent event) {
                popup.setPopupPosition(event.getX(), event.getY());
                popup.show();
            }
        });
    }
    
    @Override
    public void onBrowserEvent(Event event) {
      event.stopPropagation();
      event.preventDefault();
      switch (DOM.eventGetType(event)) {
        case Event.ONMOUSEUP:
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
          break;
        case Event.ONCONTEXTMENU:
          break;
        default:
          break; // Do nothing
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
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    
    public String getEnctype() {
        return enctype;
    }
    
    public void setEnctype(String enctype) {
        this.enctype = enctype;
    }
    
    public String getMethod() {
        return method;
    }
    
    public void setMethod(String method) {
        this.method = method;
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
            this.formItems.remove((FBFormItem) w);
        }
        return super.remove(w);
    }
    
    @Override
    public void add(Widget w) {
        super.add(w);
        if (w instanceof FBFormItem) {
            this.formItems.add((FBFormItem) w);
        }
    }
    
    public void addValidation(FBValidationItem item) {
        this.validationItems.add(item);
    }

    public void onFormLoad() {
        
    }
    
    public void onFormSubmit() {
        
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
            rep.addFormValidation(item.getRepresentation());
        }
        /* TODO rep.setInputs(inputs);
        rep.setOutputs(outputs);
        rep.setOnLoadScript(onLoadScript);
        rep.setOnSubmitScript(onSubmitScript); */
        return rep;
    }
}
