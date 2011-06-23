package org.jbpm.formbuilder.client.form.editors;

import org.jbpm.formbuilder.client.form.items.ServerTransformationFormItem;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ServerScriptEditor extends VerticalPanel {

    private final ServerTransformationFormItem item;
    
    private final Button cancelButton = new Button("Cancel");
    private final Button okButton = new Button("Ok");
    private final TextArea editionArea = new TextArea();
    
    public ServerScriptEditor(ServerTransformationFormItem item) {
        this.item = item;
        this.editionArea.setCharacterWidth(50);
        this.editionArea.setVisibleLines(10);
        add(this.editionArea);
        add(createButtonsPanel());
    }
    
    private HorizontalPanel createButtonsPanel() { 
        HorizontalPanel panel = new HorizontalPanel();
        panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        okButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                item.reset();
            }
        });
        cancelButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                item.setScriptContent(editionArea.getValue());
                item.reset();
            }
        });
        panel.add(okButton);
        panel.add(cancelButton);
        return panel;
    }
}
