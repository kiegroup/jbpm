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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.FormBuilderException;
import org.jbpm.formbuilder.client.bus.FormItemSelectionEvent;
import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.menu.EffectsPopupPanel;
import org.jbpm.formbuilder.client.messages.I18NConstants;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.client.validation.FBValidationItem;
import org.jbpm.formbuilder.common.handler.ControlKeyHandler;
import org.jbpm.formbuilder.common.handler.EventHelper;
import org.jbpm.formbuilder.common.handler.RightClickEvent;
import org.jbpm.formbuilder.common.handler.RightClickHandler;
import org.jbpm.formbuilder.common.reflect.ReflectionHelper;
import org.jbpm.formbuilder.shared.api.FBScript;
import org.jbpm.formbuilder.shared.api.FBValidation;
import org.jbpm.formbuilder.shared.api.FormItemRepresentation;
import org.jbpm.formbuilder.shared.api.InputData;
import org.jbpm.formbuilder.shared.api.OutputData;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Base class for UI components. Contains most of the edition definitions:
 *  right click functionality, inplace editor invocation, desired positioning, 
 *  width, height, validations, input association and output association.
 */
public abstract class FBFormItem extends FocusPanel {

    protected final I18NConstants i18n = FormBuilderGlobals.getInstance().getI18n();
    private List<FBValidationItem> validations = new ArrayList<FBValidationItem>();
    private Map<String, FBScript> eventActions = new HashMap<String, FBScript>();
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
        EventHelper.addRightClickHandler(this, new RightClickHandler() {
            @Override
            public void onRightClick(RightClickEvent event) {
                EffectsPopupPanel popupPanel = new EffectsPopupPanel(FBFormItem.this, true);
                if (getFormEffects() != null && !getFormEffects().isEmpty()) {
                    popupPanel.setPopupPosition(event.getX(), event.getY());
                    popupPanel.show();
                }
            }
        });
        EventHelper.addKeyboardCopyHandler(this, new ControlKeyHandler() {
            @Override
            public void onKeyboardControl() {
                FormBuilderGlobals.getInstance().copy().append(FBFormItem.this).execute();
            }
        });
        EventHelper.addKeyboardCutHandler(this, new ControlKeyHandler() {
            @Override
            public void onKeyboardControl() {
                FormBuilderGlobals.getInstance().cut().append(FBFormItem.this).execute();
            }
        });
        EventHelper.addKeyboardPasteHandler(this, new ControlKeyHandler() {
            @Override
            public void onKeyboardControl() {
                FormBuilderGlobals.getInstance().paste().append(FBFormItem.this).execute();
            }
        });
        EventHelper.addBlurHandler(this, new BlurHandler() {
            @Override
            public void onBlur(BlurEvent event) {
                reset();
            }
        });
        EventHelper.addFocusHandler(this, new FocusHandler() {
            @Override
            public void onFocus(FocusEvent event) {
                makeEditor();
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

    @Override
    public void onBrowserEvent(Event event) {
        EventHelper.onBrowserEvent(this, event);
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
        rep.setEventActions(getEventActions());
        return rep;
    }
    
    public static FBFormItem createItem(FormItemRepresentation rep) throws FormBuilderException {
        if (rep == null) {
            return null;
        }
        String className = rep.getItemClassName();
        try {
            FBFormItem item = (FBFormItem) ReflectionHelper.newInstance(className);
            item.populate(rep);
            return item;
        } catch (Exception e) {
            I18NConstants i18n = FormBuilderGlobals.getInstance().getI18n();
            throw new FormBuilderException(i18n.CouldntInstantiateClass(className), e);
        }
    }
    
    public void setValidations(List<FBValidationItem> validations) {
        if (validations == null) {
            validations = new ArrayList<FBValidationItem>();
        }
        this.validations = validations;
    }
    
    public List<FBValidationItem> getValidations() {
        return validations;
    }
    
    public void setEventActions(Map<String, FBScript> eventActions) {
        if (eventActions == null) {
            eventActions = new HashMap<String, FBScript>();
        }
        this.eventActions = eventActions;
    }

    public Map<String, FBScript> getEventActions() {
        return eventActions;
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
                    FBFormEffect effect = (FBFormEffect) ReflectionHelper.newInstance(className);
                    this.effects.add(effect);
                } catch (Exception e) {
                    I18NConstants i18n = FormBuilderGlobals.getInstance().getI18n();
                    throw new FormBuilderException(i18n.CouldntInstantiateClass(className), e);
                }
            }
        }
        if (rep.getEventActions() != null) {
            for (String key : eventActions.keySet()) {
                eventActions.put(key, null);
            }
            this.eventActions.putAll(rep.getEventActions());
        }
        this.validations.clear();
        if (rep.getItemValidations() != null) {
            for (FBValidation validation : rep.getItemValidations()) {
                FBValidationItem validationItem = FBValidationItem.createValidation(validation);
                this.validations.add(validationItem);
            }
        }
        setHeight(rep.getHeight());
        setWidth(rep.getWidth());
        this.input = rep.getInput();
        this.output = rep.getOutput();
        this.eventActions = rep.getEventActions();
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
