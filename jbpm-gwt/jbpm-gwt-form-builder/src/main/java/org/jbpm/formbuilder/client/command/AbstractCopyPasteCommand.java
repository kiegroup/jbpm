package org.jbpm.formbuilder.client.command;

import java.util.Map;

import org.jbpm.formbuilder.client.bus.FormItemSelectionEvent;
import org.jbpm.formbuilder.client.bus.FormItemSelectionEventHandler;
import org.jbpm.formbuilder.client.bus.UndoableEvent;
import org.jbpm.formbuilder.client.bus.UndoableEventHandler;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.MenuItem;

public abstract class AbstractCopyPasteCommand implements BaseCommand {

    private final EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
    
    private static Object memory;
    
    private FBFormItem selectedItem;
    private MenuItem menuItem;
    
    public AbstractCopyPasteCommand() {   
        bus.addHandler(FormItemSelectionEvent.TYPE, new FormItemSelectionEventHandler() {
            public void onEvent(FormItemSelectionEvent event) {
                if (event.isSelected()) {
                    setSelectedItem(event.getFormItemSelected());
                } else {
                    setSelectedItem(null);
                }
            }
        });
    }
    
    private void setSelectedItem(FBFormItem item) {
        this.selectedItem = item;
        enable(menuItem);
    }
    
    protected void enable() {
        enable(this.menuItem);
    }
    
    protected abstract void enable(MenuItem menuItem);
    
    public AbstractCopyPasteCommand append(FBFormItem selectedItem) {
        setSelectedItem(selectedItem);
        return this;
    }
    
    public FBFormItem getSelectedItem() {
        return selectedItem;
    }
 
    protected void fireUndoableEvent(Map<String, Object> dataSnapshot, UndoableEventHandler handler) {
        bus.fireEvent(new UndoableEvent(dataSnapshot, handler));
    }
    
    public void setItem(MenuItem item) {
        this.menuItem = item;
        setSelectedItem(getSelectedItem());
    }
    
    protected static Object getMemory() {
        return memory;
    }
    
    protected static void setMemory(Object object) {
        memory = object;
    }
}
