package org.jbpm.formbuilder.client.command;

import org.jbpm.formbuilder.shared.rep.FormRepresentation;

import com.google.gwt.user.client.Window;

public class SaveFormAsFtlCommand extends SaveFormCommand {

    public SaveFormAsFtlCommand() {
        super("ftl");
        // TODO Auto-generated constructor stub
    }

    @Override
    public void saveForm(FormRepresentation form) {
        Window.alert("Here there must be actions to save this form as Freemarker");
        // TODO Auto-generated method stub
    }

}
