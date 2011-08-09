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
package org.jbpm.formbuilder.client.resources;

import org.jbpm.formbuilder.client.FormBuilderService;
import org.jbpm.formbuilder.client.command.CopyCommand;
import org.jbpm.formbuilder.client.command.CutCommand;
import org.jbpm.formbuilder.client.command.PasteCommand;
import org.jbpm.formbuilder.client.messages.Constants;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.event.shared.EventBus;

/**
 * Base singleton to obtain global variables, like service callers and event buses
 */
public class FormBuilderGlobals {

    private static final FormBuilderGlobals INSTANCE = new FormBuilderGlobals();
    
    private EventBus eventBus;
    
    private PickupDragController dragController;
    
    private FormBuilderService service;
    
    private Constants i18n;

    private FormBuilderGlobals() {
    }
    
    public static FormBuilderGlobals getInstance() {
        return INSTANCE;
    }
    
    public void registerEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }
    
    public EventBus getEventBus() {
        return this.eventBus;
    }
    
    public void registerDragController(PickupDragController dragController) {
        this.dragController = dragController;
    }
    
    public PickupDragController getDragController() {
        return dragController;
    }

    public void registerService(FormBuilderService service) {
        this.service = service;
    }
    
    public FormBuilderService getService() {
        return service;
    }

    public void registerI18n(Constants i18n) {
        this.i18n = i18n;
    }
    
    public Constants getI18n() {
        return i18n;
    }
    
    private CopyCommand copy;
    private CutCommand cut;
    private PasteCommand paste;
    
    public void register(CopyCommand copy) {
        this.copy = copy;
    }
    
    public void register(CutCommand cut) {
        this.cut = cut;
    }
    
    public void register(PasteCommand paste) {
        this.paste = paste;
    }
    
    public CopyCommand copy() {
        return copy;
    }
    
    public CutCommand cut() {
        return cut;
    }
    
    public PasteCommand paste() {
        return paste;
    }
}
