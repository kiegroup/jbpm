package org.jbpm.formbuilder.client.bus;

import com.google.gwt.event.shared.GwtEvent;

public class NotificationEvent extends GwtEvent<NotificationEventHandler> {

    public static final Type<NotificationEventHandler> TYPE = new Type<NotificationEventHandler>();
    
    public static enum Level {
        INFO, WARN, ERROR;
    }
    
    private final Level level;
    private final String message;
    private final Throwable error;
    
    public NotificationEvent(String message) {
        this(Level.INFO, message, null);
    }
    
    public NotificationEvent(Level level, String message) {
        this(level, message, null);
    }
    
    public NotificationEvent(Level level, String message, Throwable error) {
        super();
        this.level = level;
        this.message = message;
        this.error = error;
    }

    public Throwable getError() {
        return error;
    }
    
    public Level getLevel() {
        return level;
    }
    
    public String getMessage() {
        return message;
    }

    @Override
    public Type<NotificationEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(NotificationEventHandler handler) {
        handler.onEvent(this);
    }
}
