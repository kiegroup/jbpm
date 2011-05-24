package org.jbpm.formbuilder.client.form;

import java.util.Map;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class TableLayoutFormItem extends LayoutFormItem {

    private Grid grid = new Grid(1, 1) {
        @Override
        public void add(Widget child) {
            if (child instanceof FBFormItem) {
                int widgetCount = size();
                FBFormItem item = (FBFormItem) child;
                TableLayoutFormItem.this.add(item);
                widgetCount++;
                int row = (widgetCount -1) / getRowCount();
                int col = (widgetCount - 1) % getRowCount();
                setWidget(row, col, child);
            }
        }
    };
    
    private Integer borderWidth = null;
    private Integer cellpadding = null;
    private Integer cellspacing = null;
    private Integer columns = null;
    private Integer rows = null;
    private String title = null;
    private String height = null;
    private String width = null;
    
    public TableLayoutFormItem() {
        grid.setBorderWidth(1);
        grid.setSize("90px", "90px");
        add(grid);
    }
    
    @Override
    public Panel getPanel() {
        return grid;
    }

    @Override
    public String asCode(String type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void saveValues(Map<String, Object> asPropertiesMap) {
        
        this.borderWidth = extractInt(asPropertiesMap.get("borderWidth"));
        this.cellpadding = extractInt(asPropertiesMap.get("cellpadding"));
        this.cellspacing = extractInt(asPropertiesMap.get("cellspacing"));
        this.height = asPropertiesMap.get("height").toString();
        this.width = asPropertiesMap.get("width").toString();
        this.title = asPropertiesMap.get("title").toString();
        this.columns = extractInt(asPropertiesMap.get("columns"));
        this.rows = extractInt(asPropertiesMap.get("rows"));
        
        if (this.borderWidth != null && this.borderWidth > 0) {
            grid.setBorderWidth(this.borderWidth);
        }
        if (this.cellpadding != null && this.cellpadding >= 0) {
            grid.setCellPadding(this.cellpadding);
        }
        if (this.cellspacing != null && this.cellspacing >= 0) {
            grid.setCellSpacing(this.cellspacing);
        }
        grid.setHeight(this.height);
        grid.setWidth(this.width);
        grid.setTitle(this.title);
        if (this.columns != null && this.columns > 0) {
            grid.resizeColumns(this.columns);
        }
        if (this.rows != null && this.rows > 0) {
            grid.resizeRows(this.rows);
        }
    }

    @Override
    public Map<String, Object> getFormItemPropertiesMap() {
        Map<String, Object> map = super.getFormItemPropertiesMap();
        map.put("borderWidth", this.borderWidth);
        map.put("cellpadding", this.cellpadding);
        map.put("cellspacing", this.cellspacing);
        map.put("height", this.height);
        map.put("width", this.width);
        map.put("title", this.title);
        map.put("columns", this.columns);
        map.put("rows", this.rows);
        return map;
    }
}
