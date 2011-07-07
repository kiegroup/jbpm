package org.jbpm.formbuilder.client.effect;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.formbuilder.client.bus.FormItemSelectionEvent;
import org.jbpm.formbuilder.client.bus.UndoableEvent;
import org.jbpm.formbuilder.client.bus.UndoableEventHandler;
import org.jbpm.formbuilder.client.bus.ui.FormItemAddedEvent;
import org.jbpm.formbuilder.client.bus.ui.FormItemRemovedEvent;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Panel;

public class RemoveEffect extends FBFormEffect {

    private final EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
    
    public RemoveEffect() {
        super("Remove item", false);
    }
    
    @Override
    protected void createStyles() {
        getItem().fireSelectionEvent(new FormItemSelectionEvent(getItem(), false));
        Map<String, Object> dataSnapshot = new HashMap<String, Object>();
        dataSnapshot.put("itemParent", getItem().getParent());
        dataSnapshot.put("item", getItem());
        bus.fireEvent(new UndoableEvent(dataSnapshot, new UndoableEventHandler() {
            public void onEvent(UndoableEvent event) {  }
            public void undoAction(UndoableEvent event) {
                Panel panel = (Panel) event.getData("itemParent");
                FBFormItem item = (FBFormItem) event.getData("item");
                panel.add(item);
                bus.fireEvent(new FormItemAddedEvent(item, panel));
            }
            public void doAction(UndoableEvent event) {
                FBFormItem item = (FBFormItem) event.getData("item");
                item.removeFromParent();
                bus.fireEvent(new FormItemRemovedEvent(item));
            }
        }));
    }
}
