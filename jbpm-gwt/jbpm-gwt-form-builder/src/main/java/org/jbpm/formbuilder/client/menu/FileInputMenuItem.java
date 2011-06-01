package org.jbpm.formbuilder.client.menu;

import java.util.List;

import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.form.FileInputFormItem;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Label;

public class FileInputMenuItem extends FBMenuItem {

    public FileInputMenuItem(List<FBFormEffect> formEffects) {
        super(formEffects);
    }

    @Override
    protected ImageResource getIconUrl() {
        return FormBuilderResources.INSTANCE.fileInput();
    }

    @Override
    protected Label getDescription() {
        return new Label("File Input");
    }

    @Override
    public FBMenuItem cloneWidget() {
        return new FileInputMenuItem(super.getFormEffects());
    }

    @Override
    public FBFormItem buildWidget() {
        return new FileInputFormItem(super.getFormEffects());
    }

    
} 
