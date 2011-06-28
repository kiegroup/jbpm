package org.jbpm.formbuilder.shared.form;

import org.jbpm.formbuilder.shared.rep.FormRepresentation;

public interface FormRepresentationEncoder {

    String encode(FormRepresentation form) throws FormEncodingException;
}
