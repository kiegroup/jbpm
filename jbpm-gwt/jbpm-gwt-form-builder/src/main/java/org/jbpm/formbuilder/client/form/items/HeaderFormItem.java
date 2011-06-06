package org.jbpm.formbuilder.client.form.items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.items.HeaderRepresentation;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class HeaderFormItem extends FBFormItem {


    private final HTML header = new HTML("<h1>Header</h1>");
    
    private String id;
    private String name;
    private String width;
    private String height;
    private String cssClassName;
    
    public HeaderFormItem(List<FBFormEffect> formEffects) {
        super(formEffects);
        add(getHeader());
    }
    
    @Override
    public Map<String, Object> getFormItemPropertiesMap() {
        Map<String, Object> formItemPropertiesMap = new HashMap<String, Object>();
        formItemPropertiesMap.put("id", id);
        formItemPropertiesMap.put("name", name);
        formItemPropertiesMap.put("width", width);
        formItemPropertiesMap.put("height", height);
        formItemPropertiesMap.put("cssClassName", cssClassName);
        return formItemPropertiesMap;
    }

    @Override
    public Widget createInplaceEditor() {
        final HorizontalPanel editPanel = new HorizontalPanel();
        editPanel.setBorderWidth(1);
        final TextBox textBox = new TextBox();
        textBox.setValue(getHeader().getText());
        textBox.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                setContent("<h1>" + textBox.getValue() + "</h1>");
                reset();
            }
        });
        editPanel.add(textBox);
        return editPanel;
    }

    @Override
    public void saveValues(Map<String, Object> propertiesMap) {
        this.id = propertiesMap.get("id").toString();
        this.name = propertiesMap.get("name").toString();
        this.width = propertiesMap.get("width").toString();
        this.height = propertiesMap.get("height").toString();
        this.cssClassName = propertiesMap.get("cssClassName").toString();
        populate();
    }

    private void populate() {
        if (this.width != null) {
            getHeader().setWidth(this.width);
        }
        if (this.height != null) {
            getHeader().setHeight(this.height);
        }
        if (this.cssClassName != null) {
            getHeader().setStyleName(this.cssClassName);
        }
    }
    
    protected HTML getHeader() {
        return this.header;
    }
    
    protected void setContent(String html) {
        getHeader().setHTML(html);
    }
    
    @Override
    public void addEffect(FBFormEffect effect) {
        super.addEffect(effect);
        effect.setWidget(this.header);
    }
    
    @Override
    public FormItemRepresentation getRepresentation() {
        HeaderRepresentation rep = new HeaderRepresentation();
        rep.setValue(this.header.getText());
        rep.setStyleClass(this.cssClassName);
        rep.setCssId(this.id);
        rep.setCssName(this.name);
        rep.setWidth(this.width);
        rep.setHeight(this.height);
        return rep;
    }
    
    @Override
    public FBFormItem cloneItem() {
        HeaderFormItem clone = new HeaderFormItem(getFormEffects());
        clone.cssClassName = this.cssClassName;
        clone.height = this.height;
        clone.id = this.id;
        clone.name = this.name;
        clone.width = this.width;
        clone.setContent(this.header.getHTML());
        clone.populate();
        return clone;
    }
}
