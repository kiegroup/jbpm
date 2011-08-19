package org.jbpm.formbuilder.client.effect;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.formbuilder.client.bus.UndoableEvent;
import org.jbpm.formbuilder.client.bus.UndoableHandler;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.form.items.TableLayoutFormItem;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;

import com.google.gwt.event.shared.EventBus;
import com.gwtent.reflection.client.Reflectable;

@Reflectable
public class AddRowFormEffect extends FBFormEffect {

    private final EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
    
    public AddRowFormEffect() {
        super(FormBuilderGlobals.getInstance().getI18n().AddRowEffectLabel(), false);
    }
    
    @Override
    protected void createStyles() {
        final Map<String, Object> dataSnapshot = new HashMap<String, Object>();
        dataSnapshot.put("selectedY", getParent().getAbsoluteTop());
        dataSnapshot.put("item", getItem());
        bus.fireEvent(new UndoableEvent(dataSnapshot, new UndoableHandler() {
            @Override
            public void undoAction(UndoableEvent event) {
                TableLayoutFormItem item = (TableLayoutFormItem) event.getData("item");
                Integer selectedY = (Integer) event.getData("selectedY");
                int rowNumber = item.getRowForYCoordinate(selectedY);
                item.removeRow(rowNumber);
            }
            @Override
            public void onEvent(UndoableEvent event) { }
            @Override
            public void doAction(UndoableEvent event) {
                TableLayoutFormItem item = (TableLayoutFormItem) event.getData("item");
                Integer selectedY = (Integer) event.getData("selectedY");
                int rowNumber = item.getRowForYCoordinate(selectedY);
                item.addRow(rowNumber);
            }
        }));
    }

    @Override
    public boolean isValidForItem(FBFormItem item) {
        return super.isValidForItem(item) && item instanceof TableLayoutFormItem;
    }
}
