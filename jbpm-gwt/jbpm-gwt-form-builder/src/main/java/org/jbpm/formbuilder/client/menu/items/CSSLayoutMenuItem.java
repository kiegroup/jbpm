package org.jbpm.formbuilder.client.menu.items;

import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.menu.FBMenuItem;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Label;

/*
 * TODO not implemented yet. Designed to rely on CSS to define structure.
 * Also could have a functionality similar to http://www.miglayout.com/ 
 */
public class CSSLayoutMenuItem extends FBMenuItem {

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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FBFormItem buildWidget() {
        // TODO Auto-generated method stub
        return null;
    }

}
