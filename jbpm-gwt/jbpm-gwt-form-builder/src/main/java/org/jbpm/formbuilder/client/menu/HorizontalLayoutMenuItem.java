package org.jbpm.formbuilder.client.menu;

import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.form.HorizontalLayoutFormItem;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Label;

public class HorizontalLayoutMenuItem extends FBMenuItem {

    @Override
    public FBMenuItem cloneWidget() {
        return new HorizontalLayoutMenuItem();
    }

    @Override
    protected ImageResource getIconUrl() {
        return FormBuilderResources.INSTANCE.horizontalLayoutIcon();
    }

    @Override
    protected Label getDescription() {
        return new Label("Horizontal layout");
    }

    @Override
    public FBFormItem buildWidget() {
        return new HorizontalLayoutFormItem();
    }

}
