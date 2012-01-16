package org.jbpm.formbuilder.client.menu.items;

import java.util.List;

import org.jbpm.formapi.client.effect.FBFormEffect;
import org.jbpm.formapi.client.form.FBFormItem;
import org.jbpm.formapi.client.menu.FBMenuItem;
import org.jbpm.formbuilder.client.FormBuilderGlobals;
import org.jbpm.formbuilder.client.form.items.FieldSetLayoutFormItem;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Label;
import com.gwtent.reflection.client.Reflectable;

@Reflectable
public class FieldSetLayoutMenuItem extends FBMenuItem {

	public FieldSetLayoutMenuItem() {
		super();
	}
	
	public FieldSetLayoutMenuItem(List<FBFormEffect> formEffects) {
		super(formEffects);
	}
	
	@Override
	protected ImageResource getIconUrl() {
		return FormBuilderResources.INSTANCE.fieldSet();
	}

	@Override
	public Label getDescription() {
		return new Label(FormBuilderGlobals.getInstance().getI18n().MenuItemFieldSet());
	}

	@Override
	public FBMenuItem cloneWidget() {
		return clone(new FieldSetLayoutMenuItem());
	}

	@Override
	public FBFormItem buildWidget() {
		return build(new FieldSetLayoutFormItem());
	}
}
