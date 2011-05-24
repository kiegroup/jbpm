package org.jbpm.formbuilder.common.handler;

import com.google.gwt.event.dom.client.MouseEvent;
import com.google.gwt.user.client.Event;

public class RightClickEvent extends MouseEvent<RightClickHandler> {

    public static final Type<RightClickHandler> TYPE = new Type<RightClickHandler>("rclick", new RightClickEvent(null));
    
    private final Event event;
    
    public RightClickEvent(Event event) {
        this.event = event;
    }

    @Override
    public Type<RightClickHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(RightClickHandler handler) {
        handler.onRightClick(this);
    }

    @Override
    public final int getClientX() {
        return event.getClientX();
    }

    @Override
    public final int getClientY() {
        return event.getClientY();
    }

    @Override
    public final int getScreenX() {
        return event.getScreenX();
    }

    @Override
    public final int getScreenY() {
        return event.getScreenY();
    }

    
    
}
