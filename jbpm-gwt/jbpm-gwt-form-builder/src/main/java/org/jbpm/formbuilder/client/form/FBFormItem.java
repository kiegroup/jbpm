package org.jbpm.formbuilder.client.form;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.FormBuilderException;
import org.jbpm.formbuilder.client.bus.FormItemSelectionEvent;
import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.menu.EffectsPopupPanel;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;
import org.jbpm.formbuilder.client.validation.FBValidationItem;
import org.jbpm.formbuilder.common.handler.RightClickEvent;
import org.jbpm.formbuilder.common.handler.RightClickHandler;
import org.jbpm.formbuilder.shared.rep.FBValidation;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.InputData;
import org.jbpm.formbuilder.shared.rep.OutputData;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.rpc.impl.ReflectionHelper;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

public abstract class FBFormItem extends FocusPanel {

    private List<FBValidationItem> validations = new ArrayList<FBValidationItem>();
    
    private List<RightClickHandler> rclickHandlers = new ArrayList<RightClickHandler>();
    private List<ClickHandler> clickHandlers = new ArrayList<ClickHandler>();
    private List<FBFormEffect> effects = new ArrayList<FBFormEffect>();
    
    private int desiredX;
    private int desiredY;
    
    private String widgetWidth;
    private String widgetHeight;
    
    private boolean alreadyEditing = false;
    private Widget auxiliarWidget = null;
    
    private InputData input = null;
    private OutputData output = null;
    
    public FBFormItem(List<FBFormEffect> formEffects) {
        this.effects.addAll(formEffects);
        addStyleName("fbFormItemThinBorder");
        addFocusHandler(new FocusHandler() {
            public void onFocus(FocusEvent event) {
                makeEditor();
            }
        });
        sinkEvents(Event.ONMOUSEUP | Event.ONDBLCLICK | Event.ONCONTEXTMENU);
        addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                makeEditor();
            }
        });
        addRightClickHandler(new RightClickHandler() {
            public void onRightClick(RightClickEvent event) {
                EffectsPopupPanel popupPanel = new EffectsPopupPanel(FBFormItem.this, true);
                if (getFormEffects() != null && !getFormEffects().isEmpty()) {
                    popupPanel.setPopupPosition(event.getX(), event.getY());
                    popupPanel.show();
                }
            }
        });
    } 
    
    private void makeEditor() {
        if (!getFormItemPropertiesMap().isEmpty() && !isAlreadyEditing()) {
            fireSelectionEvent(new FormItemSelectionEvent(this, true));
        }
        Widget inplaceEditor = createInplaceEditor();
        if (inplaceEditor != null && !isAlreadyEditing()) {
            auxiliarWidget = getWidget();
            clear();
            setWidget(inplaceEditor);
            setAlreadyEditing(true);
        }
    }
    
    public boolean isAlreadyEditing() {
        return alreadyEditing;
    }

    public void setAlreadyEditing(boolean alreadyEditing) {
        this.alreadyEditing = alreadyEditing;
    }

    public void reset() {
        if (auxiliarWidget != null) {
            clear();
            add(auxiliarWidget);
            setAlreadyEditing(false);
            fireSelectionEvent(new FormItemSelectionEvent(null, false));
        }
    }
    
    public final void fireSelectionEvent(FormItemSelectionEvent event) {
        EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
        bus.fireEvent(event);
    }

    public Widget createInplaceEditor() {
        return null;
    }
    
    public abstract Map<String, Object> getFormItemPropertiesMap();
    
    protected Image createDoneImage(ClickHandler handler) {
        final Image done = new Image(FormBuilderResources.INSTANCE.doneIcon());
        done.addClickHandler(handler);
        return done;
    }
    
    protected Image createRemoveImage() {
        final Image remove = new Image(FormBuilderResources.INSTANCE.removeIcon());
        remove.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                reset();
                removeFromParent();
            } 
        });
        return remove;
    }

    
    public abstract void saveValues(Map<String, Object> asPropertiesMap);

    //right click handling for optional menu

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
                        return FBFormItem.this;
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
                FBFormItem.this.rclickHandlers.remove(handler);
            }
        };
        this.rclickHandlers.add(handler);
        return reg;
    }

    @Override
    public HandlerRegistration addClickHandler(final ClickHandler handler) {
        this.clickHandlers.add(handler);
        final HandlerRegistration reg =  super.addClickHandler(handler);
        return new HandlerRegistration() {
            public void removeHandler() {
                reg.removeHandler();
                FBFormItem.this.clickHandlers.remove(handler);
            }
        };
    }
    
    public void addEffect(FBFormEffect effect) {
        if (!effects.contains(effect)) {
            effects.add(effect);
        }
    }
    
    public void removeEffect(FBFormEffect effect) {
        if (effects.contains(effect)) {
            effects.remove(effect);
        }
    }
    
    protected Integer extractInt(Object obj) {
        String s = extractString(obj);
        return s.equals("") ? null : Integer.valueOf(s);
    }
    
    protected Boolean extractBoolean(Object obj) {
        if (obj != null && obj instanceof Boolean) {
            return (Boolean) obj;
        }
        String s = extractString(obj);
        return s.equals("") ? Boolean.FALSE : Boolean.valueOf(s);
    }
    
    protected String extractString(Object obj) {
        return obj == null ? "" : obj.toString();
    }

    public List<FBFormEffect> getFormEffects() {
        return this.effects;
    }
    
    public int getDesiredX() {
        return desiredX;
    }

    public void setDesiredX(int desiredX) {
        this.desiredX = desiredX;
    }

    public int getDesiredY() {
        return desiredY;
    }

    public void setDesiredY(int desiredY) {
        this.desiredY = desiredY;
    }

    public void setDesiredPosition(int desiredX, int desiredY) {
        this.desiredX = desiredX;
        this.desiredY = desiredY;
    }

    public String getHeight() {
        return widgetHeight;
    }
    
    public String getWidth() {
        return widgetWidth;
    }

    @Override
    public void setWidth(String width) {
        if (width != null) {
            super.setWidth(width);
            this.widgetWidth = width;
        }
    }
    
    @Override
    public void setHeight(String height) {
        if (height != null) {
            super.setHeight(height);
            this.widgetHeight = height;
        }
    }
    
    public void setInput(InputData input) {
        this.input = input;
    }
    
    public void setOutput(OutputData output) {
        this.output = output;
    }
    
    public OutputData getOutput() {
        return output;
    }
    
    public InputData getInput() {
        return input;
    }
    
    public abstract FormItemRepresentation getRepresentation();
    
    public void populate(FormItemRepresentation rep) throws FormBuilderException {
        //TODO this.effects = ??? this needs to be obtained from server?
        //TODO this.validations = rep.getItemValidations(); will need a translation as well?
        this.widgetHeight = rep.getHeight();
        this.widgetWidth = rep.getWidth();
        this.input = rep.getInput();
        this.output = rep.getOutput();
    }
    
    public abstract FBFormItem cloneItem();
    
    protected <T extends FBFormItem> T cloneItem(T clone) {
        clone.validations = this.validations;
        clone.widgetHeight = this.widgetHeight;
        clone.widgetWidth = this.widgetWidth;
        clone.effects = this.effects;
        clone.input = this.input;
        clone.output = this.output;
        return clone;
    }
    
    public abstract Widget cloneDisplay();

    protected <T extends FormItemRepresentation> T getRepresentation(T rep) {
        rep.setInput(getInput());
        rep.setOutput(getOutput());
        rep.setHeight(getHeight());
        rep.setWidth(getWidth());
        List<FBValidation> repValidations = new ArrayList<FBValidation>();
        for (FBValidationItem item : getValidations()) {
            repValidations.add(item.createValidation());
        }
        rep.setItemValidations(repValidations);
        return rep;
    }
    
    public static FBFormItem createItem(FormItemRepresentation rep) throws FormBuilderException {
        if (rep == null) {
            return null;
        }
        String className = rep.getItemClassName();
        try {
            Class<?> clazz = ReflectionHelper.loadClass(className);
            FBFormItem item = (FBFormItem) ReflectionHelper.newInstance(clazz);
            item.populate(rep);
            return item;
        } catch (Exception e) {
            throw new FormBuilderException("Couldn't instantiate class " + className, e);
        }
    }
    
    public void setValidations(List<FBValidationItem> validations) {
        this.validations = validations;
    }
    
    public List<FBValidationItem> getValidations() {
        return validations;
    }
}
