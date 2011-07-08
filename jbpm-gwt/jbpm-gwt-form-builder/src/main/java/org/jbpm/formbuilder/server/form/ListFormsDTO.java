package org.jbpm.formbuilder.server.form;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.jbpm.formbuilder.shared.form.FormEncodingException;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;

@XmlRootElement (name = "listForms") public class ListFormsDTO {

    private List<FormDefDTO> _form = new ArrayList<FormDefDTO>();

    public ListFormsDTO() {
        // jaxb needs a default constructor
    }
    
    public ListFormsDTO(List<FormRepresentation> forms) throws FormEncodingException {
        if (forms != null) {
            for (FormRepresentation form : forms) {
                _form.add(new FormDefDTO(form));
            }
        }
    }
    
    public ListFormsDTO(FormRepresentation form) throws FormEncodingException {
        if (form != null) {
            _form.add(new FormDefDTO(form));
        }
    }
    
    @XmlElement
    public List<FormDefDTO> getForm() {
        return _form;
    }

    public void setForm(List<FormDefDTO> form) {
        this._form = form;
    }
}
