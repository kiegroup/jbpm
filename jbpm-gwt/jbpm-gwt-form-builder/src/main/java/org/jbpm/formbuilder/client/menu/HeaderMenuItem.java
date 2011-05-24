package org.jbpm.formbuilder.client.menu;

import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.form.HeaderFormItem;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Label;

public class HeaderMenuItem extends FBMenuItem {

    @Override
    public FBMenuItem cloneWidget() {
        return new HeaderMenuItem();
    }

    @Override
    protected ImageResource getIconUrl() {
        return FormBuilderResources.INSTANCE.header();
    }

    @Override
    protected Label getDescription() {
        return new Label("Header");
    }

    @Override
    public FBFormItem buildWidget() {
        return new HeaderFormItem();
    }

}
