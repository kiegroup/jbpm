package org.jbpm.formbuilder.client.menu;

import org.jbpm.formbuilder.client.bus.FormItemSelectedEvent;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;

import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public abstract class FormItem implements IsWidget {

    private final FocusPanel panel = new FocusPanel();
    
    public FormItem() {
        panel.addFocusHandler(new FocusHandler() {
            public void onFocus(FocusEvent event) {
                EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
                bus.fireEvent(new FormItemSelectedEvent(FormItem.this));
            }
        });
    } 
    
    public FocusPanel getPanel() {
        return panel;
    }
    
    public Widget asWidget() {
        return panel;
    }
    
    public abstract String asCode(String type);
}
