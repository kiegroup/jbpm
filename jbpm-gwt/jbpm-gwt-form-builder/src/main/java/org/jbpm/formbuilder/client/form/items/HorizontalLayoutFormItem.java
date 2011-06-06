package org.jbpm.formbuilder.client.form.items;
 
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.items.HorizontalPanelRepresentation;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;

public class HorizontalLayoutFormItem extends LayoutFormItem {

    private HorizontalPanel panel = new HorizontalPanel();
    
    public HorizontalLayoutFormItem(List<FBFormEffect> formEffects) {
        super(formEffects);
        panel.setBorderWidth(1);
        panel.setSize("90px", "30px");
        add(panel);
    }
    
    private Integer borderWidth;
    private String height;
    private String width;
    private Integer spacing;
    private String cssClassName;
    private String horizontalAlignment;
    private String verticalAlignment;
    private String title;
    private String id;
    
    @Override
    public Map<String, Object> getFormItemPropertiesMap() {
        Map<String, Object> formItemPropertiesMap = new HashMap<String, Object>();
        formItemPropertiesMap.put("borderWidth", borderWidth);
        formItemPropertiesMap.put("height", height);
        formItemPropertiesMap.put("width", width);
        formItemPropertiesMap.put("spacing", spacing);
        formItemPropertiesMap.put("cssClassName", cssClassName);
        formItemPropertiesMap.put("horizontalAlignment", horizontalAlignment);
        formItemPropertiesMap.put("verticalAlignment", verticalAlignment);
        formItemPropertiesMap.put("title", title);
        formItemPropertiesMap.put("id", id);
        return formItemPropertiesMap;
    }
    
    @Override
    public void saveValues(Map<String, Object> asPropertiesMap) {
        this.borderWidth = extractInt(asPropertiesMap.get("borderWidth"));
        this.height = asPropertiesMap.get("height").toString();
        this.width = asPropertiesMap.get("width").toString();
        this.spacing = extractInt(asPropertiesMap.get("spacing"));
        this.cssClassName = asPropertiesMap.get("cssClassName").toString();
        this.horizontalAlignment = asPropertiesMap.get("horizontalAlignment").toString();
        this.verticalAlignment = asPropertiesMap.get("verticalAlignment").toString();
        this.title = asPropertiesMap.get("title").toString();
        this.id = asPropertiesMap.get("id").toString();
        
        populate();
    }

    private void populate() {
        if (this.borderWidth != null) {
            panel.setBorderWidth(this.borderWidth);
        }
        if (this.height != null && !"".equals(this.height)) {
            panel.setHeight(this.height);
        }
        if (this.width != null && !"".equals(this.width)) {
            panel.setWidth(this.width);
        }
        if (this.spacing != null) {
            panel.setSpacing(this.spacing);
        }
        if (this.cssClassName != null) {
            panel.setStyleName(this.cssClassName);
        }
        //panel.setHorizontalAlignment(HorizontalAlignmentConstant.startOf(Direction.valueOf(horizontalAlignment))); TODO
        if (this.title != null) {
            panel.setTitle(this.title);
        }
    }

   
    
    @Override
    public Panel getPanel() {
        return panel;
    }

    @Override
    public FormItemRepresentation getRepresentation() {
        HorizontalPanelRepresentation rep = new HorizontalPanelRepresentation();
        rep.setBorderWidth(this.borderWidth);
        rep.setCssClassName(this.cssClassName);
        rep.setHeight(this.height);
        rep.setHorizontalAlignment(this.horizontalAlignment);
        rep.setId(this.id);
        rep.setSpacing(this.spacing);
        rep.setTitle(this.title);
        rep.setVerticalAlignment(this.verticalAlignment);
        rep.setWidth(this.width);
        for (FBFormItem item : getItems()) {
            rep.addItem(item.getRepresentation());
        }
        return rep;
    }
    
    @Override
    public FBFormItem cloneItem() {
        HorizontalLayoutFormItem clone = new HorizontalLayoutFormItem(getFormEffects());
        clone.borderWidth = this.borderWidth;
        clone.cssClassName = this.cssClassName;
        clone.height = this.height;
        clone.horizontalAlignment = this.horizontalAlignment;
        clone.id = this.id;
        clone.spacing = this.spacing;
        clone.title = this.title;
        clone.verticalAlignment = this.verticalAlignment;
        clone.width = this.width;
        clone.populate();
        for (FBFormItem item : getItems()) {
            clone.add(item.cloneItem());
        }
        return clone;
    }
}
