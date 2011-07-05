package org.jbpm.formbuilder.shared.form;

import java.util.List;

import org.jbpm.formbuilder.shared.rep.FormRepresentation;

public interface FormDefinitionService {

    String /*URL*/ generateForm(String pkgName, String language, FormRepresentation form);
    
    void saveForm(String pkgName, String comment, FormRepresentation form) throws FormServiceException;
    
    List<FormRepresentation> getForms(String pkgName);

    FormRepresentation getFormByTaskId(String pkgName, String taskId);
    
    /* TODO add following methods:
     * 
     * (service methods)
     * saveForm(FormRepresentation form): form_id
     * saveFormItem(FormItemRepresentation formItem): form_item_id
     * 
     * getTemplateForm(form_id formId, String language): html
     * getTemplateFormItem(form_item_id formItemId, String language): html
     * 
     * generateTemplateForm(FormRepresentation form, String language): template
     * generateTemplateFormItem(FormItemRepresentation formItem, String language): template
     * 
     * (jar methods)
     * renderTemplate(template t, Map<String, Object> inputs): html
     */
    
}
