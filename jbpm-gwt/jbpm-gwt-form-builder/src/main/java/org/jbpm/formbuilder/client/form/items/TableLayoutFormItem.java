package org.jbpm.formbuilder.client.form.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.FormBuilderException;
import org.jbpm.formbuilder.client.bus.NotificationEvent;
import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.items.TableRepresentation;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

public class TableLayoutFormItem extends LayoutFormItem {
    
    private Grid grid = new Grid(1, 1) {
        @Override
        public boolean remove(Widget widget) {
            if (widget instanceof FBFormItem) {
                return TableLayoutFormItem.this.remove(widget);
            } else {
                return super.remove(widget);
            }
        }
    };
    
    private final EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
    
    private Integer borderWidth = 1;
    private Integer cellpadding = null;
    private Integer cellspacing = null;
    private Integer columns = 1;
    private Integer rows = 1;
    private String title = null;

    public TableLayoutFormItem() {
        this(new ArrayList<FBFormEffect>());
    }
    
    public TableLayoutFormItem(List<FBFormEffect> formEffects) {
        super(formEffects);
        grid.setBorderWidth(this.borderWidth);
        add(grid);
        setSize("90px", "90px");
        grid.setSize(getWidth(), getHeight());
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
        this.setHeight(extractString(asPropertiesMap.get("height")));
        this.setWidth(extractString(asPropertiesMap.get("width")));
        this.title = extractString(asPropertiesMap.get("title"));
        this.columns = extractInt(asPropertiesMap.get("columns"));
        this.rows = extractInt(asPropertiesMap.get("rows"));
        
        populate(this.grid);
    }

    private void populate(Grid grid) {
        if (this.borderWidth != null && this.borderWidth > 0) {
            grid.setBorderWidth(this.borderWidth);
        }
        if (this.cellpadding != null && this.cellpadding >= 0) {
            grid.setCellPadding(this.cellpadding);
        }
        if (this.cellspacing != null && this.cellspacing >= 0) {
            grid.setCellSpacing(this.cellspacing);
        }
        if (getHeight() != null) {
            grid.setHeight(getHeight());
        }
        if (getWidth() != null) {
            grid.setWidth(getWidth());
        }
        if (this.title != null) {
            grid.setTitle(this.title);
        }
        if (this.columns != null && this.columns > 0) {
            grid.resizeColumns(this.columns);
        }
        if (this.rows != null && this.rows > 0) {
            grid.resizeRows(this.rows);
        }
    }

    @Override
    public Map<String, Object> getFormItemPropertiesMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("borderWidth", this.borderWidth);
        map.put("cellpadding", this.cellpadding);
        map.put("cellspacing", this.cellspacing);
        map.put("height", getHeight());
        map.put("width", getWidth());
        map.put("title", this.title);
        map.put("columns", this.columns);
        map.put("rows", this.rows);
        return map;
    }

    @Override
    public boolean add(FBFormItem child) {
        boolean added = false;
        for (int i = 0; i < grid.getRowCount() && !added; i++) {
            for (int j = 0; j < grid.getColumnCount() && !added; j++) {
                //WARN dom used: seems the only way of fixing deleted cell bug
                if (grid.getWidget(i, j) == null || grid.getWidget(i, j).getElement().getParentElement().getInnerHTML().equals("&nbsp;")) {
                    added = true;
                    FBFormItem item = (FBFormItem) child;
                    int index = (i+1)*(j+1);
                    if (super.size() > index) { 
                        super.set(index-1, item);
                    } else {
                        super.add(item);
                    }
                    grid.setWidget(i, j, child);
                    break;
                }
            }
        }
        if (!added) {
            bus.fireEvent(new NotificationEvent(NotificationEvent.Level.WARN, 
                    "Table full! Use different layouts in each cell or add more rows or columns"));
            return false;
        }
        return true;
    }
    
    @Override
    public boolean remove(Widget child) {
        boolean removed = false;
        if (child instanceof FBFormItem) {
            for (int i = 0; i < grid.getRowCount(); i++) {
                for (int j = 0; j < grid.getColumnCount(); j++) {
                    if (grid.getWidget(i, j) != null && grid.getWidget(i, j).equals(child)) {
                        removed = super.remove(child);
                        ////WARN dom used: seems the only way of fixing deleted cell bug
                        grid.getWidget(i, j).getElement().getParentElement().setInnerHTML("&nbsp;");
                        break;
                    }
                }
            }
        } else {
            removed = super.remove(child);
        }
        return removed;
    }
    
    @Override
    public FormItemRepresentation getRepresentation() {
        TableRepresentation rep = super.getRepresentation(new TableRepresentation(this.rows, this.columns));
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
    
    @Override
    public void populate(FormItemRepresentation rep) throws FormBuilderException {
        if (!(rep instanceof TableRepresentation)) {
            throw new FormBuilderException("rep should be of type TableRepresentation but is of type " + rep.getClass().getName());
        }
        super.populate(rep);
        TableRepresentation trep = (TableRepresentation) rep;
        this.rows = trep.getRows();
        this.columns = trep.getColumns();
        this.borderWidth = trep.getBorderWidth();
        this.cellpadding = trep.getCellPadding();
        this.cellspacing = trep.getCellSpacing();
        populate(this.grid);
        this.grid.clear();
        if (trep.getElements() != null) {
            for (int rowindex = 0; rowindex < trep.getElements().size(); rowindex++) {
                List<FormItemRepresentation> row = trep.getElements().get(rowindex);
                if(row != null) {
                    for (int cellindex = 0; cellindex < row.size(); cellindex++) {
                        FormItemRepresentation cell = row.get(cellindex);
                        this.grid.setWidget(rowindex, cellindex, super.createItem(cell));
                    }
                }
            }
        }
    }
    
    @Override
    public FBFormItem cloneItem() {
        TableLayoutFormItem clone = new TableLayoutFormItem(getFormEffects());
        clone.borderWidth = this.borderWidth;
        clone.cellpadding = this.cellpadding;
        clone.cellspacing = this.cellspacing;
        clone.columns = this.columns;
        clone.setHeight(getHeight());
        clone.rows = this.rows;
        clone.title = this.title;
        clone.setWidth(getWidth());
        clone.populate(clone.grid);
        for (int index = 0; index < clone.columns * clone.rows; index++) {
            int column = index%clone.columns;
            int row = index/clone.columns;
            FBFormItem item = (FBFormItem) this.grid.getWidget(row, column);
            if (item != null) {
                clone.grid.setWidget(row, column, item.cloneItem());
            }
        }
        return clone;
    }
    
    @Override
    public Widget cloneDisplay() {
        Grid g = new Grid(this.rows, this.columns);
        populate(g);
        for (int index = 0; index < this.columns * this.rows; index++) {
            int column = index%this.columns;
            int row = index/this.columns;
            FBFormItem item = (FBFormItem) this.grid.getWidget(row, column);
            if (item != null) {
                g.setWidget(row, column, item.cloneDisplay());
            }
        }
        return g;
    }
}
