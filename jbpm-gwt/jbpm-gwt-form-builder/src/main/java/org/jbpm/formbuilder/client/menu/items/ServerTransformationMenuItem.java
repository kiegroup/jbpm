package org.jbpm.formbuilder.client.menu.items;

import java.util.List;

import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.form.items.ServerTransformationFormItem;
import org.jbpm.formbuilder.client.menu.FBMenuItem;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Label;

public class ServerTransformationMenuItem extends FBMenuItem {

    public ServerTransformationMenuItem() {
    }
    
    public ServerTransformationMenuItem(List<FBFormEffect> formEffects) {
        super(formEffects);
    }
    
    @Override
    protected ImageResource getIconUrl() {
        return FormBuilderResources.INSTANCE.transformationBlock();
    }

    @Override
    public Label getDescription() {
        return new Label("Server script");
    }

    @Override
    public FBMenuItem cloneWidget() {
        return new ServerTransformationMenuItem(getFormEffects());
    }

    @Override
    public FBFormItem buildWidget() {
        return new ServerTransformationFormItem(getFormEffects());
    }

}
