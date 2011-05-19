package org.jbpm.formbuilder.client.resources;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;

public class FormBuilderGlobals {

    private static final FormBuilderGlobals INSTANCE = new FormBuilderGlobals();
    
    private EventBus eventBus = new SimpleEventBus();

    private FormBuilderGlobals() {
    }
    
    public static FormBuilderGlobals getInstance() {
        return INSTANCE;
    }
    
    public EventBus getEventBus() {
        return this.eventBus;
    }
}
