package org.jbpm.formbuilder.client.menu.items;

import java.util.List;

import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.form.items.HiddenFormItem;
import org.jbpm.formbuilder.client.menu.FBMenuItem;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Label;

public class HiddenMenuItem extends FBMenuItem {

    public HiddenMenuItem(List<FBFormEffect> formEffects) {
        super(formEffects);
    }

    @Override
    protected ImageResource getIconUrl() {
        return FormBuilderResources.INSTANCE.hidden();
    }

    @Override
    protected Label getDescription() {
        return new Label("Hidden Field");
    }
    
    @Override
    public FBMenuItem cloneWidget() {
        return new HiddenMenuItem(super.getFormEffects());
    }

    @Override
    public FBFormItem buildWidget() {
        return new HiddenFormItem(super.getFormEffects());
    }

}
