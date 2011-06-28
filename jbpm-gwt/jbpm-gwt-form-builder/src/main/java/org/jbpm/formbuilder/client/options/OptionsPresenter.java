package org.jbpm.formbuilder.client.options;

import java.util.List;

import org.jbpm.formbuilder.client.bus.MenuOptionAddedEvent;
import org.jbpm.formbuilder.client.bus.MenuOptionAddedEventHandler;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;

import com.google.gwt.event.shared.EventBus;


public class OptionsPresenter {

    private final OptionsView view;
    private final EventBus bus;
    
    public OptionsPresenter(List<MainMenuOption> menuOptions, OptionsView view) {
        this.view = view;
        this.bus = FormBuilderGlobals.getInstance().getEventBus();
        this.view.addItems(menuOptions);
        
        bus.addHandler(MenuOptionAddedEvent.TYPE, new MenuOptionAddedEventHandler() {
            public void onEvent(MenuOptionAddedEvent event) {
                OptionsPresenter.this.view.addItem(event.getOption());
            }
        });
    }
}
