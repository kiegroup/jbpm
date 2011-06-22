package org.jbpm.formbuilder.client.menu.items;

import java.util.List;

import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.form.items.ConditionalBlockFormItem;
import org.jbpm.formbuilder.client.menu.FBMenuItem;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Label;

public class ConditionalBlockMenuItem extends FBMenuItem {

    public ConditionalBlockMenuItem(List<FBFormEffect> effects) {
        super(effects);
    }
    
    @Override
    protected ImageResource getIconUrl() {
        return FormBuilderResources.INSTANCE.conditionalBlock();
    }

    @Override
    public Label getDescription() {
        return new Label("Conditional Block");
    }

    @Override
    public FBMenuItem cloneWidget() {
        return new ConditionalBlockMenuItem(getFormEffects());
    }

    @Override
    public FBFormItem buildWidget() {
        return new ConditionalBlockFormItem(getFormEffects());
    }

}
