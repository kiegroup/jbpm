package org.jbpm.formbuilder.client.resources;

import org.jbpm.formbuilder.client.FormBuilderService;

import com.allen_sauer.gwt.dnd.client.PickupDragController; 
import com.google.gwt.event.shared.EventBus;

public class FormBuilderGlobals {

    private static final FormBuilderGlobals INSTANCE = new FormBuilderGlobals();
    
    private EventBus eventBus;
    
    private PickupDragController dragController;
    
    private FormBuilderService service;

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
}
