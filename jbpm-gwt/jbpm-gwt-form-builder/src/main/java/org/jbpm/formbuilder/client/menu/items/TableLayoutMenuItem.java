package org.jbpm.formbuilder.client.menu.items;

import java.util.List;

import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.form.items.TableLayoutFormItem;
import org.jbpm.formbuilder.client.menu.FBMenuItem;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Label;

public class TableLayoutMenuItem extends FBMenuItem {

    public TableLayoutMenuItem(List<FBFormEffect> formEffects) {
        super(formEffects);
    }

    @Override
    public FBMenuItem cloneWidget() {
        return new TableLayoutMenuItem(super.getFormEffects());
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
        return new TableLayoutFormItem(super.getFormEffects());
    }

}
