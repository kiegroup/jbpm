package org.jbpm.formbuilder.client.validation;

import java.util.List;

public interface OtherValidationsAware {

    void setExistingValidations(List<FBValidationItem> existingValidations);
}
