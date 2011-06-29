package org.jbpm.formbuilder.shared.form;

import java.util.Map;

import org.jbpm.formbuilder.shared.rep.FormRepresentation;

public interface FormRepresentationDecoder {

    FormRepresentation decode(String json) throws FormEncodingException;
    
    Object decode(Map<String, Object> data) throws FormEncodingException;
}
