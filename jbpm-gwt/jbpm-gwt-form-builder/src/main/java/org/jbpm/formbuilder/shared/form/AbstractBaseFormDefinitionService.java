package org.jbpm.formbuilder.shared.form;

import org.jbpm.formbuilder.shared.rep.FormRepresentation;

public abstract class AbstractBaseFormDefinitionService implements FormDefinitionService {

    private static final String FORM_ID_PREFIX = "formDefinition_";
    private static final String ITEM_ID_PREFIX = "formItemDefinition_";
    
    /**
     * @param form FormRepresentation with name to be changed
     * @return true if its an update, false if it is an insert
     */
    protected boolean updateFormName(FormRepresentation form) {
        if (form.getName() == null || "null".equals(form.getName()) || "".equals(form.getName())) {
            form.setName(FORM_ID_PREFIX + System.currentTimeMillis());
            return false;
        } else if (!form.getName().startsWith(FORM_ID_PREFIX)){
            form.setName(FORM_ID_PREFIX + form.getName());
            return false;
        }
        return true;
    }
    
    protected boolean updateItemName(String formItemName, StringBuilder returnName) {
        if (formItemName == null || "null".equals(formItemName) || "".equals(formItemName)) {
            returnName.append(ITEM_ID_PREFIX).append(System.currentTimeMillis());
            return false;
        } else if (!formItemName.startsWith(ITEM_ID_PREFIX)){
            returnName.append(ITEM_ID_PREFIX).append(formItemName);
            return false;
        }
        returnName.append(formItemName);
        return true;
    }
    
    protected boolean isItemName(String assetId) {
        return assetId.startsWith(ITEM_ID_PREFIX);
    }
    
    protected boolean isFormName(String assetId) {
        return assetId.startsWith(FORM_ID_PREFIX);
    }
}
