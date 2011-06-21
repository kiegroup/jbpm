package org.jbpm.formbuilder.client.command;

import org.jbpm.formbuilder.client.bus.GetFormRepresentationEvent;
import org.jbpm.formbuilder.client.bus.PreviewFormRepresentationEvent;
import org.jbpm.formbuilder.client.bus.PreviewFormRepresentationEventHandler;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.MenuItem;

public class SaveFormCommand implements BaseCommand {

    private static final String SAVE_TYPE = SaveFormCommand.class.getName();
    
    private final EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
    
    public SaveFormCommand() {
        bus.addHandler(PreviewFormRepresentationEvent.TYPE, new PreviewFormRepresentationEventHandler() {
            public void onEvent(PreviewFormRepresentationEvent event) {
                if (SAVE_TYPE.equals(event.getSaveType())) {
                    Window.alert("MUST SHOW FLOATING PANEL WITH REPRESENTATION " + event.getRepresentation()); //TODO implement
                }
            }
        });
    }
    
    public void execute() {
        bus.fireEvent(new GetFormRepresentationEvent(SAVE_TYPE));
    }

    public void setItem(MenuItem item) {
        /* not implemented */
    }

}
