package org.jbpm.formbuilder.client.command;

import org.jbpm.formbuilder.client.FormBuilderException;
import org.jbpm.formbuilder.client.FormBuilderService;
import org.jbpm.formbuilder.client.bus.GetFormRepresentationEvent;
import org.jbpm.formbuilder.client.bus.GetFormRepresentationResponseEvent;
import org.jbpm.formbuilder.client.bus.GetFormRepresentationResponseHandler;
import org.jbpm.formbuilder.client.bus.ui.NotificationEvent;
import org.jbpm.formbuilder.client.bus.ui.NotificationEvent.Level;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.MenuItem;

public class ExportFormCommand implements BaseCommand {

    protected final EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
    private final FormBuilderService server = FormBuilderGlobals.getInstance().getService();
    private final String saveType = getClass().getName();
    private final String language;
    
    public ExportFormCommand(String language) {
        this.language = language;
        this.bus.addHandler(GetFormRepresentationResponseEvent.TYPE, new GetFormRepresentationResponseHandler() {
            public void onEvent(GetFormRepresentationResponseEvent event) {
                FormRepresentation form = event.getRepresentation();
                String type = event.getSaveType();
                if (saveType.equals(type)) {
                    getTemplate(form);
                }
            }
        });
    }
    
    private void getTemplate(FormRepresentation form) {
        try {
            server.loadFormTemplate(form, language);
        } catch (FormBuilderException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, 
                    "Unexpected error while exporting " + this.saveType + " form", e));
        }
    }
    
    public void execute() {
        this.bus.fireEvent(new GetFormRepresentationEvent(this.saveType));
    }

    public void setItem(MenuItem item) {
        /* do nothing */
    }

    public void setEmbeded(String profile) {
        //shouldn't be disabled when embedded
    }
}
