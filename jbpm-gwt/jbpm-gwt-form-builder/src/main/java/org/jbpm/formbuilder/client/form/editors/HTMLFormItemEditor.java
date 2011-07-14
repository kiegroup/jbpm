package org.jbpm.formbuilder.client.form.editors;

import org.jbpm.formbuilder.client.form.FBInplaceEditor;
import org.jbpm.formbuilder.client.form.items.HTMLFormItem;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

public class HTMLFormItemEditor extends FBInplaceEditor {

    private final HTMLFormItem formItem;
    private VerticalPanel panel = new VerticalPanel();
    private TextArea editorArea = new TextArea();
    private Button htmlButton = new Button("HTML");
    private Button textButton = new Button("Text");
    private Button doneButton = new Button("Done");
    
    private FocusWrapper wrapper = new FocusWrapper();
    
    public HTMLFormItemEditor(HTMLFormItem formItem) {
        this.formItem = formItem;
        HorizontalPanel buttonPanel = new HorizontalPanel();
        editorArea.setValue(this.formItem.getHtmlContent());
        editorArea.addBlurHandler(new BlurHandler() {
            public void onBlur(BlurEvent event) {
                wrapper.setValue(false);
            }
        });
        editorArea.addFocusHandler(new FocusHandler() {
            public void onFocus(FocusEvent event) {
                wrapper.setValue(true);
            }
        });
        this.htmlButton.setEnabled(false);
        buttonPanel.add(createTextButton());
        buttonPanel.add(createHtmlButton());
        editorArea.setCharacterWidth(50);
        editorArea.setVisibleLines(5);
        panel.add(buttonPanel);
        panel.add(editorArea);
        panel.add(createDoneButton());
        add(panel);
    }

    @Override
    public void focus() {
        editorArea.setFocus(true);
    }
    
    @Override
    public boolean isFocused() {
        return wrapper.getValue();
    }
    
    private Button createDoneButton() {
        this.doneButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                if (textButton.isEnabled()) {
                    String htmlContent = editorArea.getValue();
                    formItem.setHtmlContent(htmlContent);
                } else {
                    String textContent = editorArea.getValue();
                    formItem.setTextContent(textContent);
                }
                formItem.reset();
            }
        });
        return this.doneButton;
    }
    private Button createHtmlButton() {
        this.htmlButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                formItem.setTextContent(editorArea.getValue());
                editorArea.setValue(formItem.getHtmlContent());
                textButton.setEnabled(true);
                htmlButton.setEnabled(false);
            }
        });
        return this.htmlButton;
    }

    private Button createTextButton() {
        this.textButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                formItem.setHtmlContent(editorArea.getValue());
                editorArea.setValue(formItem.getTextContent());
                htmlButton.setEnabled(true);
                textButton.setEnabled(false);
            }
        });
        return this.textButton;
    }
}
