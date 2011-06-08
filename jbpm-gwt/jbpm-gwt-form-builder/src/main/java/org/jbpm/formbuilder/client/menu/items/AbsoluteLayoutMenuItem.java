package org.jbpm.formbuilder.client.menu.items;

import java.util.List;

import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.form.items.AbsoluteLayoutFormItem;
import org.jbpm.formbuilder.client.menu.FBMenuItem;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Label;

public class AbsoluteLayoutMenuItem extends FBMenuItem {

    public AbsoluteLayoutMenuItem() {
        super();
    }
    
    public AbsoluteLayoutMenuItem(List<FBFormEffect> formEffects) {
        super(formEffects);
    }

    @Override
    protected ImageResource getIconUrl() {
        return FormBuilderResources.INSTANCE.absoluteLayoutIcon();
    }

    @Override
    protected Label getDescription() {
        return new Label("Absolute Layout");
    }

    @Override
    public FBMenuItem cloneWidget() {
        return new AbsoluteLayoutMenuItem(super.getFormEffects());
    }

    @Override
    public FBFormItem buildWidget() {
        return new AbsoluteLayoutFormItem(super.getFormEffects());
    }

}
