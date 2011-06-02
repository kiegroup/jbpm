package org.jbpm.formbuilder.client.bus;

import org.jbpm.formbuilder.client.menu.FBMenuItem;

import com.google.gwt.event.shared.GwtEvent;

public class MenuOptionAddedEvent extends GwtEvent<MenuOptionAddedEventHandler> {

    public static final Type<MenuOptionAddedEventHandler> TYPE = new Type<MenuOptionAddedEventHandler>();
    
    private final FBMenuItem menuItem;
    private final String groupName;
    
    public MenuOptionAddedEvent(FBMenuItem menuItem, String groupName) {
        this.menuItem = menuItem;
        this.groupName = groupName;
    }
    
    public String getGroupName() {
        return groupName;
    }
    
    public FBMenuItem getMenuItem() {
        return menuItem;
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
