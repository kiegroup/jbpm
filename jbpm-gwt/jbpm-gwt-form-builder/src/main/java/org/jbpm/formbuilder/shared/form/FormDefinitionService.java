package org.jbpm.formbuilder.shared.form;

import java.util.List;

import org.jbpm.formbuilder.shared.rep.FormRepresentation;

public interface FormDefinitionService {

    String generateForm(String pkgName, String language, FormRepresentation form);
    
    void saveForm(String pkgName, String comment, FormRepresentation form) throws FormServiceException;
    
    List<FormRepresentation> getForms(String pkgName);

    FormRepresentation getFormByTaskId(String pkgName, String taskId);
}
