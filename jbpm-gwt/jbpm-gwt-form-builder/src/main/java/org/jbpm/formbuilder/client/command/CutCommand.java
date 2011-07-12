package org.jbpm.formbuilder.client.command;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.formbuilder.client.bus.UndoableEvent;
import org.jbpm.formbuilder.client.bus.UndoableEventHandler;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;

import com.google.gwt.user.client.ui.HasOneWidget;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

public class CutCommand extends OneMemoryObjectCommand {

    public CutCommand() {
        super();
        FormBuilderGlobals.getInstance().register(this);
    }
    
    public void execute() {
        Map<String, Object> dataSnapshot = new HashMap<String, Object>();
        dataSnapshot.put("selectedItem", getSelectedItem());
        dataSnapshot.put("oldItemParent", getSelectedItem() == null ? null : getSelectedItem().getParent());
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
                FBFormItem item = (FBFormItem) event.getData("selectedItem");
                Object oldMemory = event.getData("oldMemory");
                Widget oldParent = (Widget) event.getData("oldItemParent");
                OneMemoryObjectCommand.setMemory(oldMemory);
                if (oldParent instanceof HasWidgets) {
                    HasWidgets oldParentPanel = (HasWidgets) oldParent;
                    oldParentPanel.add(item);
                } else if (oldParent instanceof HasOneWidget) {
                    HasOneWidget oldParentPanel = (HasOneWidget) oldParent;
                    oldParentPanel.setWidget(item);
                }
            }
            public void onEvent(UndoableEvent event) { }
        });
    }
}
