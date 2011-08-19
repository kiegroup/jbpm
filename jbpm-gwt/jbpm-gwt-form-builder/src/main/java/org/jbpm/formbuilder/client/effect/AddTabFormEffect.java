package org.jbpm.formbuilder.client.effect;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.formbuilder.client.bus.UndoableEvent;
import org.jbpm.formbuilder.client.bus.UndoableHandler;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.form.items.TabbedLayoutFormItem;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;

import com.google.gwt.event.shared.EventBus;
import com.gwtent.reflection.client.Reflectable;

@Reflectable
public class AddTabFormEffect extends FBFormEffect {
   
    private final EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
    
    public AddTabFormEffect() {
        super(FormBuilderGlobals.getInstance().getI18n().AddTabEffectLabel(), false);
    }    
    
    @Override
    protected void createStyles() {
        final Map<String, Object> dataSnapshot = new HashMap<String, Object>();
        dataSnapshot.put("selectedX", getParent().getAbsoluteLeft());
        dataSnapshot.put("selectedY", getParent().getAbsoluteTop());
        dataSnapshot.put("item", getItem());
        bus.fireEvent(new UndoableEvent(dataSnapshot, new UndoableHandler() {
            @Override
            public void undoAction(UndoableEvent event) {
                TabbedLayoutFormItem item = (TabbedLayoutFormItem) event.getData("item");
                Integer selectedX = (Integer) event.getData("selectedX");
                Integer selectedY = (Integer) event.getData("selectedY");
                int tabNumber = item.getTabForCoordinates(selectedX, selectedY);
                item.removeTab(tabNumber);
            }
            @Override
            public void onEvent(UndoableEvent event) { }
            @Override
            public void doAction(UndoableEvent event) {
                TabbedLayoutFormItem item = (TabbedLayoutFormItem) event.getData("item");
                Integer selectedX = (Integer) event.getData("selectedX");
                Integer selectedY = (Integer) event.getData("selectedY");
                int tabNumber = item.getTabForCoordinates(selectedX, selectedY);
                item.insertTab(tabNumber, null, null);
            }
        }));
    }

    @Override
    public boolean isValidForItem(FBFormItem item) {
        return super.isValidForItem(item) && item instanceof TabbedLayoutFormItem;
    }
    
}
