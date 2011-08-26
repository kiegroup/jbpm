package org.jbpm.formbuilder.client.validation;

import java.util.Map;

import org.jbpm.formbuilder.client.FormBuilderException;
import org.jbpm.formbuilder.client.messages.I18NConstants;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.shared.rep.FBValidation;
import org.jbpm.formbuilder.shared.rep.validation.IsEmailValidation;

import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class IsEmailValidationItem extends FBValidationItem {

    private final I18NConstants i18n = FormBuilderGlobals.getInstance().getI18n();

    @Override
    public String getName() {
        return i18n.IsEmailValidationName();
    }

    @Override
    public Map<String, HasValue<String>> getPropertiesMap() {
        Map<String, HasValue<String>> map = super.getPropertiesMap();
        if (!map.containsKey("errorMessage")) {
            map.put("errorMessage", new TextBox());
        }
        return map;
    }

    @Override
    public void populate(FBValidation validation) throws FormBuilderException {
        if (!(validation instanceof IsEmailValidation)) {
            throw new FormBuilderException(i18n.RepNotOfType(validation.getClass().getName(), "IsEmailValidation"));
        }
    }
    
    @Override
    public FBValidation createValidation() {
        return getRepresentation(new IsEmailValidation());
    }

    @Override
    public Widget createDisplay() {
        return null;
    }

    @Override
    public FBValidationItem cloneItem() {
        IsEmailValidationItem item = new IsEmailValidationItem();
        item.populatePropertiesMap(getPropertiesMap());
        return item;
    }
}
