package org.jbpm.formbuilder.client.menu;

import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.form.TableLayoutFormItem;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Label;

public class TableLayoutMenuItem extends FBMenuItem {

    @Override
    public FBMenuItem cloneWidget() {
        return new TableLayoutMenuItem();
    }

    @Override
    protected ImageResource getIconUrl() {
        return FormBuilderResources.INSTANCE.tableLayoutIcon();
    }

    @Override
    protected Label getDescription() {
        return new Label("Table layout");
    }

    @Override
    public FBFormItem buildWidget() {
        return new TableLayoutFormItem();
    }

}
