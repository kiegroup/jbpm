package org.jbpm.formbuilder.server.trans.xsl;

import java.net.MalformedURLException;
import java.net.URL;

import org.jbpm.formbuilder.server.trans.LanguageException;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;

public class Language implements org.jbpm.formbuilder.server.trans.Language {

    private static final String LANG = "xsl";
    
    public String getLanguage() {
        return LANG;
    }
    
    public URL translateForm(FormRepresentation form) throws LanguageException {
        try {
            return new URL("<!-- TODO -->"); //TODO
        } catch (MalformedURLException e) {
            throw new LanguageException("Problem mocking url", e);
        }
    }

    public Object translateItem(FormItemRepresentation item) throws LanguageException {
        return "<!-- TODO -->"; //TODO
    }
}
