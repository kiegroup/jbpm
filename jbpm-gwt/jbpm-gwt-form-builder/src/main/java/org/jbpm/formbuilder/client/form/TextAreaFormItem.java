package org.jbpm.formbuilder.client.form;

import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.items.TextAreaRepresentation;

import com.google.gwt.user.client.ui.TextArea;

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
        Map<String, Object> map = super.getFormItemPropertiesMap();
        map.put("defaultValue", this.defaultValue);
        map.put("rows", this.rows);
        map.put("cols", this.cols);
        map.put("name", this.name);
        map.put("id", this.id);
        return map;
    }

    @Override
    public void saveValues(Map<String, Object> asPropertiesMap) {
        Integer rows = extractInt(asPropertiesMap.get("rows"));
        Integer cols = extractInt(asPropertiesMap.get("cols"));
        this.defaultValue = asPropertiesMap.get("defaultValue").toString();
        if (rows != null && rows > 0) {
            this.rows = rows;
            area.setVisibleLines(rows);
        }
        if (cols != null && cols > 0) {
            this.cols = cols;
            area.setCharacterWidth(cols);
        }
        area.setValue(defaultValue);
    }

    @Override
    public FormItemRepresentation getRepresentation() {
        TextAreaRepresentation rep = new TextAreaRepresentation();
        rep.setCols(this.cols);
        rep.setId(this.id);
        rep.setName(this.name);
        rep.setRows(this.rows);
        rep.setValue(this.defaultValue);
        return rep;
    }

}
