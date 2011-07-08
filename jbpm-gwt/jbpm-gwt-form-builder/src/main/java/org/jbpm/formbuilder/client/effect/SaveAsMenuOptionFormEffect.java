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
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class SaveAsMenuOptionFormEffect extends FBFormEffect {

    private String newMenuOptionName;
    private String groupName;
    private EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
    
    public SaveAsMenuOptionFormEffect() {
        super("Save as menu option", true);
    }

    public void setNewMenuOptionName(String newMenuOptionName) {
        this.newMenuOptionName = newMenuOptionName;
    }
    
    public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
    
    @Override
    protected void createStyles() {
        final FBFormItem formItem = super.getItem();
        FBMenuItem menuItem = new CustomOptionMenuItem(formItem, newMenuOptionName, formItem.getFormEffects(), groupName);
        Map<String, Object> dataSnapshot = new HashMap<String, Object>();
        dataSnapshot.put("menuItem", menuItem);
        dataSnapshot.put("groupName", groupName);
        bus.fireEvent(new UndoableEvent(dataSnapshot, new UndoableEventHandler() {
            public void onEvent(UndoableEvent event) {  }
            public void undoAction(UndoableEvent event) {
                FBMenuItem menuItem = (FBMenuItem) event.getData("menuItem");
                String groupName = (String) event.getData("groupName");
                MenuItemRemoveEvent mevent = new MenuItemRemoveEvent(menuItem, groupName);
                FormBuilderGlobals.getInstance().getEventBus().fireEvent(mevent);
            }
            public void doAction(UndoableEvent event) {
                FBMenuItem menuItem = (FBMenuItem) event.getData("menuItem");
                String groupName = (String) event.getData("groupName");
                MenuItemAddedEvent mevent = new MenuItemAddedEvent(menuItem, groupName);
                FormBuilderGlobals.getInstance().getEventBus().fireEvent(mevent);
            }
        }));
    }

    @Override
    public PopupPanel createPanel() {
        final PopupPanel panel = new PopupPanel(true);
        panel.setSize("250px", "90px");
        VerticalPanel vPanel = new VerticalPanel();
        Grid grid = new Grid(2, 2);
        final TextBox optionNameBox = new TextBox();
        final TextBox groupNameBox = new TextBox();
        grid.setWidget(0, 0, new Label("Option Name:"));
        grid.setWidget(0, 1, optionNameBox);
        grid.setWidget(1, 0, new Label("Group Name:"));
        grid.setWidget(1, 1, groupNameBox);
        
        Button applyButton = new Button("Apply");
        applyButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                setNewMenuOptionName(optionNameBox.getValue());
                setGroupName(groupNameBox.getValue());
                createStyles();
                panel.hide();
            }
        });
        vPanel.add(grid);
        vPanel.add(applyButton);
        panel.add(vPanel);
        return panel;
    }
}
