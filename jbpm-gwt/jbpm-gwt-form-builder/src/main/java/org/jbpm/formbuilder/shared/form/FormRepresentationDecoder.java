package org.jbpm.formbuilder.shared.form;

import org.jbpm.formbuilder.shared.rep.FormRepresentation;

public interface FormRepresentationDecoder {

    FormRepresentation decode(String json) throws FormEncodingException;
}
