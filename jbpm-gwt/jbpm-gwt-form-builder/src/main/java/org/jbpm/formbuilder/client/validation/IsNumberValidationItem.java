package org.jbpm.formbuilder.client.validation;

import java.util.Map;

import org.jbpm.formbuilder.client.FormBuilderException;
import org.jbpm.formbuilder.client.messages.I18NConstants;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.shared.rep.FBValidation;
import org.jbpm.formbuilder.shared.rep.validation.IsNumberValidation;

import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class IsNumberValidationItem extends FBValidationItem {

    private final I18NConstants i18n = FormBuilderGlobals.getInstance().getI18n();

    @Override
    public String getName() {
        return i18n.IsNumberValidationName();
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
        if (!(validation instanceof IsNumberValidation)) {
            throw new FormBuilderException(i18n.RepNotOfType(validation.getClass().getName(), "IsNumberValidation"));
        }
    }
    
    @Override
    public FBValidation createValidation() {
        return getRepresentation(new IsNumberValidation());
    }

    @Override
    public Widget createDisplay() {
        return null;
    }

    @Override
    public FBValidationItem cloneItem() {
        IsNumberValidationItem item = new IsNumberValidationItem();
        item.populatePropertiesMap(getPropertiesMap());
        return item;
    }
}
