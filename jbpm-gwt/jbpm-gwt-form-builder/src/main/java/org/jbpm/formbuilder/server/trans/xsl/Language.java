package org.jbpm.formbuilder.server.trans.xsl;

import org.jbpm.formbuilder.server.trans.LanguageException;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;

public class Language implements org.jbpm.formbuilder.server.trans.Language {

    private static final String LANG = "xsl";
    
    public String getLanguage() {
        return LANG;
    }
    
    public String translateForm(FormRepresentation form) throws LanguageException {
        return "<!-- TODO -->"; //TODO
    }

    public String translateItem(FormItemRepresentation item) throws LanguageException {
        return "<!-- TODO -->"; //TODO
    }
}
