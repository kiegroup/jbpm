package org.jbpm.formbuilder.client.bus;

import org.jbpm.formbuilder.client.options.MainMenuOption;

import com.google.gwt.event.shared.GwtEvent;

public class MenuOptionAddedEvent extends GwtEvent<MenuOptionAddedEventHandler> {

    public static final Type<MenuOptionAddedEventHandler> TYPE = new Type<MenuOptionAddedEventHandler>();
    
    private final MainMenuOption option;
    
    public MenuOptionAddedEvent(MainMenuOption option) {
        super();
        this.option = option;
    }

    public MainMenuOption getOption() {
        return option;
    }
    
    @Override
    public Type<MenuOptionAddedEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(MenuOptionAddedEventHandler handler) {
        handler.onEvent(this);
    }

}
