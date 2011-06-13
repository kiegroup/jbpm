package org.jbpm.formbuilder.client.resources;

import com.allen_sauer.gwt.dnd.client.PickupDragController; 
import com.google.gwt.event.shared.EventBus;

public class FormBuilderGlobals {

    private static final FormBuilderGlobals INSTANCE = new FormBuilderGlobals();
    
    private EventBus eventBus;
    
    private PickupDragController dragController;

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
}
