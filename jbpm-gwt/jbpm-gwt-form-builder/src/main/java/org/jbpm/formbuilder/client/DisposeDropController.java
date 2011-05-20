package org.jbpm.formbuilder.client;

import com.allen_sauer.gwt.dnd.client.DragContext;
import com.allen_sauer.gwt.dnd.client.drop.SimpleDropController;
import com.google.gwt.user.client.ui.Widget;

public class DisposeDropController extends SimpleDropController {

    public DisposeDropController(Widget dropTarget) {
        super(dropTarget);
    }

    @Override
    public void onDrop(DragContext context) {
        context.draggable.removeFromParent();
    }
}
