package org.jbpm.formbuilder.client.menu.items;

import java.util.List;

import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.form.items.TextAreaFormItem;
import org.jbpm.formbuilder.client.menu.FBMenuItem;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Label;

public class TextAreaMenuItem extends FBMenuItem {

    public TextAreaMenuItem() {
        super();
    }
    
    public TextAreaMenuItem(List<FBFormEffect> formEffects) {
        super(formEffects);
    }

    @Override
    protected ImageResource getIconUrl() {
        return FormBuilderResources.INSTANCE.textArea();
    }

    @Override
    protected Label getDescription() {
        return new Label("Text Area");
    }

    @Override
    public FBMenuItem cloneWidget() {
        return new TextAreaMenuItem(super.getFormEffects());
    }

    @Override
    public FBFormItem buildWidget() {
        return new TextAreaFormItem(super.getFormEffects());
    }

}
