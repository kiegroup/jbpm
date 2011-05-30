package org.jbpm.formbuilder.client.effect;

import org.jbpm.formbuilder.client.bus.FormItemSelectionEvent;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;

import com.google.gwt.user.client.ui.Image;

public class RemoveEffect extends FBFormEffect {

    public RemoveEffect() {
        super(new Image(FormBuilderResources.INSTANCE.removeIcon()), false);
    }
    
    @Override
    protected void createStyles() {
        getItem().fireSelectionEvent(new FormItemSelectionEvent(getItem(), false));
        getItem().removeFromParent();
    }
}
