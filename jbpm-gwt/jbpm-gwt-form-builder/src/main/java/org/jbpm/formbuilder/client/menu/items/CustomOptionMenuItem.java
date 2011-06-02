package org.jbpm.formbuilder.client.menu.items;

import java.util.List; 

import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.menu.FBMenuItem;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Label;

public class CustomOptionMenuItem extends FBMenuItem {

    private final String newMenuOptionName;
    private final FBFormItem cloneableItem;
    
    public CustomOptionMenuItem(FBFormItem cloneableItem, String newMenuOptionName, List<FBFormEffect> formEffects) {
        super(formEffects);
        this.cloneableItem = cloneableItem;
        this.newMenuOptionName = newMenuOptionName;
        repaint();
    }

    @Override
    protected ImageResource getIconUrl() {
        return FormBuilderResources.INSTANCE.questionIcon();
    }

    @Override
    protected Label getDescription() {
        return new Label(newMenuOptionName);
    }

    @Override
    public FBMenuItem cloneWidget() {
        return new CustomOptionMenuItem(cloneableItem, newMenuOptionName, getFormEffects());
    }

    @Override
    public FBFormItem buildWidget() {
        return cloneableItem.cloneItem();
    }
}