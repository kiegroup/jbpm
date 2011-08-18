package org.jbpm.formbuilder.client.menu.items;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.form.items.TabbedLayoutFormItem;
import org.jbpm.formbuilder.client.menu.FBMenuItem;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Label;
import com.gwtent.reflection.client.Reflectable;

@Reflectable
public class TabbedLayoutMenuItem extends FBMenuItem {

    public TabbedLayoutMenuItem() {
        this(new ArrayList<FBFormEffect>());
    }
    
    public TabbedLayoutMenuItem(List<FBFormEffect> effects) {
        super(effects);
    }

    @Override
    protected ImageResource getIconUrl() {
        return FormBuilderResources.INSTANCE.tabbedLayoutIcon();
    }

    @Override
    public Label getDescription() {
        return new Label(i18n.MenuItemTabbedLayout());
    }

    @Override
    public FBMenuItem cloneWidget() {
        return new TabbedLayoutMenuItem(getFormEffects());
    }

    @Override
    public FBFormItem buildWidget() {
        return new TabbedLayoutFormItem(getFormEffects());
    }

}
