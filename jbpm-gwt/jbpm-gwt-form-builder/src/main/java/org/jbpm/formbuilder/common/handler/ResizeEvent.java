package org.jbpm.formbuilder.common.handler;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.Widget;

public class ResizeEvent extends GwtEvent<ResizeEventHandler> {

    public static final Type<ResizeEventHandler> TYPE = new Type<ResizeEventHandler>();
    
    private final Widget widget;
    private final int width;
    private final int height;
    
    public ResizeEvent(Widget widget, int width, int height) {
        this.widget = widget;
        this.width = width;
        this.height = height;
    }
    
    public Widget getWidget() {
        return widget;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }

    @Override
    public Type<ResizeEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(ResizeEventHandler handler) {
        handler.onResize(this);
    }

}
