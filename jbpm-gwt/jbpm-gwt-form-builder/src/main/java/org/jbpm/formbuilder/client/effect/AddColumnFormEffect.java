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
public class AddColumnFormEffect extends FBFormEffect {

    private final EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
    
    public AddColumnFormEffect() {
        super(FormBuilderGlobals.getInstance().getI18n().AddColumnEffectLabel(), false);
    }
    
    @Override
    public boolean isValidForItem(FBFormItem item) {
        return super.isValidForItem(item) && item instanceof TableLayoutFormItem;
    }

    @Override
    protected void createStyles() {
        final Map<String, Object> dataSnapshot = new HashMap<String, Object>();
        dataSnapshot.put("selectedX", getParent().getAbsoluteLeft());
        dataSnapshot.put("item", getItem());
        bus.fireEvent(new UndoableEvent(dataSnapshot, new UndoableHandler() {
            @Override
            public void undoAction(UndoableEvent event) {
                TableLayoutFormItem item = (TableLayoutFormItem) event.getData("item");
                Integer selectedX = (Integer) event.getData("selectedX");
                int columnNumber = item.getColumnForXCoordinate(selectedX);
                item.removeColumn(columnNumber);
            }
            @Override
            public void onEvent(UndoableEvent event) { }
            @Override
            public void doAction(UndoableEvent event) {
                TableLayoutFormItem item = (TableLayoutFormItem) event.getData("item");
                Integer selectedX = (Integer) event.getData("selectedX");
                int columnNumber = item.getColumnForXCoordinate(selectedX);
                item.addColumn(columnNumber);
            }
        }));
    }
}
