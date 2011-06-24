package org.jbpm.formbuilder.client.command;

import org.jbpm.formbuilder.client.FormBuilderException;
import org.jbpm.formbuilder.client.FormBuilderService;
import org.jbpm.formbuilder.client.bus.NotificationEvent;
import org.jbpm.formbuilder.client.bus.NotificationEvent.Level;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;

public class PreviewFormAsFtlCommand extends PreviewFormCommand {

    private static final String LANG = "ftl";
    
    private final FormBuilderService server = FormBuilderGlobals.getInstance().getService();
    
    public PreviewFormAsFtlCommand() {
        super(LANG);
    }

    @Override
    public void saveForm(FormRepresentation form) {
        try {
            String url = server.generateForm(form, LANG);
            refreshPopupForURL(url);
        } catch (FormBuilderException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, 
                    "Unexpected error while previewing ftl form", e));
        } 
    }
}
