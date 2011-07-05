package org.jbpm.formbuilder.client.menu.items;

import java.util.List;

import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.form.items.FlowLayoutFormItem;
import org.jbpm.formbuilder.client.menu.FBMenuItem;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Label;

public class FlowLayoutMenuItem extends FBMenuItem {

	public FlowLayoutMenuItem() {
		super();
	}
	
	public FlowLayoutMenuItem(List<FBFormEffect> effects) {
		super(effects);
	}
	
    @Override
    protected ImageResource getIconUrl() {
    	return FormBuilderResources.INSTANCE.flowLayoutIcon();
    }

    @Override
    public Label getDescription() {
        return new Label("Flow Layout");
    }

    @Override
    public FBMenuItem cloneWidget() {
        return new FlowLayoutMenuItem(getFormEffects());
    }

    @Override
    public FBFormItem buildWidget() {
        return new FlowLayoutFormItem(getFormEffects());
    }

}
