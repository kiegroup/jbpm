package org.jbpm.formbuilder.client.form;
 
import java.util.Map;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;

public class HorizontalLayoutFormItem extends LayoutFormItem {

    private HorizontalPanel panel = new HorizontalPanel();
    
    public HorizontalLayoutFormItem() {
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
    public String asCode(String type) {
        
        //panel.setCellHeight(w, height);
        //panel.setCellWidth(w, width);
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, Object> getFormItemPropertiesMap() {
        Map<String, Object> formItemPropertiesMap = super.getFormItemPropertiesMap();
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
        
        if (this.borderWidth != null) {
            panel.setBorderWidth(this.borderWidth);
        }
        panel.setHeight(this.height);
        if (this.width != null && !"".equals(this.width)) {
            panel.setWidth(this.width);
        }
        if (this.spacing != null) {
            panel.setSpacing(this.spacing);
        }
        panel.setStyleName(this.cssClassName);
        //panel.setHorizontalAlignment(HorizontalAlignmentConstant.startOf(Direction.valueOf(horizontalAlignment))); TODO
        panel.setTitle(this.title);
    }

   
    
    @Override
    public Panel getPanel() {
        return panel;
    }

    
}
