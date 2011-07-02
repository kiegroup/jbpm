package org.jbpm.formbuilder.client.menu.items;

import java.util.List;

import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.form.items.BorderLayoutFormItem;
import org.jbpm.formbuilder.client.menu.FBMenuItem;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Label;

/*
 * TODO yet to be implemented. Inspired by Swing Border Layout (http://download.oracle.com/javase/tutorial/uiswing/layout/using.html)
 */
public class BorderLayoutMenuItem extends FBMenuItem {

	// NORTH, NORTHEAST, EAST, SOUTHEAST, SOUTH, SOUTHWEST, WEST, NORTHWEST, CENTER;
	
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
