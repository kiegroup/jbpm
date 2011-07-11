package org.jbpm.formbuilder.shared.form;

import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;
import org.jbpm.formbuilder.shared.task.TaskRef;

public interface FormDefinitionService {

    List<FormRepresentation> getForms(String pkgName) throws FormServiceException;
    Map<String, FormItemRepresentation> getFormItems(String pkgName) throws FormServiceException;

    String /*formId*/ saveForm(String pkgName, FormRepresentation form) throws FormServiceException;
    String /*formItemId*/ saveFormItem(String pkgName, String formItemName, FormItemRepresentation formItem) throws FormServiceException;
    
    void deleteForm(String pkgName, String formId) throws FormServiceException;
    void deleteFormItem(String pkgName, String formItemId) throws FormServiceException;
    
    FormRepresentation getForm(String pkgName, String formId) throws FormServiceException;
    FormItemRepresentation getFormItem(String pkgName, String formItemId) throws FormServiceException;

    FormRepresentation getAssociatedForm(String pkgName, TaskRef task) throws FormServiceException;

    // TODO see where to put method renderTemplate(template t, Map<String, Object> inputs): html
}
