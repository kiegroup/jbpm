package org.jbpm.formbuilder.client.form.items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.FormBuilderException;
import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.items.TextAreaRepresentation;

import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

public class TextAreaFormItem extends FBFormItem {

    private TextArea area = new TextArea();
    
    private String defaultValue;
    private Integer rows = 3;
    private Integer cols = 30;
    private String name;
    private String id;
    
    public TextAreaFormItem(List<FBFormEffect> formEffects) {
        super(formEffects);
        area.setVisibleLines(this.rows);
        area.setCharacterWidth(this.cols);
        add(area);
    }
    
    @Override
    public Map<String, Object> getFormItemPropertiesMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("defaultValue", this.defaultValue);
        map.put("rows", this.rows);
        map.put("cols", this.cols);
        map.put("name", this.name);
        map.put("id", this.id);
        map.put("width", this.getWidth());
        map.put("height", this.getHeight());
        return map;
    }

    @Override
    public void saveValues(Map<String, Object> asPropertiesMap) {
        Integer rows = extractInt(asPropertiesMap.get("rows"));
        Integer cols = extractInt(asPropertiesMap.get("cols"));
        this.defaultValue = extractString(asPropertiesMap.get("defaultValue"));
        this.name = extractString(asPropertiesMap.get("name"));
        setWidth(extractString(asPropertiesMap.get("width")));
        setHeight(extractString(asPropertiesMap.get("height")));
        if (rows != null && rows > 0) {
            this.rows = rows;
        }
        if (cols != null && cols > 0) {
            this.cols = cols;
        }
        populate(this.area);
    }

    private void populate(TextArea area) {
        if (this.rows != null) {
            area.setVisibleLines(this.rows);
        }
        if (this.cols != null) {
            area.setCharacterWidth(this.cols);
        }
        if (this.defaultValue != null) {
            area.setValue(this.defaultValue);
        }
        if (this.name != null) {
            area.setName(this.name);
        }
        if (getWidth() != null) {
            area.setWidth(getWidth());
        }
        if (getHeight() != null) {
            area.setHeight(getHeight());
        }
    }

    @Override
    public FormItemRepresentation getRepresentation() {
        TextAreaRepresentation rep = super.getRepresentation(new TextAreaRepresentation());
        rep.setCols(this.cols);
        rep.setId(this.id);
        rep.setName(this.name);
        rep.setRows(this.rows);
        rep.setValue(this.defaultValue);
        return rep;
    }
    
    @Override
    public void populate(FormItemRepresentation rep) throws FormBuilderException {
        if (!(rep instanceof TextAreaRepresentation)) {
            throw new FormBuilderException("rep should be of type FormBuilderException but is of type " + rep.getClass().getName());
        }
        super.populate(rep);
        TextAreaRepresentation trep = (TextAreaRepresentation) rep;
        this.cols = trep.getCols();
        this.id = trep.getId();
        this.name = trep.getName();
        this.rows = trep.getRows();
        this.defaultValue = trep.getValue();
        populate(this.area);
    }
    
    @Override
    public FBFormItem cloneItem() {
        TextAreaFormItem clone = new TextAreaFormItem(getFormEffects());
        clone.cols = this.cols;
        clone.defaultValue = this.defaultValue;
        clone.id = this.id;
        clone.name = this.name;
        clone.rows = this.rows;
        clone.populate(clone.area);
        return clone;
    }

    @Override
    public Widget cloneDisplay() {
        TextArea ta = new TextArea();
        populate(ta);
        return ta;
    }

    public String getInputValue() {
        return area.getValue();
    }
}
