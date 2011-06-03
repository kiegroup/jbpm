package org.jbpm.formbuilder.client.command;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.formbuilder.client.bus.UndoableEvent;
import org.jbpm.formbuilder.client.bus.UndoableEventHandler;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.layout.LayoutView;
import org.jbpm.formbuilder.client.menu.FBMenuItem;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.drop.AbstractDropController;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class DropFormItemController extends AbstractDropController {

    private final LayoutView layoutView;
    private final EventBus bus;
    
    public DropFormItemController(Widget dropTarget, LayoutView layoutView) {
        super(dropTarget);
        this.layoutView = layoutView;
        this.bus = FormBuilderGlobals.getInstance().getEventBus();
    }
    
    @Override
    public void onDrop(DragContext context) {
        Widget drag = context.draggable;
        int x = context.desiredDraggableX;
        int y = context.desiredDraggableY;
        if (drag != null && drag instanceof FBMenuItem) { //when you add a component from the menu
            FBMenuItem menuItem = (FBMenuItem) drag;
            FBFormItem formItem = menuItem.buildWidget();
            Map<String, Object> dataSnapshot = new HashMap<String, Object>();
            dataSnapshot.put("layoutView", layoutView);
            dataSnapshot.put("formItem", formItem);
            dataSnapshot.put("menuItem", menuItem);
            dataSnapshot.put("x", x);
            dataSnapshot.put("y", y);
            this.bus.fireEvent(new UndoableEvent(dataSnapshot, new UndoableEventHandler() {
                public void onEvent(UndoableEvent event) {  }
                public void undoAction(UndoableEvent event) {
                    FBFormItem formItem = (FBFormItem) event.getData("formItem");
                    LayoutView layoutView = (LayoutView) event.getData("layoutView");
                    Integer x = (Integer) event.getData("x");
                    Integer y = (Integer) event.getData("y");
                    Panel panel = layoutView.getUnderlyingLayout(x, y);
                    panel.remove(formItem);
                }
                public void doAction(UndoableEvent event) {
                    FBFormItem formItem = (FBFormItem) event.getData("formItem");
                    FBMenuItem menuItem = (FBMenuItem) event.getData("menuItem");
                    Integer x = (Integer) event.getData("x");
                    Integer y = (Integer) event.getData("y");
                    if (formItem != null) {
                        Panel panel = layoutView.getUnderlyingLayout(x, y);
                        panel.remove(menuItem);
                        panel.add(formItem);
                    }
                }
            }));
        }
    }
}
