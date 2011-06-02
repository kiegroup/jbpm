package org.jbpm.formbuilder.client.menu.items;

import java.util.List;

import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.form.items.CheckBoxFormItem;
import org.jbpm.formbuilder.client.menu.FBMenuItem;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Label;


public class CheckBoxMenuItem extends FBMenuItem {

    public CheckBoxMenuItem(List<FBFormEffect> formEffects) {
        super(formEffects);
    }

    @Override
    public FBMenuItem cloneWidget() {
        return new CheckBoxMenuItem(super.getFormEffects());
    }

    @Override
    protected ImageResource getIconUrl() {
        return FormBuilderResources.INSTANCE.checkBox();
    }

    @Override
    protected Label getDescription() {
        return new Label("Check Box");
    }

    @Override
    public FBFormItem buildWidget() {
        return new CheckBoxFormItem(super.getFormEffects());
    }
}
