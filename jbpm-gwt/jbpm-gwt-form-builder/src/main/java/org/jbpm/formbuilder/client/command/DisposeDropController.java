package org.jbpm.formbuilder.client.command;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.formbuilder.client.bus.UndoableEvent;
import org.jbpm.formbuilder.client.bus.UndoableEventHandler;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.drop.SimpleDropController;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class DisposeDropController extends SimpleDropController {

    private final EventBus bus;
    
    public DisposeDropController(Widget dropTarget) {
        super(dropTarget);
        this.bus = FormBuilderGlobals.getInstance().getEventBus();
    }

    @Override
    public void onDrop(DragContext context) {
        Widget panel = context.draggable.getParent();
        Map<String, Object> dataSnapshot = new HashMap<String, Object>();
        dataSnapshot.put("draggableObject", context.draggable);
        dataSnapshot.put("panel", panel);
        this.bus.fireEvent(new UndoableEvent(dataSnapshot, new UndoableEventHandler() {
            public void onEvent(UndoableEvent event) {  }
            public void undoAction(UndoableEvent event) {
                Panel panel = (Panel) event.getData("panel");
                panel.add((Widget) event.getData("draggableObject"));
            }
            public void doAction(UndoableEvent event) {
                Widget widget = (Widget) event.getData("draggableObject");
                widget.removeFromParent();
            }
        }));
    }
}
