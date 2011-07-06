package org.jbpm.formbuilder.shared.form;

import java.util.List;

import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;

public interface FormDefinitionService {

    List<FormRepresentation> getForms(String pkgName) throws FormServiceException;
    List<FormItemRepresentation> getFormItems(String pkgName) throws FormServiceException;

    String /*formId*/ saveForm(String pkgName, FormRepresentation form) throws FormServiceException;
    String /*formItemId*/ saveFormItem(String pkgName, String formItemName, FormItemRepresentation formItem) throws FormServiceException;
    FormRepresentation getForm(String pkgName, String formId) throws FormServiceException;
    FormItemRepresentation getFormItem(String pkgName, String formItemId) throws FormServiceException;
    
    // TODO see where to put method renderTemplate(template t, Map<String, Object> inputs): html
}
