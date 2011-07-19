package org.jbpm.formbuilder.client.command;

public class ExportFormAsFtlCommand extends ExportFormCommand {

    private static final String LANG = "ftl";
    
    public ExportFormAsFtlCommand() {
        super(LANG);
    }

}
