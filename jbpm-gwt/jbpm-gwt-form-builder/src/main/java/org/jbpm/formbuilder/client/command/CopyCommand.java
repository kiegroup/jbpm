package org.jbpm.formbuilder.client.command;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.formbuilder.client.bus.UndoableEvent;
import org.jbpm.formbuilder.client.bus.UndoableEventHandler;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;

import com.google.gwt.user.client.ui.MenuItem;

public class CopyCommand extends AbstractCopyPasteCommand {

    public CopyCommand() {
        super();
        FormBuilderGlobals.getInstance().register(this);
    }
    
    @Override
    protected void enable(MenuItem menuItem) {
        menuItem.setEnabled(getSelectedItem() != null);
    }
    
    public void execute() {
        Map<String, Object> dataSnapshot = new HashMap<String, Object>();
        dataSnapshot.put("selectedItem", getSelectedItem());
        dataSnapshot.put("oldMemory", AbstractCopyPasteCommand.getMemory());
        fireUndoableEvent(dataSnapshot, new UndoableEventHandler() {
            public void doAction(UndoableEvent event) {
                FBFormItem item = (FBFormItem) event.getData("selectedItem");
                if (item == null) {
                    AbstractCopyPasteCommand.setMemory(null);
                } else {
                    AbstractCopyPasteCommand.setMemory(item.cloneItem());
                }
                FormBuilderGlobals.getInstance().paste().enable();
            }
            public void undoAction(UndoableEvent event) {
                Object oldMemory = event.getData("oldMemory");
                AbstractCopyPasteCommand.setMemory(oldMemory);
                FormBuilderGlobals.getInstance().paste().enable();
            }
            public void onEvent(UndoableEvent event) { }
        });
    }
}
