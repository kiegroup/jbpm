package org.jbpm.formbuilder.client.notification;

public interface NotificationsView {

    interface Presenter {
        
    };
    
    String getColorCss(String name);

    void append(String colorCss, String message, Throwable error);

}
