package org.jbpm.formbuilder.client.form.items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.FormBuilderException;
import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
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
    private String cssClassName;
    
    public LabelFormItem(List<FBFormEffect> formEffects) {
        super(formEffects);
        add(getLabel());
    }
    
    @Override
    public Map<String, Object> getFormItemPropertiesMap() {
        Map<String, Object> formItemPropertiesMap = new HashMap<String, Object>();
        formItemPropertiesMap.put("id", id);
        formItemPropertiesMap.put("width", getWidth());
        formItemPropertiesMap.put("height", getHeight());
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
        this.id = extractString(propertiesMap.get("id"));
        this.setWidth(extractString(propertiesMap.get("width")));
        this.setHeight(extractString(propertiesMap.get("height")));
        this.cssClassName = extractString(propertiesMap.get("cssClassName"));
        populate(getLabel());
    }

    private void populate(Label label) {
        if (this.getWidth() != null) {
            label.setWidth(this.getWidth());
        }
        if (this.getHeight() != null) {
            label.setHeight(this.getHeight());
        }
        if (this.cssClassName != null) {
            label.setStyleName(this.cssClassName);
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
        LabelRepresentation rep = super.getRepresentation(new LabelRepresentation());
        rep.setValue(this.label.getText());
        rep.setCssName(this.cssClassName);
        rep.setId(this.id);
        return rep;
    }
    
    @Override
    public void populate(FormItemRepresentation rep) throws FormBuilderException {
        if (!(rep instanceof LabelRepresentation)) {
            throw new FormBuilderException("rep should be of type LabelRepresentation but is of type " + rep.getClass().getName());
        }
        super.populate(rep);
        LabelRepresentation lrep = (LabelRepresentation) rep;
        this.label.setText(lrep.getValue());
        this.cssClassName = lrep.getCssName();
        this.id = lrep.getId();
        populate(this.label);
    }
    
    @Override
    public FBFormItem cloneItem() {
        LabelFormItem clone = new LabelFormItem(getFormEffects());
        clone.cssClassName = this.cssClassName;
        clone.setHeight(this.getHeight());
        clone.id = this.id;
        clone.setWidth(this.getWidth());
        clone.getLabel().setText(this.label.getText());
        clone.populate(clone.label);
        return clone;
    }
    
    @Override
    public Widget cloneDisplay() {
        Label lb = new Label();
        populate(lb);
        return lb;
    }
}
