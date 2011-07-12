package org.jbpm.formbuilder.client.validation;

import java.util.Map;

import org.jbpm.formbuilder.client.FormBuilderException;
import org.jbpm.formbuilder.shared.rep.FBValidation;
import org.jbpm.formbuilder.shared.rep.validation.NotEmptyValidation;

import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class NotEmptyValidationItem extends FBValidationItem {

    @Override
    public String getName() {
        return "Not Empty";
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
        if (!(validation instanceof NotEmptyValidation)) {
            throw new FormBuilderException("validation should be of type NotEmptyValidation but is of type " + validation.getClass().getName());
        }
    }
    
    @Override
    public FBValidation createValidation() {
        return getRepresentation(new NotEmptyValidation());
    }

    @Override
    public Widget createDisplay() {
        return null;
    }

    @Override
    public FBValidationItem cloneItem() {
        NotEmptyValidationItem item = new NotEmptyValidationItem();
        item.populatePropertiesMap(getPropertiesMap());
        return item;
    }

}
