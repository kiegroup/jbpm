package org.jbpm.formbuilder.server.form;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.jbpm.formbuilder.shared.form.FormEncodingException;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;

@XmlRootElement (name = "listFormItems") public class ListFormsItemsDTO {

    private List<FormItemDefDTO> _formItem = new ArrayList<FormItemDefDTO>();

    public ListFormsItemsDTO() {
        // jaxb needs a default constructor
    }
    
    public ListFormsItemsDTO(Map<String, FormItemRepresentation> formItems) throws FormEncodingException {
        if (formItems != null) {
            for (Map.Entry<String, FormItemRepresentation> entry : formItems.entrySet()) {
                _formItem.add(new FormItemDefDTO(entry.getKey(), entry.getValue()));
            }
        }
    }
    
    public ListFormsItemsDTO(String formItemId, FormItemRepresentation formItem) throws FormEncodingException {
        if (formItem != null) {
            _formItem.add(new FormItemDefDTO(formItemId, formItem));
        }
    }
    
    @XmlElement
    public List<FormItemDefDTO> getFormItem() {
        return _formItem;
    }

    public void setFormItem(List<FormItemDefDTO> formItem) {
        this._formItem = formItem;
    }
}
