package org.jbpm.formbuilder.client.effect;

import org.jbpm.formbuilder.client.bus.MenuOptionAddedEvent;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.menu.FBMenuItem;
import org.jbpm.formbuilder.client.menu.items.CustomOptionMenuItem;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class SaveAsMenuOptionFormEffect extends FBFormEffect {

    private String newMenuOptionName;
    
    public SaveAsMenuOptionFormEffect() {
        super(createImage(), true);
    }

    public void setNewMenuOptionName(String newMenuOptionName) {
        this.newMenuOptionName = newMenuOptionName;
    }
    
    public static Image createImage() {
        Image img = new Image(FormBuilderResources.INSTANCE.saveAsMenuOptionEffect());
        img.setAltText("Save as menu option");
        img.setTitle("Save as menu option");
        return img;
    }
    
    @Override
    protected void createStyles() {
        final FBFormItem formItem = super.getItem();
        FBMenuItem menuItem = new CustomOptionMenuItem(formItem, newMenuOptionName, formItem.getFormEffects());
        MenuOptionAddedEvent event = new MenuOptionAddedEvent(menuItem, "Custom");
        FormBuilderGlobals.getInstance().getEventBus().fireEvent(event);
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
