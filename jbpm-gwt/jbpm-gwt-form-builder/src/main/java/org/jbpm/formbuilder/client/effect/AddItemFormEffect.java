package org.jbpm.formbuilder.client.effect;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.formbuilder.client.bus.UndoableEvent;
import org.jbpm.formbuilder.client.bus.UndoableEventHandler;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.form.OptionsFormItem;
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

public class AddItemFormEffect extends FBFormEffect {

    private String newLabel;
    private String newValue;
    
    public AddItemFormEffect() {
        super("Add item to list", true);
    }
    
    public String getNewLabel() {
        return newLabel;
    }

    public void setNewLabel(String newLabel) {
        this.newLabel = newLabel;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    @Override
    protected void createStyles() {
        FBFormItem item = super.getItem();
        if (item instanceof OptionsFormItem) {
            OptionsFormItem opt = (OptionsFormItem) item;
            opt.addItem(getNewLabel(), getNewValue());
        }
    }

    @Override
    public PopupPanel createPanel() {
        final PopupPanel panel = new PopupPanel();
        panel.setSize("150px", "100px");
        VerticalPanel vPanel = new VerticalPanel();
        HorizontalPanel hPanel1 = new HorizontalPanel();
        hPanel1.add(new Label("New Item Label:"));
        final TextBox labelBox = new TextBox();
        hPanel1.add(labelBox);
        HorizontalPanel hPanel2 = new HorizontalPanel();
        hPanel2.add(new Label("New Item Value:"));
        final TextBox valueBox = new TextBox();
        hPanel2.add(valueBox);
        Button applyButton = new Button("Apply");
        applyButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                Map<String, Object> dataSnapshot = new HashMap<String, Object>();
                dataSnapshot.put("labelBoxValue", labelBox.getValue());
                dataSnapshot.put("valueBoxValue", valueBox.getValue());
                dataSnapshot.put("item", getItem());
                EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
                bus.fireEvent(new UndoableEvent(dataSnapshot, new UndoableEventHandler() {
                    public void onEvent(UndoableEvent event) {  }
                    public void undoAction(UndoableEvent event) {
                        String key = (String) event.getData("labelBoxValue");
                        FBFormItem item = (FBFormItem) event.getData("item");
                        if (item instanceof OptionsFormItem) {
                            OptionsFormItem opt = (OptionsFormItem) item;
                            opt.deleteItem(key);
                        }
                    }
                    public void doAction(UndoableEvent event) {
                        String newLabel = (String) event.getData("labelBoxValue");
                        String newValue = (String) event.getData("valueBoxValue");
                        setNewLabel(newLabel);
                        setNewValue(newValue);
                        createStyles();
                    }
                }));
            }
        });        
        vPanel.add(hPanel1);
        vPanel.add(hPanel2);
        vPanel.add(applyButton);
        panel.add(vPanel);
        return panel;
    }

}
