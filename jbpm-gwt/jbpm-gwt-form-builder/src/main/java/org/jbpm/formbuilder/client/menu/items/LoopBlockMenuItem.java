package org.jbpm.formbuilder.client.menu.items;

import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.menu.FBMenuItem;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Label;

public class LoopBlockMenuItem extends FBMenuItem {

    @Override
    protected ImageResource getIconUrl() {
        return FormBuilderResources.INSTANCE.loopBlock();
    }

    @Override
    public Label getDescription() {
        return new Label("Loop block");
    }

    @Override
    public FBMenuItem cloneWidget() {
        return new LoopBlockMenuItem(); //TODO
    }

    @Override
    public FBFormItem buildWidget() {
        // TODO Auto-generated method stub
        return null;
    }

}
