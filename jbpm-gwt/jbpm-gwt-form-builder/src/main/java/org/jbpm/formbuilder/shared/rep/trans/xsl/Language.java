package org.jbpm.formbuilder.shared.rep.trans.xsl;

import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;
import org.jbpm.formbuilder.shared.rep.trans.LanguageException;

public class Language implements org.jbpm.formbuilder.shared.rep.trans.Language {

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
