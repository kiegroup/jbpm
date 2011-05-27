package org.jbpm.formbuilder.client.menu;

import org.jbpm.formbuilder.client.form.ComboBoxFormItem;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Label;

public class ComboBoxMenuItem extends FBMenuItem {

    @Override
    public FBMenuItem cloneWidget() {
        return new ComboBoxMenuItem();
    }

    @Override
    protected ImageResource getIconUrl() {
        return FormBuilderResources.INSTANCE.comboBox();
    }

    @Override
    protected Label getDescription() {
        return new Label("Combo Box");
    }

    @Override
    public FBFormItem buildWidget() {
        return new ComboBoxFormItem();
    }

}
