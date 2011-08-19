package org.jbpm.formbuilder.client.effect;

import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.common.panels.MovablePanel;

import com.allen_sauer.gwt.dnd.client.DragEndEvent;
import com.allen_sauer.gwt.dnd.client.DragHandlerAdapter;
import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.user.client.ui.Widget;
import com.gwtent.reflection.client.Reflectable;

@Reflectable
public class MoveItemFormEffect extends FBFormEffect {

    private final PickupDragController dragController = FormBuilderGlobals.getInstance().getDragController();
    
    public MoveItemFormEffect() {
        super(FormBuilderGlobals.getInstance().getI18n().MoveItemEffectLabel(), false);
    }

    @Override
    protected void createStyles() {
        final FBFormItem item = getItem();
        final Widget actualWidget = item.getWidget();
        MovablePanel movable = new MovablePanel(actualWidget, item);
        dragController.makeDraggable(movable);
        dragController.addDragHandler(new DragHandlerAdapter() {
            @Override
            public void onDragEnd(DragEndEvent event) {
                item.clear();
                item.setWidget(actualWidget);
            }
        });
        item.clear();
        item.setWidget(movable);
    }
}
