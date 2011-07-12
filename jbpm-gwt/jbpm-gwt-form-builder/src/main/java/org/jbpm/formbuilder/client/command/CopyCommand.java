package org.jbpm.formbuilder.client.command;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.formbuilder.client.bus.UndoableEvent;
import org.jbpm.formbuilder.client.bus.UndoableEventHandler;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;

public class CopyCommand extends OneMemoryObjectCommand {

    public CopyCommand() {
        super();
        FormBuilderGlobals.getInstance().register(this);
    }
    
    public void execute() {
        if (getSelectedItem() == null) {
            OneMemoryObjectCommand.setMemory(null);
        } else {
            OneMemoryObjectCommand.setMemory(getSelectedItem().cloneItem());
        }
        
        Map<String, Object> dataSnapshot = new HashMap<String, Object>();
        dataSnapshot.put("selectedItem", getSelectedItem());
        dataSnapshot.put("oldMemory", OneMemoryObjectCommand.getMemory());
        fireUndoableEvent(dataSnapshot, new UndoableEventHandler() {
            public void doAction(UndoableEvent event) {
                FBFormItem item = (FBFormItem) event.getData("selectedItem");
                if (item == null) {
                    OneMemoryObjectCommand.setMemory(null);
                } else {
                    OneMemoryObjectCommand.setMemory(item.cloneItem());
                    item.removeFromParent();
                }
            }
            public void undoAction(UndoableEvent event) {
                Object oldMemory = event.getData("oldMemory");
                OneMemoryObjectCommand.setMemory(oldMemory);
            }
            public void onEvent(UndoableEvent event) { }
        });
    }
}
