package org.jbpm.formbuilder.server.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement (name = "formPreview") public class FormPreviewDTO {

    private List<FormPreviewParameterDTO> _input = new ArrayList<FormPreviewParameterDTO>();
    private String _representation;
    
    public FormPreviewDTO() {
        // jaxb needs a default constructor
    }

    @XmlElement
    public List<FormPreviewParameterDTO> getInput() {
        return _input;
    }

    public void setInput(List<FormPreviewParameterDTO> input) {
        this._input = input;
    }

    @XmlElement
    public String getRepresentation() {
        return _representation;
    }

    public void setRepresentation(String representation) {
        this._representation = representation;
    }

    public Map<String, Object> getInputsAsMap() {
        Map<String, Object> retval = null;
        if (_input != null) {
            retval = new HashMap<String, Object>();
            for (FormPreviewParameterDTO input : _input) {
                if (input != null) {
                    retval.put(input.getKey(), input.getValue());
                }
            }
        }
        return retval;
    }
}
