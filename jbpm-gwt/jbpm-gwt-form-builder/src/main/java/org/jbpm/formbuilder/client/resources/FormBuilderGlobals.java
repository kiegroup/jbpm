package org.jbpm.formbuilder.client.resources;

import org.jbpm.formbuilder.client.FormBuilderService;
import org.jbpm.formbuilder.client.command.CopyCommand;
import org.jbpm.formbuilder.client.command.CutCommand;
import org.jbpm.formbuilder.client.command.PasteCommand;

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
