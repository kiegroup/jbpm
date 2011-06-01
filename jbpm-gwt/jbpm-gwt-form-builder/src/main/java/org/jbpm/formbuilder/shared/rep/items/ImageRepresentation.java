package org.jbpm.formbuilder.shared.rep.items;

import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.trans.LanguageException;
import org.jbpm.formbuilder.shared.rep.trans.LanguageFactory;

public class ImageRepresentation extends FormItemRepresentation {

    private String altText;
    private String height;
    private String width;
    private String url;
    private String id;
    
    public String getAltText() {
        return altText;
    }

    public void setAltText(String altText) {
        this.altText = altText;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String translate(String language) throws LanguageException {
        return LanguageFactory.getInstance().getLanguage(language).image(this);
    }
}
