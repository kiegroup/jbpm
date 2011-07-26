package org.jbpm.formbuilder.client.command;

import com.gwtent.reflection.client.Reflectable;

@Reflectable
public class ExportFormAsXulForPhpCommand extends ExportFormCommand {

    private static final String LANG = "xulphp";
    
    public ExportFormAsXulForPhpCommand() {
        super(LANG);
    }

}
