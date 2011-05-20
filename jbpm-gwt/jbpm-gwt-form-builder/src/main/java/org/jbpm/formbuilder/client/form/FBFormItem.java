package org.jbpm.formbuilder.client.form;

import java.util.HashMap; 
import java.util.Map;

import org.jbpm.formbuilder.client.bus.FormItemSelectionEvent;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

public abstract class FBFormItem extends FocusPanel {

    private boolean alreadyEditing = false;
    private Widget auxiliarWidget = null;
    
    public FBFormItem() {
        addFocusHandler(new FocusHandler() {
            public void onFocus(FocusEvent event) {
                fireSelectionEvent(new FormItemSelectionEvent(FBFormItem.this, true));
                Widget w = createInplaceEditor();
                if (w != null && !isAlreadyEditing()) {
                    auxiliarWidget = getWidget();
                    clear();
                    add(w);
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
            clear();
            add(auxiliarWidget);
            setAlreadyEditing(false);
            fireSelectionEvent(new FormItemSelectionEvent(null, false));
        }
    }
    
    protected final void fireSelectionEvent(FormItemSelectionEvent event) {
        EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
        bus.fireEvent(event);
    }

    public abstract String asCode(String type);
    
    public abstract Widget createInplaceEditor();
    
    public Map<String, Object> getFormItemPropertiesMap() {
        return new HashMap<String, Object>();
    }
    
    protected Image createDoneImage(ClickHandler handler) {
        final Image done = new Image(FormBuilderResources.INSTANCE.doneIcon());
        done.addClickHandler(handler);
        return done;
    }
    
    protected Image createRemoveImage() {
        final Image remove = new Image(FormBuilderResources.INSTANCE.removeIcon());
        remove.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                reset();
                removeFromParent();
            } 
        });
        return remove;
    }

    public abstract void saveValues(Map<String, Object> asPropertiesMap);
}
