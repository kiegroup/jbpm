package org.jbpm.formbuilder.client.resources;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;

public class FormBuilderGlobals {

    private static final FormBuilderGlobals INSTANCE = new FormBuilderGlobals();
    
    private EventBus eventBus = new SimpleEventBus();
    
    private PickupDragController dragController;

    private FormBuilderGlobals() {
    }
    
    public static FormBuilderGlobals getInstance() {
        return INSTANCE;
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
