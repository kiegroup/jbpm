package org.jbpm.formbuilder.client.toolbar;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ToolbarDialog extends DialogBox {

    private final Label warning = new Label(""); 
    private final Button okButton = new Button("Continue");
    
    public ToolbarDialog(String warningText) {
        super(false, true);
        warning.setText(warningText);
        VerticalPanel panel = new VerticalPanel();
        panel.add(warning);
        HorizontalPanel buttons = new HorizontalPanel();
        Button cancelButton = new Button("Cancel");
        cancelButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                hide();
            }
        });
        okButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                hide();
            }
        });
        buttons.add(okButton);
        buttons.add(cancelButton);
        panel.add(buttons);
        add(panel);
        int height = RootPanel.getBodyElement().getClientHeight();
        int width = RootPanel.getBodyElement().getClientWidth();
        setSize("300px", "100px");
        setPopupPosition((width - 150) / 2, (height - 50) / 2);
    }
    
    public void addOkButtonHandler(ClickHandler handler) {
        okButton.addClickHandler(handler);
    }
}
