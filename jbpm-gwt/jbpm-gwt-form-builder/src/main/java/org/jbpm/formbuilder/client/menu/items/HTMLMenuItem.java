package org.jbpm.formbuilder.client.menu.items;

import java.util.List;

import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.form.items.HTMLFormItem;
import org.jbpm.formbuilder.client.menu.FBMenuItem;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Label;

public class HTMLMenuItem extends FBMenuItem {

    public HTMLMenuItem() {
        super();
    }
    
    public HTMLMenuItem(List<FBFormEffect> formEffects) {
        super(formEffects);
    }

    @Override
    protected ImageResource getIconUrl() {
        return FormBuilderResources.INSTANCE.html();
    }

    @Override
    public Label getDescription() {
        return new Label("HTML script");
    }

    @Override
    public FBMenuItem cloneWidget() {
        return new HTMLMenuItem(super.getFormEffects());
    }

    @Override
    public FBFormItem buildWidget() {
        return new HTMLFormItem(super.getFormEffects());
    }

}
