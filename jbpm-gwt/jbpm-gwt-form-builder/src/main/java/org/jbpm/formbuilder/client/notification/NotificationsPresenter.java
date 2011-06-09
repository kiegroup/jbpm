package org.jbpm.formbuilder.client.notification;

import org.jbpm.formbuilder.client.bus.NotificationEvent;
import org.jbpm.formbuilder.client.bus.NotificationEventHandler;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.shared.EventBus;

public class NotificationsPresenter {

    private final NotificationsView view;
    private final EventBus bus = FormBuilderGlobals.getInstance().getEventBus();

    public NotificationsPresenter(NotificationsView notifView) {
        this.view = notifView;
        bus.addHandler(NotificationEvent.TYPE, new NotificationEventHandler() {
            public void onEvent(NotificationEvent event) {
                String colorCss = view.getColorCss(event.getLevel().name());
                String message = event.getMessage();
                Throwable error = event.getError();
                view.append(colorCss, message, error);
            }
        });
        view.addHandler(new FocusHandler() {
            public void onFocus(FocusEvent event) {
                view.saveHeight();
                view.setHeight("50%");
            }
        }, FocusEvent.getType());
        view.addHandler(new BlurHandler() {
            public void onBlur(BlurEvent event) {
                view.setHeight(view.getSavedHeight());
            }
        }, BlurEvent.getType());
    }
    
}
