package org.jbpm.formbuilder.client.command;

import org.jbpm.formbuilder.client.bus.NotificationEvent;
import org.jbpm.formbuilder.client.bus.NotificationEvent.Level;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;
import org.jbpm.formbuilder.shared.rep.trans.LanguageException;

import com.google.gwt.user.client.Window;

public class SaveFormAsFtlCommand extends SaveFormCommand {

    public SaveFormAsFtlCommand() {
        super("ftl");
    }

    @Override
    public void saveForm(FormRepresentation form) {
        try {
            String ftlContent = form.translate("ftl");
            String fileName = form.getTaskId() + ".ftl";
            Window.alert("FILE: "+ fileName + "\n" + ftlContent);
            //TODO super.bus.fireEvent(new CreateFileEvent(ftlContent, fileName));
        } catch (LanguageException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, 
                    "Unexpected error while saving ftl form", e));
        }
    }
    
    

}
