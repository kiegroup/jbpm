package org.jbpm.formbuilder.server.form;

import javax.xml.bind.annotation.XmlElement;

import org.jbpm.formbuilder.shared.form.FormEncodingException;
import org.jbpm.formbuilder.shared.form.FormRepresentationEncoder;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;

public class FormDefDTO {

    private String _json;
    
    public FormDefDTO() {
        // jaxb needs a default constructor
    }
    
    public FormDefDTO(FormRepresentation form) throws FormEncodingException {
        FormRepresentationEncoder encoder = FormEncodingServerFactory.getEncoder();
        this._json = encoder.encode(form);
    }
    
    @XmlElement
    public String getJson() {
        return _json;
    }
    
    public void setJson(String json) {
        this._json = json;
    }
}
