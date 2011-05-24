package org.jbpm.formbuilder.client.effect;

import org.jbpm.formbuilder.client.form.FBFormItem;

import com.google.gwt.user.client.ui.Image;

public abstract class FBFormEffect {

    private FBFormItem item;
    
    private final Image image;
    
    public FBFormEffect(Image image) {
        this.image = image;
    }

    public Image getImage() {
        return image;
    }
    
    public void apply(FBFormItem item) {
        item.addEffect(this);
        this.item = item;
        createStyles();
    }
    
    public void remove(FBFormItem item) {
        item.removeEffect(this);
    }
    
    protected abstract void createStyles();
    
    protected FBFormItem getItem() {
        return this.item;
    }
}
