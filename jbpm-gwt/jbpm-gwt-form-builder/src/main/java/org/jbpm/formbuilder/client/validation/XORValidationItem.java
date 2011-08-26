package org.jbpm.formbuilder.client.validation;

import java.util.List;

import org.jbpm.formbuilder.client.FormBuilderException;
import org.jbpm.formbuilder.client.messages.I18NConstants;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.shared.rep.FBValidation;
import org.jbpm.formbuilder.shared.rep.validation.XORValidation;

import com.google.gwt.user.client.ui.Widget;
import com.gwtent.reflection.client.Reflectable;

@Reflectable
public class XORValidationItem  extends FBValidationItem implements OtherValidationsAware {

    private final I18NConstants i18n = FormBuilderGlobals.getInstance().getI18n();
    
    private SubValidationsList subValidations = null;
    
    @Override
    public String getName() {
        return i18n.XORValidationName();
    }
    
    @Override
    public void setExistingValidations(List<FBValidationItem> existingValidations) {
        subValidations = new SubValidationsList("XOR", existingValidations);
    }

    @Override
    public FBValidation createValidation() {
        XORValidation validation = getRepresentation(new XORValidation());
        for (FBValidationItem subValidationItem : subValidations.getItems()) {
            FBValidation subValidation = subValidationItem.createValidation();
            validation.addValidation(subValidation);
        }
        return validation;
    }

    @Override
    public Widget createDisplay() {
        return subValidations;
    }

    @Override
    public FBValidationItem cloneItem() {
        XORValidationItem clone = new XORValidationItem();
        for (FBValidationItem item : this.subValidations.getItems()) {
            clone.subValidations.addItem(item.cloneItem());
        }
        return clone;
    }

    @Override
    public void populate(FBValidation validation) throws FormBuilderException {
        if (!(validation instanceof XORValidation)) {
            throw new FormBuilderException(i18n.RepNotOfType(validation.getClass().getName(), "XORValidation"));
        }
        subValidations.clearItems();
        XORValidation xor = (XORValidation) validation;
        List<FBValidation> subVals = xor.getValidations();
        for (FBValidation subVal : subVals) {
            FBValidationItem item = createValidation(subVal);
            subValidations.addItem(item);
        }
    }

}
