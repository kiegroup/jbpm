package org.jbpm.formbuilder.client.menu.items;

import java.util.List;

import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.form.items.HeaderFormItem;
import org.jbpm.formbuilder.client.menu.FBMenuItem;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Label;

public class HeaderMenuItem extends FBMenuItem {

    public HeaderMenuItem() {
        super();
    }
    
    public HeaderMenuItem(List<FBFormEffect> formEffects) {
        super(formEffects);
    }

    @Override
    public FBMenuItem cloneWidget() {
        return new HeaderMenuItem(super.getFormEffects());
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
        return new HeaderFormItem(super.getFormEffects());
    }

}
