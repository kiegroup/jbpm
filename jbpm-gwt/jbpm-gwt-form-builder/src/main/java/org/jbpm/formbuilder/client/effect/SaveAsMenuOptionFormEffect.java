package org.jbpm.formbuilder.client.effect;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.formbuilder.client.bus.MenuItemAddedEvent;
import org.jbpm.formbuilder.client.bus.MenuItemRemoveEvent;
import org.jbpm.formbuilder.client.bus.UndoableEvent;
import org.jbpm.formbuilder.client.bus.UndoableEventHandler;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.menu.FBMenuItem;
import org.jbpm.formbuilder.client.menu.items.CustomOptionMenuItem;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class SaveAsMenuOptionFormEffect extends FBFormEffect {

    private String newMenuOptionName;
    private EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
    
    public SaveAsMenuOptionFormEffect() {
        super("Save as menu option", true);
    }

    public void setNewMenuOptionName(String newMenuOptionName) {
        this.newMenuOptionName = newMenuOptionName;
    }
    
    @Override
    protected void createStyles() {
        final FBFormItem formItem = super.getItem();
        FBMenuItem menuItem = new CustomOptionMenuItem(formItem, newMenuOptionName, formItem.getFormEffects());
        //TODO fire UndoableEvent
        Map<String, Object> dataSnapshot = new HashMap<String, Object>();
        dataSnapshot.put("menuItem", menuItem);
        bus.fireEvent(new UndoableEvent(dataSnapshot, new UndoableEventHandler() {
            public void onEvent(UndoableEvent event) {  }
            public void undoAction(UndoableEvent event) {
                FBMenuItem menuItem = (FBMenuItem) event.getData("menuItem");
                MenuItemRemoveEvent mevent = new MenuItemRemoveEvent(menuItem, "Custom");
                FormBuilderGlobals.getInstance().getEventBus().fireEvent(mevent);
            }
            public void doAction(UndoableEvent event) {
                FBMenuItem menuItem = (FBMenuItem) event.getData("menuItem");
                MenuItemAddedEvent mevent = new MenuItemAddedEvent(menuItem, "Custom");
                FormBuilderGlobals.getInstance().getEventBus().fireEvent(mevent);
            }
        }));
        //END  fire UndoableEvent
    }

    @Override
    public PopupPanel createPanel() {
        final PopupPanel panel = new PopupPanel();
        panel.setSize("200px", "60px");
        VerticalPanel vPanel = new VerticalPanel();
        HorizontalPanel hPanel = new HorizontalPanel();
        hPanel.add(new Label("Option Name:"));
        final TextBox labelBox = new TextBox();
        hPanel.add(labelBox);
        Button applyButton = new Button("Apply");
        applyButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                setNewMenuOptionName(labelBox.getValue());
                createStyles();
                panel.hide();
            }
        });
        vPanel.add(hPanel);
        vPanel.add(applyButton);
        panel.add(vPanel);
        return panel;
    }
}
