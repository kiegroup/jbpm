package org.jbpm.formbuilder.shared.form;

import java.util.List;

import org.jbpm.formbuilder.shared.rep.FormRepresentation;

public class MockFormDefinitionService implements FormDefinitionService {

    public String generateForm(String pkgName, String language, FormRepresentation form) {
        return "form representation";
    }

    public void saveForm(String pkgName, String comment, FormRepresentation form) {
        // TODO Auto-generated method stub

    }
    
    public List<FormRepresentation> getForms(String pkgName) {
        return null;
    }

}
