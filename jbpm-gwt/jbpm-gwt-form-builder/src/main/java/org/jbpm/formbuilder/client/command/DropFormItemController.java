package org.jbpm.formbuilder.client.command;

import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.layout.LayoutView;
import org.jbpm.formbuilder.client.menu.FBMenuItem;

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.drop.AbstractDropController;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class DropFormItemController extends AbstractDropController {

    private final LayoutView layoutView;
    
    public DropFormItemController(Widget dropTarget, LayoutView layoutView) {
        super(dropTarget);
        this.layoutView = layoutView;
    }
    
    @Override
    public void onDrop(DragContext context) {
        Widget drag = context.draggable;
        int x = context.desiredDraggableX;
        int y = context.desiredDraggableY;
        if (drag != null && drag instanceof FBMenuItem) { //when you add a component from the menu
            FBMenuItem menuItem = (FBMenuItem) drag;
            FBFormItem formItem = menuItem.buildWidget();
            if (formItem != null) {
                Panel panel = layoutView.getUnderlyingLayout(x, y);
                panel.remove(menuItem);
                panel.add(formItem);
            }
        }
    }
}
