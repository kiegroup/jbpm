package org.jbpm.formbuilder.client.form;

import java.util.Map;

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
    
    public HeaderFormItem() {
        add(getHeader());
    }
    
    @Override
    public String asCode(String type) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public Map<String, Object> getFormItemPropertiesMap() {
        Map<String, Object> formItemPropertiesMap = super.getFormItemPropertiesMap();
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
                getHeader().setHTML("<h1>" + textBox.getValue() + "</h1>");
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
        getHeader().setWidth(this.width);
        getHeader().setHeight(this.height);
        getHeader().setStyleName(this.cssClassName);
    }
    
    protected HTML getHeader() {
        return this.header;
    }
}
