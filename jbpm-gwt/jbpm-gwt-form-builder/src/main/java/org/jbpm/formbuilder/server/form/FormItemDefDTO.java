package org.jbpm.formbuilder.server.form;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.jbpm.formbuilder.shared.form.FormEncodingException;
import org.jbpm.formbuilder.shared.form.FormRepresentationEncoder;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;

public class FormItemDefDTO {

    private String _json;
    private String _formItemId;
    
    public FormItemDefDTO() {
        // jaxb needs a default constructor
    }
    
    public FormItemDefDTO(String formItemId, FormItemRepresentation formItem) throws FormEncodingException {
        FormRepresentationEncoder encoder = FormEncodingServerFactory.getEncoder();
        this._formItemId = formItemId;
        this._json = encoder.encode(formItem);
    }
    
    @XmlElement
    public String getJson() {
        return _json;
    }
    
    public void setJson(String json) {
        this._json = json;
    }
    
    @XmlAttribute
    public String getFormItemId() {
        return _formItemId;
    }
    
    public void setFormItemId(String formItemId) {
        this._formItemId = formItemId;
    }
}
