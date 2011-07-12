package org.jbpm.formbuilder.client.command;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.formbuilder.client.bus.UndoableEvent;
import org.jbpm.formbuilder.client.bus.UndoableEventHandler;
import org.jbpm.formbuilder.client.bus.ui.FormItemAddedEvent;
import org.jbpm.formbuilder.client.bus.ui.FormItemRemovedEvent;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.HasOneWidget;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.Widget;

public class CutCommand extends AbstractCopyPasteCommand {

    public CutCommand() {
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
        dataSnapshot.put("oldItemParent", getSelectedItem() == null ? null : getSelectedItem().getParent());
        dataSnapshot.put("oldMemory", AbstractCopyPasteCommand.getMemory());
        fireUndoableEvent(dataSnapshot, new UndoableEventHandler() {
            public void doAction(UndoableEvent event) {
                FBFormItem item = (FBFormItem) event.getData("selectedItem");
                if (item == null) {
                    AbstractCopyPasteCommand.setMemory(null);
                } else {
                    AbstractCopyPasteCommand.setMemory(item.cloneItem());
                    item.removeFromParent();
                    EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
                    bus.fireEvent(new FormItemRemovedEvent(item));
                }
                FormBuilderGlobals.getInstance().paste().enable();
            }
            public void undoAction(UndoableEvent event) {
                FBFormItem item = (FBFormItem) event.getData("selectedItem");
                Object oldMemory = event.getData("oldMemory");
                Widget oldParent = (Widget) event.getData("oldItemParent");
                AbstractCopyPasteCommand.setMemory(oldMemory);
                FormBuilderGlobals.getInstance().paste().enable();
                if (oldParent instanceof HasWidgets) {
                    HasWidgets oldParentPanel = (HasWidgets) oldParent;
                    oldParentPanel.add(item);
                } else if (oldParent instanceof HasOneWidget) {
                    HasOneWidget oldParentPanel = (HasOneWidget) oldParent;
                    oldParentPanel.setWidget(item);
                }
                EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
                bus.fireEvent(new FormItemAddedEvent(item, oldParent));
            }
            public void onEvent(UndoableEvent event) { }
        });
    }
}
