package org.jbpm.formbuilder.shared.form;

import org.jbpm.formbuilder.shared.rep.FormRepresentation;

public class MockFormDefinitionService implements FormDefinitionService {

    public String generateForm(String language, FormRepresentation form) {
        return "form representation";
    }

    public void saveForm(String reference, FormRepresentation form) {
        // TODO Auto-generated method stub

    }

}
