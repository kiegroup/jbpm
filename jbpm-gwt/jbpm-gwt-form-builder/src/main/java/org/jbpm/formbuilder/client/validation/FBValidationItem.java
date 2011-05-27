package org.jbpm.formbuilder.client.validation;

import org.jbpm.formbuilder.shared.rep.FBValidation;

import com.google.gwt.user.client.ui.PopupPanel;

public abstract class FBValidationItem {

    public FBValidationItem() {
        
    }
    
    public abstract PopupPanel getDisplay();
    
    public abstract FBValidation getRepresentation();
    
}
