package org.jbpm.formbuilder.client.menu.items;

import java.util.List;

import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.form.items.HorizontalLayoutFormItem;
import org.jbpm.formbuilder.client.menu.FBMenuItem;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Label;

public class HorizontalLayoutMenuItem extends FBMenuItem {

    public HorizontalLayoutMenuItem() {
        super();
    }
    
    public HorizontalLayoutMenuItem(List<FBFormEffect> formEffects) {
        super(formEffects);
    }

    @Override
    public FBMenuItem cloneWidget() {
        return new HorizontalLayoutMenuItem(super.getFormEffects());
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
        return new HorizontalLayoutFormItem(super.getFormEffects());
    }

}
