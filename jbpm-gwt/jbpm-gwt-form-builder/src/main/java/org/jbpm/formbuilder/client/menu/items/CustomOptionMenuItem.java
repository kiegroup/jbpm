package org.jbpm.formbuilder.client.menu.items;

import java.util.List;

import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.menu.FBMenuItem;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Label;

public class CustomOptionMenuItem extends FBMenuItem {

    private String newMenuOptionName;
    private FBFormItem cloneableItem;

    public CustomOptionMenuItem() {
        //needs a default constructor for reconstruction from xml in GWT
    }
    
    public CustomOptionMenuItem(FBFormItem cloneableItem, String newMenuOptionName, List<FBFormEffect> formEffects) {
        super(formEffects);
        this.cloneableItem = cloneableItem;
        this.newMenuOptionName = newMenuOptionName;
        repaint();
    }

    public String getNewMenuOptionName() {
        return newMenuOptionName;
    }
    
    public FBFormItem getCloneableItem() {
        return cloneableItem;
    }
    
    public void setCloneableItem(FBFormItem cloneableItem) {
        this.cloneableItem = cloneableItem;
    }
    
    public void setNewMenuOptionName(String newMenuOptionName) {
        this.newMenuOptionName = newMenuOptionName;
        repaint();
    }
    
    @Override
    protected ImageResource getIconUrl() {
        return FormBuilderResources.INSTANCE.questionIcon();
    }

    @Override
    public Label getDescription() {
        return new Label(newMenuOptionName);
    }

    @Override
    public FBMenuItem cloneWidget() {
        return new CustomOptionMenuItem(cloneableItem, newMenuOptionName, getFormEffects());
    }

    @Override
    public void addEffect(FBFormEffect effect) {
        super.addEffect(effect);
        this.cloneableItem.addEffect(effect);
    }
    
    @Override
    public FBFormItem buildWidget() {
        return cloneableItem.cloneItem();
    }
}