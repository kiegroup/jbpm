package org.jbpm.formbuilder.shared.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;

public class MockFormDefinitionService implements FormDefinitionService {

    private Map<String, List<FormRepresentation>> forms = new HashMap<String, List<FormRepresentation>>();
    private Map<String, List<Map.Entry<String, FormItemRepresentation>>> items = 
        new HashMap<String, List<Map.Entry<String, FormItemRepresentation>>>();
    
    public List<FormRepresentation> getForms(String pkgName) {
        return forms.get(pkgName);
    }
    
    public FormRepresentation getForm(String pkgName, String formId) throws FormServiceException {
        List<FormRepresentation> list = forms.get(pkgName);
        if (list == null) {
            throw new FormServiceException();
        }
        FormRepresentation form = null;
        for (FormRepresentation f : list) {
            if (formId.equals(f.getName())) {
                form = f;
                break;
            }
        }
        return form;
    }
    
    public FormItemRepresentation getFormItem(String pkgName, String formItemId)
            throws FormServiceException {
        List<Map.Entry<String, FormItemRepresentation>> list = items.get(pkgName);
        if (list == null) {
            throw new FormServiceException();
        }
        FormItemRepresentation item = null;
        for (Map.Entry<String, FormItemRepresentation> i : list) {
            if (formItemId.equals(i.getKey())) {
                item = i.getValue();
                break;
            }
        }
        return item;
    }
    
    public List<FormItemRepresentation> getFormItems(String pkgName) {
        List<Map.Entry<String, FormItemRepresentation>> list = items.get(pkgName);
        List<FormItemRepresentation> retval = null;
        if (list != null) {
            retval = new ArrayList<FormItemRepresentation>();
            for (Map.Entry<String, FormItemRepresentation> entry : list) {
                retval.add(entry.getValue());
            }
        }
        return retval;
    }

    public String saveForm(String pkgName, FormRepresentation form) {
        if (form.getName() == null) {
            form.setName("formDefinition_" + System.currentTimeMillis());
        } else if (!form.getName().startsWith("formDefinition_")){
            form.setName("formDefinition_" + form.getName());
        }
        List<FormRepresentation> list = forms.get(pkgName);
        if (list == null) {
            list = new ArrayList<FormRepresentation>();
        }
        list.add(form);
        forms.put(pkgName, list);
        return form.getName();
    }

    public String saveFormItem(String pkgName, String formItemName, final FormItemRepresentation formItem) {
        if (formItemName == null) {
            formItemName = "formItemDefinition_" + System.currentTimeMillis();
        } else if (!formItemName.startsWith("formDefinition_")){
            formItemName = "formDefinition_" + formItemName;
        }
        List<Map.Entry<String, FormItemRepresentation>> list = items.get(pkgName);
        if (list == null) {
            list = new ArrayList<Map.Entry<String, FormItemRepresentation>>();
        }
        final String itemName = formItemName;
        list.add(new Map.Entry<String, FormItemRepresentation>() {
            public String getKey() {
                return itemName;
            }
            public FormItemRepresentation getValue() {
                return formItem;
            }
            public FormItemRepresentation setValue(FormItemRepresentation value) {
                return formItem;
            }
        });
        items.put(pkgName, list);
        return itemName;
    }
    
    public void deleteForm(String pkgName, String formId) {
        List<FormRepresentation> list = forms.get(pkgName);
        if (list != null) {
            FormRepresentation toRemove = null;
            for (FormRepresentation form : list) {
                if (formId.equals(form.getName())) {
                    toRemove = form;
                    break;
                }
            }
            if (toRemove != null) {
                list.remove(toRemove);
            }
            forms.put(pkgName, list);
        }
    }
    
    public void deleteFormItem(String pkgName, String formItemId) {
        List<Map.Entry<String, FormItemRepresentation>> list = items.get(pkgName);
        if (list != null) {
            Map.Entry<String, FormItemRepresentation> toRemove = null;
            for (Map.Entry<String, FormItemRepresentation> item: list) {
                if (formItemId.equals(item.getKey())) {
                    toRemove = item;
                    break;
                }
            }
            if (toRemove != null) {
                list.remove(toRemove);
            }
            items.put(pkgName, list);
        }
    }
}
