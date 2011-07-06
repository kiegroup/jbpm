package org.jbpm.formbuilder.client.menu.items;

import java.util.List;

import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.form.items.CSSLayoutFormItem;
import org.jbpm.formbuilder.client.menu.FBMenuItem;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Label;

public class CSSLayoutMenuItem extends FBMenuItem {

    public CSSLayoutMenuItem() {
        super();
    }
    
    public CSSLayoutMenuItem(List<FBFormEffect> formEffects) {
        super(formEffects);
    }

    @Override
    protected ImageResource getIconUrl() {
        return FormBuilderResources.INSTANCE.cssLayoutIcon();
    }

    @Override
    public Label getDescription() {
        return new Label("CSS Layout");
    }

    @Override
    public FBMenuItem cloneWidget() {
        return new CSSLayoutMenuItem(getFormEffects());
    }

    @Override
    public FBFormItem buildWidget() {
        return new CSSLayoutFormItem(getFormEffects());
    }

}
