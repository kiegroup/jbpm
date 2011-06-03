package org.jbpm.formbuilder.client.bus;

import org.jbpm.formbuilder.client.menu.FBMenuItem;

import com.google.gwt.event.shared.GwtEvent;

public class MenuOptionRemoveEvent extends GwtEvent<MenuOptionRemoveEventHandler> {

    public static final Type<MenuOptionRemoveEventHandler> TYPE = new Type<MenuOptionRemoveEventHandler>();
    
    private final FBMenuItem menuItem;
    private final String groupName;
    
    public MenuOptionRemoveEvent(FBMenuItem menuItem, String groupName) {
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
    public Type<MenuOptionRemoveEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(MenuOptionRemoveEventHandler handler) {
        handler.onEvent(this);
    }

}
