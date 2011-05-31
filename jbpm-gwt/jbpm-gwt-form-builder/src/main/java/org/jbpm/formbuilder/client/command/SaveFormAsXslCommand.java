package org.jbpm.formbuilder.client.command;

import org.jbpm.formbuilder.shared.rep.FormRepresentation;

import com.google.gwt.user.client.Window;

public class SaveFormAsXslCommand extends SaveFormCommand {

    public SaveFormAsXslCommand() {
        super("xsl");
        // TODO Auto-generated constructor stub
    }

    @Override
    public void saveForm(FormRepresentation form) {
        Window.alert("Here there must be actions to save this form as Xsl");
        // TODO Auto-generated method stub
    }

}
