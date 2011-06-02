package org.jbpm.formbuilder.client.menu;

import com.allen_sauer.gwt.dnd.client.DragController;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class FBMenuPanel extends VerticalPanel {

    private DragController dragController;

    public FBMenuPanel(DragController dragController) {
        this.dragController = dragController;
        setSpacing(2);
        setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
    }

    /**
     * Overloaded method that makes widgets draggable.
     * 
     * @param w
     *            the widget to be added are made draggable
     */
    public void add(FBMenuItem menuItem) {
        this.dragController.makeDraggable(menuItem);
        super.add(menuItem);
    }

    /**
     * Removed widgets that are instances of {@link PaletteWidget} are
     * immediately replaced with a cloned copy of the original.
     * 
     * @param w
     *            the widget to remove
     * @return true if a widget was removed
     */
    @Override
    public boolean remove(Widget w) {
        int index = getWidgetIndex(w);
        if (index != -1 && w instanceof FBMenuItem) {
            FBMenuItem clone = ((FBMenuItem) w).cloneWidget();
            dragController.makeDraggable(clone);
            insert(clone, index);
        }
        return super.remove(w);
    }
}
