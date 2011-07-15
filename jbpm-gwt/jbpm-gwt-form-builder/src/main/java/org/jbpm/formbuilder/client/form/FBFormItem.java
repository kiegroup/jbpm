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
import java.util.Map;

import org.jbpm.formbuilder.client.FormBuilderException;
import org.jbpm.formbuilder.client.bus.FormItemSelectionEvent;
import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.menu.EffectsPopupPanel;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.client.validation.FBValidationItem;
import org.jbpm.formbuilder.common.handler.RightClickEvent;
import org.jbpm.formbuilder.common.handler.RightClickHandler;
import org.jbpm.formbuilder.shared.rep.FBValidation;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.InputData;
import org.jbpm.formbuilder.shared.rep.OutputData;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.rpc.impl.ReflectionHelper;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Base class for UI components. Contains most of the edition definitions:
 *  right click functionality, inplace editor invocation, desired positioning, 
 *  width, height, validations, input association and output association.
 */
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
        sinkEvents(Event.ONMOUSEUP | Event.ONDBLCLICK | Event.ONCONTEXTMENU | Event.ONKEYPRESS | Event.ONFOCUS | Event.ONBLUR);
        /*addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                makeEditor();
            }
        });*/
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
        FBInplaceEditor inplaceEditor = createInplaceEditor();
        if (inplaceEditor != null && !isAlreadyEditing()) {
            auxiliarWidget = getWidget();
            clear();
            setWidget(inplaceEditor);
            setAlreadyEditing(true);
            inplaceEditor.focus();
        }
    }
    
    public boolean isAlreadyEditing() {
        return alreadyEditing;
    }

    public void setAlreadyEditing(boolean alreadyEditing) {
        this.alreadyEditing = alreadyEditing;
    }

    public void reset() {
        if (auxiliarWidget != null && !getEditor().isFocused()) {
            clear();
            add(auxiliarWidget);
            setAlreadyEditing(false);
            fireSelectionEvent(new FormItemSelectionEvent(this, false));
        }
    }
    
    private FBInplaceEditor getEditor() {
        return (FBInplaceEditor) getWidget();
    }
    
    public final void fireSelectionEvent(FormItemSelectionEvent event) {
        EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
        bus.fireEvent(event);
    }

    //right click handling for optional menu

    @Override
    public void onBrowserEvent(Event event) {
        switch (DOM.eventGetType(event)) {
        case Event.ONMOUSEUP:
            event.stopPropagation();
            event.preventDefault();
            handleMouseUp(event);
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
            handleKeyPress(event);
            break;
        case Event.ONFOCUS:
            makeEditor();
            break;
        case Event.ONBLUR:
            reset();
            break;
        default:
            // Do nothing
        }//end switch
    }

    private void handleMouseUp(Event event) {
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
    }

    private void handleKeyPress(Event event) {
        if (event.getCtrlKey()) {
            event.stopPropagation();
            event.preventDefault();
            switch (event.getCharCode()) {
                case 'c': case 'C': //copy
                FormBuilderGlobals.getInstance().copy().append(this).execute();
                break;
            case 'x': case 'X': //cut
                FormBuilderGlobals.getInstance().cut().append(this).execute();
                break;
            case 'v': case 'V': //paste
                FormBuilderGlobals.getInstance().paste().append(this).execute();
                break;
            default: 
                super.onBrowserEvent(event);
            }
        } else {
            super.onBrowserEvent(event);
        }
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
    
    protected <T extends FBFormItem> T cloneItem(T clone) {
        clone.validations = this.validations;
        clone.widgetHeight = this.widgetHeight;
        clone.widgetWidth = this.widgetWidth;
        clone.effects = this.effects;
        clone.input = this.input;
        clone.output = this.output;
        return clone;
    }
    
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
        for (FBFormEffect effect : getFormEffects()) {
            rep.addEffectClass(effect.getClass());
        }
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

    /**
     * If you wish that on clicking your UI component, it becomes replaced by
     * a custom editor, this is where you must create it
     * @return A custom subclass of {@link FBInplaceEditor} to replace component
     * and be rechanged after lost of focus. Default returns null
     */
    public FBInplaceEditor createInplaceEditor() {
        return null;
    }
    
    /**
     * This method must be defined to tell outside default editors what properties
     * this UI component has. Outside editors will then provide functionality to edit
     * these properties and invoke {@link #saveValues(Map)} 
     * @return a map of the properties of this UI component
     */
    public abstract Map<String, Object> getFormItemPropertiesMap();
    
    /**
     * This method must be defined so that outside default editor can tell this 
     * UI component the new value of its properties. It's the entire responsibility
     * of this UI component to repopulate itself from these properties 
     * @param asPropertiesMap a map of the proeprties to set on this UI component
     */
    public abstract void saveValues(Map<String, Object> asPropertiesMap);
    
    /**
     * This method is used to create a POJO representation of the UI component that any
     * java service can understand.
     * @return a POJO representation of this UI component 
     */
    public abstract FormItemRepresentation getRepresentation();
    
    /**
     * This method must be overriden by each {@link FBFormItem} subclass to repopulate
     * its properties from an outside POJO representation.
     * @param rep the POJO representation of this UI component. It's the responsibility 
     * of each {@link FBFormItem} instance to validate the POJO representation for itself,
     * call the superclass method, and define what and how properties of its UI component
     * should be updated.
     * @throws FormBuilderException in case of error or invalid content
     */
    public void populate(FormItemRepresentation rep) throws FormBuilderException {
        if (rep.getEffectClasses() != null) {
            this.effects = new ArrayList<FBFormEffect>(rep.getEffectClasses().size());
            for (String className : rep.getEffectClasses()) {
                try {
                    Class<?> clazz = ReflectionHelper.loadClass(className);
                    FBFormEffect effect = (FBFormEffect) ReflectionHelper.newInstance(clazz);
                    this.effects.add(effect);
                } catch (Exception e) {
                    throw new FormBuilderException("Couldn't instantiate class " + className, e);
                }
            }
        }
        //TODO this.validations = rep.getItemValidations(); will need a translation as well?
        this.widgetHeight = rep.getHeight();
        this.widgetWidth = rep.getWidth();
        this.input = rep.getInput();
        this.output = rep.getOutput();
    }
    
    /**
     * This methods is similar to {@link #clone()}, but returns a proper type and forces implementation
     * @return a clone of this very object
     */
    public abstract FBFormItem cloneItem();

    /**
     * Similar to {@link #cloneItem()}, but only clones the underlying UI GWT component.
     * @return
     */
    public abstract Widget cloneDisplay();
}
