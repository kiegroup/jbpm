package org.jbpm.formbuilder.client.menu.items;

import java.util.List;

import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.form.ImageFormItem;
import org.jbpm.formbuilder.client.menu.FBMenuItem;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Label;

public class ImageMenuItem extends FBMenuItem {

    public ImageMenuItem(List<FBFormEffect> formEffects) {
        super(formEffects);
    }

    @Override
    protected ImageResource getIconUrl() {
        return FormBuilderResources.INSTANCE.image();
    }

    @Override
    protected Label getDescription() {
        return new Label("Image");
    }

    @Override
    public FBMenuItem cloneWidget() {
        return new ImageMenuItem(super.getFormEffects());
    }

    @Override
    public FBFormItem buildWidget() {
        return new ImageFormItem(super.getFormEffects());
    }

}
