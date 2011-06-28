package org.jbpm.formbuilder.client.bus;

import org.jbpm.formbuilder.client.menu.FBMenuItem;

import com.google.gwt.event.shared.GwtEvent;

public class MenuItemRemoveEvent extends GwtEvent<MenuItemRemoveEventHandler> {

    public static final Type<MenuItemRemoveEventHandler> TYPE = new Type<MenuItemRemoveEventHandler>();
    
    private final FBMenuItem menuItem;
    private final String groupName;
    
    public MenuItemRemoveEvent(FBMenuItem menuItem, String groupName) {
        this.menuItem = menuItem;
        this.groupName = groupName;
    }
    
    public FBMenuItem getMenuItem() {
        return menuItem;
    }
    
    public String getGroupName() {
        return groupName;
    }

    @Override
    public Type<MenuItemRemoveEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(MenuItemRemoveEventHandler handler) {
        handler.onEvent(this);
    }

}
