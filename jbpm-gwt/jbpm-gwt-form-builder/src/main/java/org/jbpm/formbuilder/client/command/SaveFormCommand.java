package org.jbpm.formbuilder.client.command;

import org.jbpm.formbuilder.client.bus.GetFormRepresentationEvent;
import org.jbpm.formbuilder.client.bus.SaveFormRepresentationEvent;
import org.jbpm.formbuilder.client.bus.SaveFormRepresentationEventHandler;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.MenuItem;

public abstract class SaveFormCommand implements BaseCommand {

    protected final EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
    private final String saveType;
    
    public SaveFormCommand(final String saveType) {
        this.saveType = saveType;
        this.bus.addHandler(SaveFormRepresentationEvent.TYPE, new SaveFormRepresentationEventHandler() {
            public void onEvent(SaveFormRepresentationEvent event) {
                FormRepresentation form = event.getRepresentation();
                if (saveType.equals(event.getSaveType())) {
                    saveForm(form);
                }
            }
        });
    }
    
    public void setItem(MenuItem item) {
        /* not implemented */
    }
    
    public void execute() {
        this.bus.fireEvent(new GetFormRepresentationEvent(this.saveType));
    }
    
    public abstract void saveForm(FormRepresentation form);

}
