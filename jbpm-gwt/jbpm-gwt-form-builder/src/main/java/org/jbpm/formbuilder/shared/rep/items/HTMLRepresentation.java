package org.jbpm.formbuilder.shared.rep.items;

import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.trans.LanguageException;
import org.jbpm.formbuilder.shared.rep.trans.LanguageFactory;

public class HTMLRepresentation extends FormItemRepresentation {

    private String width;
    private String height;
    private String content;
    
    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String translate(String language) throws LanguageException {
        return LanguageFactory.getInstance().getLanguage(language).html(this);
    }
}
