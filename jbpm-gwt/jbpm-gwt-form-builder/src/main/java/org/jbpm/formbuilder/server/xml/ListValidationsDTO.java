package org.jbpm.formbuilder.server.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.jbpm.formbuilder.shared.menu.ValidationDescription;

@XmlRootElement (name = "validations")
public class ListValidationsDTO {

    private List<ValidationDTO> _validation;

    public ListValidationsDTO() {
        // jaxb needs a default constructor
    }
    
    public ListValidationsDTO(List<ValidationDescription> validations) {
        if (validations != null) {
            for (ValidationDescription val : validations) {
                getValidation().add(new ValidationDTO(val));
            }
        }
    }
    
    public List<ValidationDTO> getValidation() {
        if (_validation == null) {
            _validation = new ArrayList<ValidationDTO>();
        }
        return _validation;
    }

    public void setValidation(List<ValidationDTO> validation) {
        this._validation = validation;
    }
}
