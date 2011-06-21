package org.jbpm.formbuilder.server.form;

import org.jbpm.formbuilder.shared.rep.FormRepresentation;

public interface FormRepresentationDecoder {

    FormRepresentation decode(String json) throws FormEncodingException;
}
