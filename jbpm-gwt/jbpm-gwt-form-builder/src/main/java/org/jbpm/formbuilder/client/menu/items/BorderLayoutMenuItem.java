package org.jbpm.formbuilder.client.menu.items;

import java.util.List;

import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.form.items.BorderLayoutFormItem;
import org.jbpm.formbuilder.client.menu.FBMenuItem;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Label;

public class BorderLayoutMenuItem extends FBMenuItem {

	public BorderLayoutMenuItem() {
		super();
	}
	
    public BorderLayoutMenuItem(List<FBFormEffect> formEffects) {
		super(formEffects);
	}

	@Override
    protected ImageResource getIconUrl() {
        return FormBuilderResources.INSTANCE.borderLayoutIcon();
    }

    @Override
    public Label getDescription() {
        return new Label("Border Layout");
    }

    @Override
    public FBMenuItem cloneWidget() {
        return new BorderLayoutMenuItem(getFormEffects());
    }

    @Override
    public FBFormItem buildWidget() {
        return new BorderLayoutFormItem(getFormEffects());
    }

}
