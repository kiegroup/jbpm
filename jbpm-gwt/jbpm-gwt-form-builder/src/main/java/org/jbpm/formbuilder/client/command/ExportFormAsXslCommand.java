package org.jbpm.formbuilder.client.command;

import com.gwtent.reflection.client.Reflectable;

@Reflectable
public class ExportFormAsXslCommand extends ExportFormCommand {

    private static final String LANG = "xsl";
    
    public ExportFormAsXslCommand() {
        super(LANG);
    }

}
