package org.jbpm.formbuilder.client.form.editors;

import org.jbpm.formbuilder.client.form.FBInplaceEditor;
import org.jbpm.formbuilder.client.form.items.ServerTransformationFormItem;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ServerScriptEditor extends FBInplaceEditor {

    private final ServerTransformationFormItem item;
    
    private final Button cancelButton = new Button("Cancel");
    private final Button okButton = new Button("Ok");
    private final TextArea editionArea = new TextArea();
    
    private final FocusWrapper wrapper = new FocusWrapper();
    
    public ServerScriptEditor(ServerTransformationFormItem item) {
        VerticalPanel panel = new VerticalPanel();
        this.item = item;
        this.editionArea.setCharacterWidth(50);
        this.editionArea.setVisibleLines(10);
        this.editionArea.setValue(item.getScriptContent());
        editionArea.addBlurHandler(new BlurHandler() {
            public void onBlur(BlurEvent event) {
                wrapper.setValue(false);
            }
        });
        editionArea.addFocusHandler(new FocusHandler() {
            public void onFocus(FocusEvent event) {
                wrapper.setValue(true);
            }
        });
        panel.add(this.editionArea);
        panel.add(createButtonsPanel());
        add(panel);
    }
    
    @Override
    public void focus() {
        this.editionArea.setFocus(true);
    }
    
    @Override
    public boolean isFocused() {
        return wrapper.getValue();
    }
    
    private HorizontalPanel createButtonsPanel() { 
        HorizontalPanel panel = new HorizontalPanel();
        panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        okButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                item.setScriptContent(editionArea.getValue());
                item.reset();
            }
        });
        cancelButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                item.reset();
            }
        });
        panel.add(okButton);
        panel.add(cancelButton);
        return panel;
    }
}
