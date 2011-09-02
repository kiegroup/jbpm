package org.jbpm.formbuilder.client.validation;

import java.util.Map;

import org.jbpm.formbuilder.client.FormBuilderException;
import org.jbpm.formbuilder.client.messages.I18NConstants;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.shared.api.FBValidation;
import org.jbpm.formbuilder.shared.api.validation.IsIntegerValidation;

import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.gwtent.reflection.client.Reflectable;

@Reflectable
public class IsIntegerValidationItem extends FBValidationItem {

    private final I18NConstants i18n = FormBuilderGlobals.getInstance().getI18n();

    @Override
    public String getName() {
        return i18n.IsIntegerValidationName();
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
        if (!(validation instanceof IsIntegerValidation)) {
            throw new FormBuilderException(i18n.RepNotOfType(validation.getClass().getName(), "IsIntegerValidation"));
        }
    }
    
    @Override
    public FBValidation createValidation() {
        return getRepresentation(new IsIntegerValidation());
    }

    @Override
    public Widget createDisplay() {
        return null;
    }

    @Override
    public FBValidationItem cloneItem() {
        IsIntegerValidationItem item = new IsIntegerValidationItem();
        item.populatePropertiesMap(getPropertiesMap());
        return item;
    }
}
