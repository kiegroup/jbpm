package org.jbpm.formbuilder.client.bus;

import org.jbpm.formbuilder.client.menu.FBMenuItem;

import com.google.gwt.event.shared.GwtEvent;

public class MenuItemAddedEvent extends GwtEvent<MenuItemAddedEventHandler> {

    public static final Type<MenuItemAddedEventHandler> TYPE = new Type<MenuItemAddedEventHandler>();
    
    private final FBMenuItem menuItem;
    private final String groupName;
    
    public MenuItemAddedEvent(FBMenuItem menuItem, String groupName) {
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
    public Type<MenuItemAddedEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(MenuItemAddedEventHandler handler) {
        handler.onEvent(this);
    }

}
