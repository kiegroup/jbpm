package org.jbpm.formbuilder.client.form;

import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.items.HTMLRepresentation;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class HTMLFormItem extends FBFormItem {

    private HTML html = new HTML();
    private Button htmlButton = new Button("HTML");
    private Button textButton = new Button("Text");
    
    private String width;
    private String height;
    
    public HTMLFormItem(List<FBFormEffect> formEffects) {
        super(formEffects);
        html.setSize("200px", "100px");
        html.setHTML("<div style=\"background-color: #DDDDDD; \">HTML: Click to edit</div>");
        add(html);
    }

    @Override
    public Map<String, Object> getFormItemPropertiesMap() {
        Map<String, Object> map = super.getFormItemPropertiesMap();
        map.put("width", this.width);
        map.put("height", this.height);
        return map;
    }
    
    @Override
    public Widget createInplaceEditor() {
        PopupPanel popup = new PopupPanel();
        VerticalPanel vPanel = new VerticalPanel();
        HorizontalPanel buttonPanel = new HorizontalPanel();
        TextArea editorArea = new TextArea();
        editorArea.setValue(html.getText());
        this.textButton.setEnabled(false);
        buttonPanel.add(createTextButton(editorArea));
        buttonPanel.add(createHtmlButton(editorArea));
        editorArea.setCharacterWidth(50);
        editorArea.setVisibleLines(5);
        vPanel.add(buttonPanel);
        vPanel.add(editorArea);
        popup.add(vPanel);
        return popup;
    }
    
    private Button createHtmlButton(final TextArea editorArea) {
        this.htmlButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                html.setText(editorArea.getValue());
                editorArea.setValue(html.getHTML());
                htmlButton.setEnabled(false);
                textButton.setEnabled(true);
            }
        });
        return this.htmlButton;
    }

    private Button createTextButton(final TextArea editorArea) {
        this.textButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                html.setHTML(editorArea.getValue());
                editorArea.setValue(html.getText());
                textButton.setEnabled(false);
                htmlButton.setEnabled(true);
            }
        });
        return this.textButton;
    }

    @Override
    public void saveValues(Map<String, Object> asPropertiesMap) {
        this.width = asPropertiesMap.get("width").toString();
        this.height = asPropertiesMap.get("height").toString();
        
        html.setWidth(this.width);
        html.setHeight(this.height);
    }

    @Override
    public FormItemRepresentation getRepresentation() {
        HTMLRepresentation rep = new HTMLRepresentation();
        rep.setWidth(width);
        rep.setHeight(height);
        rep.setContent(html.getHTML());
        return rep;
    }

}
