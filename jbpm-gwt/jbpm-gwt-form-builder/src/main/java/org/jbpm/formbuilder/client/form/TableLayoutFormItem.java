package org.jbpm.formbuilder.client.form;

import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.items.TableRepresentation;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class TableLayoutFormItem extends LayoutFormItem {

    private Grid grid = new Grid(1, 1) {
        @Override
        public void add(Widget child) {
            tableAdd(child);
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
    
    public TableLayoutFormItem(List<FBFormEffect> formEffects) {
        super(formEffects);
        grid.setBorderWidth(1);
        grid.setSize("90px", "90px");
        add(grid);
    }
    
    @Override
    public Panel getPanel() {
        return grid;
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

    protected void tableAdd(Widget child) {
        if (child instanceof FBFormItem) {
            int widgetCount = super.size();
            if (widgetCount >= grid.getColumnCount() * grid.getRowCount()) {
                boolean added = false;
                for (int i = 0; i < grid.getRowCount(); i++) {
                    for (int j = 0; j < grid.getColumnCount(); j++) {
                        if (grid.getWidget(i, j) == null) {
                            added = true;
                            FBFormItem item = (FBFormItem) child;
                            super.set(((i+1)*(j+1))-1, item);
                            grid.setWidget(i, j, child);
                            break;
                        }
                    }
                }
                if (!added) {
                    Window.alert("Table full! Use different layouts in each cell or add more rows or columns");
                }
            } else {
                FBFormItem item = (FBFormItem) child;
                super.add(item);
                int row = widgetCount / grid.getColumnCount();
                int col = widgetCount % grid.getColumnCount();
                if (grid.getWidget(row, col) == null) {
                    grid.setWidget(row, col, child);
                } else {
                    Window.alert("Table full! Use different layouts in each cell or add more rows or columns");
                }
            }
        }
    }
    
    @Override
    public FormItemRepresentation getRepresentation() {
        TableRepresentation rep = new TableRepresentation(this.rows, this.columns);
        rep.setBorderWidth(this.borderWidth);
        rep.setCellPadding(this.cellpadding);
        rep.setCellSpacing(this.cellspacing);
        for (int index = 0; index < this.columns * this.rows; index++) {
            int column = index%this.columns;
            int row = index/this.columns;
            FBFormItem item = (FBFormItem) grid.getWidget(row, column);
            if (item != null) {
                FormItemRepresentation subRep = item.getRepresentation();
                rep.setElement(row, column, subRep);
            }
        }
        return rep;
    }
}
