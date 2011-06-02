package org.jbpm.formbuilder.client.options;

import org.jbpm.formbuilder.client.FormBuilderModel;


public class OptionsPresenter {

    private final FormBuilderModel model;
    private final OptionsView view;
    
    public OptionsPresenter(FormBuilderModel model, OptionsView view) {
        this.model = model;
        this.view = view;
        this.view.addItems(this.model.getCurrentOptions());
    }
}
