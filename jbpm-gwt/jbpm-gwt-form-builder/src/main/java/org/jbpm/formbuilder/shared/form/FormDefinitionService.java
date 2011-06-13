package org.jbpm.formbuilder.shared.form;

import org.jbpm.formbuilder.shared.rep.FormRepresentation;

public interface FormDefinitionService {

    String generateForm(String language, FormRepresentation form);
    
    void saveForm(String reference, FormRepresentation form);
}
