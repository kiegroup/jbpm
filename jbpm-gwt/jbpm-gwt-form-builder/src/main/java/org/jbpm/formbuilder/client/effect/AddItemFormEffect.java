package org.jbpm.formbuilder.client.effect;

import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.form.OptionsFormItem;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class AddItemFormEffect extends FBFormEffect {

    private String newLabel;
    private String newValue;
    
    public AddItemFormEffect() {
        super(createImage());
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

    private static Image createImage() {
        Image img = new Image(FormBuilderResources.INSTANCE.addItemIcon());
        img.setAltText("Add item");
        return img;
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
        panel.setSize("300px", "200px");
        VerticalPanel vPanel = new VerticalPanel();
        HorizontalPanel hPanel1 = new HorizontalPanel();
        hPanel1.add(new Label("New Item Label:"));
        final TextBox labelBox = new TextBox();
        labelBox.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                AddItemFormEffect.this.setNewLabel(labelBox.getValue());
            };
        });
        hPanel1.add(labelBox);
        HorizontalPanel hPanel2 = new HorizontalPanel();
        hPanel2.add(new Label("New Item Value:"));
        final TextBox valueBox = new TextBox();
        valueBox.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                AddItemFormEffect.this.setNewValue(valueBox.getValue());
            }
        });
        hPanel2.add(valueBox);
        Button applyButton = new Button("Apply");
        applyButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                AddItemFormEffect.this.setNewLabel(labelBox.getValue());
                AddItemFormEffect.this.setNewValue(valueBox.getValue());
                panel.hide();
            }
        });        
        vPanel.add(hPanel1);
        vPanel.add(hPanel2);
        vPanel.add(applyButton);
        panel.add(vPanel);
        return panel;
    }

}
