package org.jbpm.formbuilder.client.menu.items;

import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.menu.FBMenuItem;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Label;

/*
 * TODO not implemented yet. Ment for displaying line graphics in a form 
 */
public class LineGraphMenuItem extends FBMenuItem {

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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FBFormItem buildWidget() {
        // TODO Auto-generated method stub
        return null;
    }

}
