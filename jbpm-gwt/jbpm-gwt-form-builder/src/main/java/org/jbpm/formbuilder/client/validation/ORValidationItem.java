package org.jbpm.formbuilder.client.validation;

import java.util.List;

import org.jbpm.formbuilder.client.FormBuilderException;
import org.jbpm.formbuilder.client.messages.I18NConstants;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.shared.api.FBValidation;
import org.jbpm.formbuilder.shared.api.validation.ORValidation;

import com.google.gwt.user.client.ui.Widget;
import com.gwtent.reflection.client.Reflectable;

@Reflectable
public class ORValidationItem extends FBValidationItem implements OtherValidationsAware {

    private final I18NConstants i18n = FormBuilderGlobals.getInstance().getI18n();
    
    private SubValidationsList subValidations = null;
    
    @Override
    public String getName() {
        return i18n.ORValidationName();
    }
    
    @Override
    public void setExistingValidations(List<FBValidationItem> existingValidations) {
        subValidations = new SubValidationsList("OR", existingValidations);
    }

    @Override
    public FBValidation createValidation() {
        ORValidation validation = getRepresentation(new ORValidation());
        if (subValidations != null && subValidations.getItems() != null) {
            for (FBValidationItem subValidationItem : subValidations.getItems()) {
                FBValidation subValidation = subValidationItem.createValidation();
                validation.addValidation(subValidation);
            }
        }
        return validation;
    }

    @Override
    public Widget createDisplay() {
        return subValidations;
    }

    @Override
    public FBValidationItem cloneItem() {
        ORValidationItem clone = new ORValidationItem();
        for (FBValidationItem item : this.subValidations.getItems()) {
            clone.subValidations.addItem(item.cloneItem());
        }
        return clone;
    }

    @Override
    public void populate(FBValidation validation) throws FormBuilderException {
        if (!(validation instanceof ORValidation)) {
            throw new FormBuilderException(i18n.RepNotOfType(validation.getClass().getName(), "ORValidation"));
        }
        subValidations.clearItems();
        ORValidation or = (ORValidation) validation;
        List<FBValidation> subVals = or.getValidations();
        for (FBValidation subVal : subVals) {
            FBValidationItem item = createValidation(subVal);
            subValidations.addItem(item);
        }
    }

}
