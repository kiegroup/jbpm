package org.jbpm.formbuilder.client.form;

import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.items.LabelRepresentation;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class LabelFormItem extends FBFormItem {

    private final Label label = new Label("Label");
    
    private String id;
    private String width;
    private String height;
    private String cssClassName;
    
    public LabelFormItem(List<FBFormEffect> formEffects) {
        super(formEffects);
        add(getLabel());
    }
    
    @Override
    public Map<String, Object> getFormItemPropertiesMap() {
        Map<String, Object> formItemPropertiesMap = super.getFormItemPropertiesMap();
        formItemPropertiesMap.put("id", id);
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
        textBox.setValue(getLabel().getText());
        textBox.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                getLabel().setText(textBox.getValue());
                reset();
            }
        });
        editPanel.add(textBox);
        return editPanel;
    }

    @Override
    public void saveValues(Map<String, Object> propertiesMap) {
        this.id = propertiesMap.get("id").toString();
        this.width = propertiesMap.get("width").toString();
        this.height = propertiesMap.get("height").toString();
        this.cssClassName = propertiesMap.get("cssClassName").toString();
        populate();
    }

    private void populate() {
        if (this.width != null) {
            getLabel().setWidth(this.width);
        }
        if (this.height != null) {
            getLabel().setHeight(this.height);
        }
        if (this.cssClassName != null) {
            getLabel().setStyleName(this.cssClassName);
        }
    }
    
    protected Label getLabel() {
        return this.label;
    }
    
    @Override
    public void addEffect(FBFormEffect effect) {
        super.addEffect(effect);
        effect.setWidget(this.label);
    }
    
    @Override
    public FormItemRepresentation getRepresentation() {
        LabelRepresentation rep = new LabelRepresentation();
        rep.setValue(this.label.getText());
        rep.setCssName(this.cssClassName);
        rep.setId(this.id);
        rep.setWidth(this.width);
        rep.setHeight(this.height);
        return rep;
    }
    
    @Override
    public FBFormItem cloneItem() {
        LabelFormItem clone = new LabelFormItem(getFormEffects());
        clone.cssClassName = this.cssClassName;
        clone.height = this.height;
        clone.id = this.id;
        clone.width = this.width;
        clone.getLabel().setText(this.label.getText());
        clone.populate();
        return clone;
    }
}
