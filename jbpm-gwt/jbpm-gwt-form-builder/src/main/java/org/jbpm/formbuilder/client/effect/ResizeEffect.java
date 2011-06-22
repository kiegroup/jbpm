package org.jbpm.formbuilder.client.effect;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.formbuilder.client.bus.UndoableEvent;
import org.jbpm.formbuilder.client.bus.UndoableEventHandler;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.common.handler.ResizeEvent;
import org.jbpm.formbuilder.common.handler.ResizeEventHandler;
import org.jbpm.formbuilder.common.panels.ResizablePanel;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Widget;

public class ResizeEffect extends FBFormEffect {

    private int widgetWidth;
    private int widgetHeight;
    
    public ResizeEffect() {
        super("Resize", false);
    }
    
    @Override
    protected void createStyles() {
        FBFormItem item = getItem();
        widgetHeight = item.getOffsetHeight() + 20;
        widgetWidth = item.getOffsetWidth() + 20;
        Widget actualWidget = getItem().getWidget();
        ResizablePanel resizable = new ResizablePanel(actualWidget, widgetWidth, widgetHeight);
        resizable.addResizeHandler(new ResizeEventHandler() {
            public void onResize(ResizeEvent event) {
                undoableEvent(event, getItem());
                getItem().clear();
                getItem().setWidget(event.getWidget());
            }
        });
        getItem().clear();
        getItem().setWidget(resizable);
        resizable.setSize("" + widgetWidth + "px", "" + widgetHeight + "px");
    }
    
    protected void undoableEvent(ResizeEvent event, FBFormItem item) {
        Map<String, Object> dataSnapshot = new HashMap<String, Object>();
        EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
        dataSnapshot.put("item", item);
        dataSnapshot.put("oldWidth", item.getWidth());
        dataSnapshot.put("oldHeight", item.getHeight());
        dataSnapshot.put("newWidth", "" + event.getWidth() + "px");
        dataSnapshot.put("newHeight", "" + event.getHeight() + "px");
        bus.fireEvent(new UndoableEvent(dataSnapshot, new UndoableEventHandler() {
            public void onEvent(UndoableEvent event) { /* do nothing */ }
            public void undoAction(UndoableEvent event) {
                FBFormItem item = (FBFormItem) event.getData("item");
                String oldWidth = (String) event.getData("oldWidth");
                String oldHeight = (String) event.getData("oldHeight");
                item.setSize(oldWidth, oldHeight);
            }
            public void doAction(UndoableEvent event) {
                FBFormItem item = (FBFormItem) event.getData("item");
                String newWidth = (String) event.getData("newWidth");
                String newHeight = (String) event.getData("newHeight");
                item.setSize(newWidth, newHeight);
            }
        }));
        
        
    }
}
