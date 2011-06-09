package org.jbpm.formbuilder.client.options;

import java.util.List;


public class OptionsPresenter {

    private final OptionsView view;
    
    public OptionsPresenter(List<MainMenuOption> menuOptions, OptionsView view) {
        this.view = view;
        this.view.addItems(menuOptions);
    }
}
