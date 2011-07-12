package org.jbpm.formbuilder.client.command;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.formbuilder.client.bus.UndoableEvent;
import org.jbpm.formbuilder.client.bus.UndoableEventHandler;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.form.items.LayoutFormItem;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;

public class PasteCommand extends OneMemoryObjectCommand {

    public PasteCommand() {
        super();
        FormBuilderGlobals.getInstance().register(this);
    }
    
    public void execute() {
        Map<String, Object> dataSnapshot = new HashMap<String, Object>();
        dataSnapshot.put("itemToHold", getSelectedItem() == null ? null : getSelectedItem().cloneItem());
        dataSnapshot.put("memory", OneMemoryObjectCommand.getMemory());
        fireUndoableEvent(dataSnapshot, new UndoableEventHandler() {
            public void doAction(UndoableEvent event) {
                FBFormItem itemToHold = (FBFormItem) event.getData("itemToHold");
                Object obj = event.getData("memory");
                if (itemToHold != null && obj instanceof FBFormItem) {
                    FBFormItem itemToPaste = (FBFormItem) obj;
                    if (itemToHold instanceof LayoutFormItem) {
                        ((LayoutFormItem) itemToHold).add(itemToPaste);
                    } else {
                        itemToHold.add(itemToPaste);
                    }
                }
            }
            public void onEvent(UndoableEvent event) { }
            public void undoAction(UndoableEvent event) {
                FBFormItem itemToHold = (FBFormItem) event.getData("itemToHold");
                Object obj = event.getData("memory");
                if (itemToHold != null && obj instanceof FBFormItem) {
                    FBFormItem itemToPaste = (FBFormItem) obj;
                    itemToPaste.removeFromParent();
                }
            }
        });
        
    }
}
