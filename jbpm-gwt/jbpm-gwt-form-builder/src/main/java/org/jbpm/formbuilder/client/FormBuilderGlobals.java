/*
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

import org.jbpm.formbuilder.client.messages.I18NConstants;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.event.shared.EventBus;

/**
 * Base singleton to obtain global variables, like service callers and event buses
 */
public class FormBuilderGlobals {

    public static final String FORM_PANEL_KEY = "org.jbpm.formbuilder.FormBuilder.FORM_PANEL";
    public static final String BASE_LOCALE = "org.jbpm.formbuilder.server.render.Renderer.BASE_LOCALE";
    
    private static final FormBuilderGlobals INSTANCE = new FormBuilderGlobals();
    private static final CommonGlobals SUPERINSTANCE = CommonGlobals.getInstance();
    
    private I18NConstants i18n;
    private FormBuilderService service;
    
    private FormBuilderGlobals() {
    }
    
    public static FormBuilderGlobals getInstance() {
        return INSTANCE;
    }

    public I18NConstants getI18n() {
        return i18n;
    }

    public void registerI18n(I18NConstants i18n) {
        this.i18n = i18n;
    }

    public void registerService(FormBuilderService service) {
        this.service = service;
    }
    
    public FormBuilderService getService() {
        return service;
    }
    
    public void registerEventBus(EventBus eventBus) {
        SUPERINSTANCE.registerEventBus(eventBus);
    }
    
    public EventBus getEventBus() {
        return SUPERINSTANCE.getEventBus();
    }
    
    public void registerCopy(AbstractFormItemCommand copy) {
        SUPERINSTANCE.registerCopy(copy);
    }
    
    public void registerCut(AbstractFormItemCommand cut) {
        SUPERINSTANCE.registerCut(cut);
    }
    
    public void registerPaste(AbstractFormItemCommand paste) {
        SUPERINSTANCE.registerPaste(paste);
    }
    
    public AbstractFormItemCommand copy() {
        return SUPERINSTANCE.copy();
    }
    
    public AbstractFormItemCommand cut() {
        return SUPERINSTANCE.cut();
    }
    
    public AbstractFormItemCommand paste() {
        return SUPERINSTANCE.paste();
    }

    public void registerDragController(PickupDragController dragController) {
        SUPERINSTANCE.registerDragController(dragController);
    }
    
    public PickupDragController getDragController() {
        return SUPERINSTANCE.getDragController();
    }
}
