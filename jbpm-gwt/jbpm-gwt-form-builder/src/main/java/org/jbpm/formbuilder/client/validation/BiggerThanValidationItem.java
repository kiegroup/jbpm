package org.jbpm.formbuilder.client.validation;

import java.util.Map;

import org.jbpm.formbuilder.client.FormBuilderException;
import org.jbpm.formbuilder.client.messages.I18NConstants;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.shared.rep.FBValidation;
import org.jbpm.formbuilder.shared.rep.validation.BiggerThanValidation;

import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class BiggerThanValidationItem extends FBValidationItem {

    private final I18NConstants i18n = FormBuilderGlobals.getInstance().getI18n();
    
    @Override
    public String getName() {
        return i18n.BiggerThanValidationName();
    }

    @Override
    public FBValidation createValidation() {
        return getRepresentation(new BiggerThanValidation());
    }

    @Override
    public Widget createDisplay() {
        return null;
    }

    @Override
    public FBValidationItem cloneItem() {
        BiggerThanValidationItem item = new BiggerThanValidationItem();
        item.populatePropertiesMap(getPropertiesMap());
        return item;
    }

    @Override
    public void populate(FBValidation validation) throws FormBuilderException {
        if (!(validation instanceof BiggerThanValidation)) {
            throw new FormBuilderException(i18n.RepNotOfType(validation.getClass().getName(), "BiggerThanValidation"));
        }
        TextBox valueTextBox = new TextBox();
        if (validation.getDataMap().get("value") != null) {
            valueTextBox.setValue(validation.getDataMap().get("value").toString());
        }
        super.getPropertiesMap().put("value", valueTextBox);
    }

    @Override
    public Map<String, HasValue<String>> getPropertiesMap() {
        Map<String, HasValue<String>> map = super.getPropertiesMap();
        if (!map.containsKey("errorMessage")) {
            map.put("errorMessage", new TextBox());
        }
        if (!map.containsKey("value")) {
            map.put("value", new TextBox());
        }
        return map;
    }
}
