package org.jbpm.formbuilder.client.menu.items;

import java.util.List;

import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.form.items.LoopBlockFormItem;
import org.jbpm.formbuilder.client.menu.FBMenuItem;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Label;

public class LoopBlockMenuItem extends FBMenuItem {

    public LoopBlockMenuItem() {
    }
    
    public LoopBlockMenuItem(List<FBFormEffect> formEffects) {
        super(formEffects);
    }
    
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
        return new LoopBlockMenuItem(getFormEffects());
    }

    @Override
    public FBFormItem buildWidget() {
        return new LoopBlockFormItem(getFormEffects());
    }

}
