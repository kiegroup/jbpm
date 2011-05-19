package org.jbpm.formbuilder.client.menu;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.formbuilder.client.bus.FormItemDeselectedEvent;
import org.jbpm.formbuilder.client.bus.FormItemSelectedEvent;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;

import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public abstract class FormItem implements IsWidget {

    private final FocusPanel panel = new FocusPanel();
    
    private boolean alreadyEditing = false;
    private Widget auxiliarWidget = null;
    
    public FormItem() {
        panel.addFocusHandler(new FocusHandler() {
            public void onFocus(FocusEvent event) {
                fireBusEvent(new FormItemSelectedEvent(FormItem.this));
                Widget w = createInplaceEditor();
                if (w != null && !isAlreadyEditing()) {
                    auxiliarWidget = panel.getWidget();
                    panel.clear();
                    panel.add(w);
                    setAlreadyEditing(true);
                }
            }
        });
    } 
    
    public boolean isAlreadyEditing() {
        return alreadyEditing;
    }

    public void setAlreadyEditing(boolean alreadyEditing) {
        this.alreadyEditing = alreadyEditing;
    }

    protected void reset() {
        if (auxiliarWidget != null) {
            panel.clear();
            panel.add(auxiliarWidget);
            setAlreadyEditing(false);
            fireBusEvent(new FormItemDeselectedEvent());
        }
    }
    
    public FocusPanel getPanel() {
        return panel;
    }
    
    public Widget asWidget() {
        return panel;
    }
    
    protected final void fireBusEvent(GwtEvent<?> event) {
        EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
        bus.fireEvent(event);
    }

    public abstract String asCode(String type);
    
    public abstract Widget createInplaceEditor();
    
    public Map<String, Object> getFormItemPropertiesMap() {
        return new HashMap<String, Object>();
    }

    public abstract void saveValues(Map<String, Object> asPropertiesMap);
}
