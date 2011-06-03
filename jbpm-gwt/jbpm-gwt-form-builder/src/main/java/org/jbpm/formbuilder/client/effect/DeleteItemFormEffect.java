package org.jbpm.formbuilder.client.effect;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.formbuilder.client.bus.UndoableEvent;
import org.jbpm.formbuilder.client.bus.UndoableEventHandler;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.form.OptionsFormItem;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class DeleteItemFormEffect extends FBFormEffect {

    private String dropItemLabel;
    private final EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
    
    public DeleteItemFormEffect() {
        super(createImage(), true);
    }

    private static Image createImage() {
        Image image = new Image(FormBuilderResources.INSTANCE.deleteItemIcon());
        image.setAltText("Delete Item");
        return image;
    }

    public void setDropItemLabel(String dropItemLabel) {
        this.dropItemLabel = dropItemLabel;
    }
    
    public String getDropItemLabel() {
        return this.dropItemLabel;
    }
    
    @Override
    protected void createStyles() {
        FBFormItem item = super.getItem();
        if (item instanceof OptionsFormItem) {
            OptionsFormItem opt = (OptionsFormItem) item;
            opt.deleteItem(getDropItemLabel());
        }
    }
    
    protected void revertStyles(String label, String value) {
        FBFormItem item = super.getItem();
        if (item instanceof OptionsFormItem) {
            OptionsFormItem opt = (OptionsFormItem) item;
            opt.addItem(label, value);
        }
    }
    
    protected String getValue(String label) {
        FBFormItem item = super.getItem();
        String value = null;
        if (item instanceof OptionsFormItem) {
            OptionsFormItem opt = (OptionsFormItem) item;
            value = opt.getItems().get(label);
        }
        return value;
    }
    
    @Override
    public PopupPanel createPanel() {
        final PopupPanel panel = new PopupPanel();
        panel.setSize("150px", "66px");
        VerticalPanel vPanel = new VerticalPanel();
        HorizontalPanel hPanel1 = new HorizontalPanel();
        hPanel1.add(new Label("Label to delete:"));
        final TextBox labelBox = new TextBox();
        labelBox.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                undoableEffect(panel, labelBox.getValue());
            }
        });
        hPanel1.add(labelBox);
        Button applyButton = new Button("Apply");
        applyButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                undoableEffect(panel, labelBox.getValue());
            }
        });        
        vPanel.add(hPanel1);
        vPanel.add(applyButton);
        panel.add(vPanel);
        return panel;
    }

    private void undoableEffect(final PopupPanel panel, final String label) {
        Map<String, Object> dataSnapshot = new HashMap<String, Object>();
        dataSnapshot.put("deletedLabel", label);
        dataSnapshot.put("deletedValue", getValue(label));
        bus.fireEvent(new UndoableEvent(dataSnapshot, new UndoableEventHandler() {
            public void onEvent(UndoableEvent event) {  }
            public void undoAction(UndoableEvent event) {
                String label = (String) event.getData("deletedLabel");
                String value = (String) event.getData("deletedValue");
                revertStyles(label, value);
                panel.hide();
            }
            public void doAction(UndoableEvent event) {
                String label = (String) event.getData("deletedLabel");
                DeleteItemFormEffect.this.setDropItemLabel(label);
                createStyles();
                panel.hide();
            }
        }));
    }
}
