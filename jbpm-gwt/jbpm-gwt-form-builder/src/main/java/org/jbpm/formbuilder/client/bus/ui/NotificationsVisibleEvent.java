package org.jbpm.formbuilder.client.bus.ui;

import com.google.gwt.event.shared.GwtEvent;

public class NotificationsVisibleEvent extends GwtEvent<NotificationsVisibleHandler> {

    public static final Type<NotificationsVisibleHandler> TYPE = new Type<NotificationsVisibleHandler>();
    
    private final boolean visible;
    
    public NotificationsVisibleEvent(boolean visible) {
        super();
        this.visible = visible;
    }

    public boolean isVisible() {
        return visible;
    }
    
    @Override
    public Type<NotificationsVisibleHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(NotificationsVisibleHandler handler) {
        handler.onEvent(this);
    }

}
