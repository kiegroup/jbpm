package org.jbpm.formbuilder.client.effect;

import org.jbpm.formbuilder.client.form.FBFormItem;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

public abstract class FBFormEffect {

    private FBFormItem item;
    private Widget widget;
    
    private final String name;
    private final boolean hasSubMenu;
    
    public FBFormEffect(String name, boolean hasSubMenu) {
        this.name = name;
        this.hasSubMenu = hasSubMenu;
    }

    public String getName() {
        return name;
    }
    
    public void apply(FBFormItem item) {
        item.addEffect(this);
        this.item = item;
        if (!hasSubMenu) {
            createStyles();
        }
    }
    
    public void remove(FBFormItem item) {
        item.removeEffect(this);
    }
    
    protected abstract void createStyles();

    public PopupPanel createPanel() {
        return null;
    }
    
    protected FBFormItem getItem() {
        return this.item;
    }
    
    public void setWidget(Widget widget) {
        this.widget = widget;
    }
    
    public Widget getWidget() {
        return widget;
    }
    
    public boolean isValidForItem(FBFormItem item) {
        return true;
    }
}
