package org.jbpm.formbuilder.client.menu.items;

import java.util.List;

import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.form.items.RadioButtonFormItem;
import org.jbpm.formbuilder.client.menu.FBMenuItem;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Label;

public class RadioButtonMenuItem extends FBMenuItem {

    public RadioButtonMenuItem() {
        super();
    }
    
    public RadioButtonMenuItem(List<FBFormEffect> formEffects) {
        super(formEffects);
    }

    @Override
    protected ImageResource getIconUrl() {
        return FormBuilderResources.INSTANCE.radioButton();
    }

    @Override
    public Label getDescription() {
        return new Label("Radio buttons");
    }

    @Override
    public FBMenuItem cloneWidget() {
        return new RadioButtonMenuItem(super.getFormEffects());
    }

    @Override
    public FBFormItem buildWidget() {
        return new RadioButtonFormItem(super.getFormEffects());
    }

}
