package org.jbpm.formbuilder.shared.form;

import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;

public interface FormRepresentationEncoder {

    String encode(FormRepresentation form) throws FormEncodingException;
    
    String encode(FormItemRepresentation item) throws FormEncodingException;
}
