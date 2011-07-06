package org.jbpm.formbuilder.client.menu.items;

import java.util.List;

import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.form.items.LineGraphFormItem;
import org.jbpm.formbuilder.client.menu.FBMenuItem;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Label;

/*
 * TODO not implemented yet. Ment for displaying line graphics in a form 
 */
public class LineGraphMenuItem extends FBMenuItem {

    public LineGraphMenuItem() {
        super();
    }
    
    public LineGraphMenuItem(List<FBFormEffect> formEffects) {
        super(formEffects);
    }
    
    @Override
    protected ImageResource getIconUrl() {
        return FormBuilderResources.INSTANCE.lineGraph();
    }

    @Override
    public Label getDescription() {
        return new Label("Line Graph");
    }

    @Override
    public FBMenuItem cloneWidget() {
        return new LineGraphMenuItem(getFormEffects());
    }

    @Override
    public FBFormItem buildWidget() {
        return new LineGraphFormItem(getFormEffects());
    }

}
