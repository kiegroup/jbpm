package org.jbpm.formbuilder.client.bus;

import org.jbpm.formbuilder.client.menu.FBMenuItem;

import com.google.gwt.event.shared.GwtEvent;

public class MenuItemFromServerEvent extends GwtEvent<MenuItemFromServerHandler> {

    public static final Type<MenuItemFromServerHandler> TYPE = new Type<MenuItemFromServerHandler>();
    
    private final FBMenuItem menuItem;
    private final String groupName;
    
    public MenuItemFromServerEvent(FBMenuItem menuItem, String groupName) {
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
    public Type<MenuItemFromServerHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(MenuItemFromServerHandler handler) {
        handler.onEvent(this);
    }

}
