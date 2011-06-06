package org.jbpm.formbuilder.client.form.editors;

import org.jbpm.formbuilder.client.form.items.HTMLFormItem;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

public class HTMLFormItemEditor extends VerticalPanel {

    private final HTMLFormItem formItem;
    private TextArea editorArea = new TextArea();
    private Button htmlButton = new Button("HTML");
    private Button textButton = new Button("Text");
    private Button doneButton = new Button("Done");

    
    public HTMLFormItemEditor(HTMLFormItem formItem) {
        this.formItem = formItem;
        HorizontalPanel buttonPanel = new HorizontalPanel();
        editorArea.setValue(this.formItem.getHtmlContent());
        this.htmlButton.setEnabled(false);
        buttonPanel.add(createTextButton());
        buttonPanel.add(createHtmlButton());
        editorArea.setCharacterWidth(50);
        editorArea.setVisibleLines(5);
        add(buttonPanel);
        add(editorArea);
        add(createDoneButton());
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
