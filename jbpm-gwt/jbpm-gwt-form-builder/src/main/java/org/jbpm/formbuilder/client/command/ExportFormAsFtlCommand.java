package org.jbpm.formbuilder.client.command;

import com.gwtent.reflection.client.Reflectable;

@Reflectable
public class ExportFormAsFtlCommand extends ExportFormCommand {

    private static final String LANG = "ftl";
    
    public ExportFormAsFtlCommand() {
        super(LANG);
    }

}
