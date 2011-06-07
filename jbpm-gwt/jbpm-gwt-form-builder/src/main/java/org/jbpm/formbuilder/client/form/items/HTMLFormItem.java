package org.jbpm.formbuilder.client.form.items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.form.editors.HTMLFormItemEditor;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.items.HTMLRepresentation;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

public class HTMLFormItem extends FBFormItem {

    private HTML html = new HTML();
    
    public HTMLFormItem(List<FBFormEffect> formEffects) {
        super(formEffects);
        html.setHTML("<div style=\"background-color: #DDDDDD; \">HTML: Click to edit</div>");
        add(html);
        setWidth("200px");
        setHeight("100px");
        setSize(getWidth(),getHeight());
    }

    @Override
    public Map<String, Object> getFormItemPropertiesMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("width", getWidth());
        map.put("height", getHeight());
        return map;
    }
    
    @Override
    public Widget createInplaceEditor() {
        return new HTMLFormItemEditor(this);
    }
    
    @Override
    public void saveValues(Map<String, Object> asPropertiesMap) {
        this.setWidth(extractString(asPropertiesMap.get("width")));
        this.setHeight(extractString(asPropertiesMap.get("height")));
        populate(this.html);
    }

    private void populate(HTML html) {
        if (getWidth() != null) {
            html.setWidth(getWidth());
        }
        if (getHeight() != null) {
            html.setHeight(getHeight());
        }
    }

    public void setContent(String html) {
        this.html.setHTML(html);
    }
    
    @Override
    public FormItemRepresentation getRepresentation() {
        HTMLRepresentation rep = new HTMLRepresentation();
        rep.setWidth(getWidth());
        rep.setHeight(getHeight());
        rep.setContent(html.getHTML());
        return rep;
    }
    
    @Override
    public FBFormItem cloneItem() {
        HTMLFormItem clone = new HTMLFormItem(getFormEffects());
        clone.setHeight(getHeight());
        clone.setWidth(getWidth());
        clone.setContent(this.html.getHTML());
        clone.populate(clone.html);
        return clone;
    }

    public String getTextContent() {
        return this.html.getText();
    }
    
    public String getHtmlContent() {
        return this.html.getHTML();
    }
    
    public void setTextContent(String textContent) {
        this.html.setText(textContent);
    }
    
    public void setHtmlContent(String htmlContent) {
        this.html.setHTML(htmlContent);
    }
    
    @Override
    public Widget cloneDisplay() {
        HTML html = new HTML();
        html.setHTML(this.html.getHTML());
        populate(html);
        return html;
    }
}
