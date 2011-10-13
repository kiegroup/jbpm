/*
 * Copyright 2011 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.formbuilder.client.form.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.FormBuilderException;
import org.jbpm.formbuilder.client.FormBuilderGlobals;
import org.jbpm.formbuilder.client.bus.ui.NotificationEvent;
import org.jbpm.formbuilder.client.effect.ChangeColspanFormEffect;
import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.form.LayoutFormItem;
import org.jbpm.formbuilder.client.form.PhantomPanel;
import org.jbpm.formbuilder.client.messages.I18NConstants;
import org.jbpm.formbuilder.shared.api.FormItemRepresentation;
import org.jbpm.formbuilder.shared.api.items.MIGPanelRepresentation;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.gwtent.reflection.client.Reflectable;

/**
 * UI form item. Represents a flexible table (mig layout like)
 */
@Reflectable
public class MIGLayoutFormItem extends LayoutFormItem {

    private final I18NConstants i18n = FormBuilderGlobals.getInstance().getI18n();
    private final EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
    
    private FlexTable table = new FlexTable() {
        @Override
        public boolean remove(Widget widget) {
            if (widget instanceof FBFormItem) {
                return MIGLayoutFormItem.this.remove(widget);
            } else if (widget instanceof PhantomPanel) {
                boolean retval = false;
                int row = 0, column = 0;
                while (row < super.getRowCount() && !retval) {
                    for (column = 0; column < super.getCellCount(row) && !retval; column++) {
                        if (super.getWidget(row, column) != null && isPhantom(super.getWidget(row, column))) {
                            retval = true;
                            break;
                        }
                    }
                    if (retval) break; else row++; 
                }
                if (retval) {
                    if (super.getWidget(row, column) != null) {
                        super.getWidget(row, column).getElement().getParentElement().setInnerHTML("&nbsp;");
                    }
                }
                return retval;
            } else {
                return super.remove(widget);
            }
        }
    };
    
    private Integer borderWidth = 1;
    private Integer cellpadding = null;
    private Integer cellspacing = null;
    private Integer rows = 1;
    private String title = null;

    public MIGLayoutFormItem() {
        this(new ArrayList<FBFormEffect>());
    }
    
    public MIGLayoutFormItem(List<FBFormEffect> formEffects) {
        super(formEffects);
        table.setBorderWidth(this.borderWidth);
        add(table);
        setSize("90px", "90px");
        table.setSize(getWidth(), getHeight());
    }
    
    @Override
    public void replacePhantom(FBFormItem item) {
        boolean found = false;
        int row = 0, column = 0;
        while (row < table.getRowCount()) {
            for (column = 0; column < table.getCellCount(row) && !found; column++) {
                if (isPhantom(table.getWidget(row, column))) {
                    found= true;
                    break;
                }
            }
            if (found) break; else row++;
        }
        if (found) {
            int index = (row * table.getCellCount(row)) + column;
            if (super.size() > index) { 
                super.insert(index, item);
            } else {
                super.add(item);
            }
            table.setWidget(row, column, null);
            table.setWidget(row, column, item);
        } else {
            add(item);
        }

    }
    
    @Override
    public boolean remove(Widget child) {
        boolean removed = false;
        if (child instanceof FBFormItem) {
            for (int i = 0; i < table.getRowCount(); i++) {
                for (int j = 0; j < table.getCellCount(i); j++) {
                    if (table.getWidget(i, j) != null && table.getWidget(i, j).equals(child)) {
                        removed = super.remove(child);
                        ////WARN dom used: seems the only way of fixing deleted cell bug
                        table.getWidget(i, j).getElement().getParentElement().setInnerHTML("&nbsp;");
                        break;
                    }
                }
            }
            FBFormItem item = (FBFormItem) child;
            item.removeEffectOfType(ChangeColspanFormEffect.class);
        } else {
            removed = super.remove(child);
        }
        return removed;
    }
    
    @Override
    public boolean add(FBFormItem child) {
        if (!child.hasEffectOfType(ChangeColspanFormEffect.class)) {
            child.addEffect(new ChangeColspanFormEffect());
        }
        boolean added = false;
        for (int i = 0; i < table.getRowCount() && !added; i++) {
            for (int j = 0; j < table.getCellCount(i) && !added; j++) {
                if (table.getWidget(i, j) == null || isWhiteSpace(table.getWidget(i, j))) {
                    added = true;
                    int index = (i * table.getCellCount(i)) + j;
                    if (super.size() > index) { 
                        super.insert(index-1, child);
                    } else {
                        super.add(child);
                    }
                    table.addCell(i);
                    table.setWidget(i, j, child);
                    break;
                }
            }
        }
        if (!added) {
            bus.fireEvent(new NotificationEvent(NotificationEvent.Level.WARN, i18n.TableFull()));
            return false;
        }
        return true;
    }

    @Override
    public void add(PhantomPanel phantom, int x, int y) {
        int row = 0, column = 0;
        boolean found = false;
        while (row < table.getRowCount() && !found) {
            for (column = 0; column < table.getCellCount(row) && !found; column++) {
                Element cellElement = table.getCellFormatter().getElement(row, column);
                if (x > cellElement.getAbsoluteLeft() && x < cellElement.getAbsoluteRight() &&
                    y > cellElement.getAbsoluteTop() && y < cellElement.getAbsoluteBottom() &&
                    (table.getWidget(row, column) == null || isWhiteSpace(table.getWidget(row, column)) || isPhantom(table.getWidget(row, column)))) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                row++;
            }
        }
        if (found && !isPhantom(table.getWidget(row, column))) {
            table.setWidget(row, column, phantom);
        }
    }

    @Override
    public HasWidgets getPanel() {
        return table;
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
        map.put("rows", this.rows);
        return map;
    }

    @Override
    public void saveValues(Map<String, Object> asPropertiesMap) {
        this.borderWidth = extractInt(asPropertiesMap.get("borderWidth"));
        this.cellpadding = extractInt(asPropertiesMap.get("cellpadding"));
        this.cellspacing = extractInt(asPropertiesMap.get("cellspacing"));
        this.setHeight(extractString(asPropertiesMap.get("height")));
        this.setWidth(extractString(asPropertiesMap.get("width")));
        this.title = extractString(asPropertiesMap.get("title"));
        this.rows = extractInt(asPropertiesMap.get("rows"));
        
        populate(this.table);
    }

    @Override
    public FormItemRepresentation getRepresentation() {
        MIGPanelRepresentation rep = super.getRepresentation(new MIGPanelRepresentation());
        rep.setRows(this.rows);
        rep.setBorderWidth(this.borderWidth);
        rep.setCellPadding(this.cellpadding);
        rep.setCellSpacing(this.cellspacing);
        rep.setTitle(this.title);
        for (int r = 0; r < table.getRowCount(); r++) {
            for (int c = 0; c < table.getCellCount(r); c++) {
                Widget widget = table.getWidget(r, c);
                int colspan = table.getFlexCellFormatter().getColSpan(r, c);
                int rowspan = table.getFlexCellFormatter().getRowSpan(r, c);
                if (widget != null && widget instanceof FBFormItem) {
                    FBFormItem item = (FBFormItem) widget;
                    FormItemRepresentation subRep = item.getRepresentation();
                    rep.setElement(r, c, subRep, colspan, rowspan);
                }
            }
        }
        return rep;
    }

    @Override
    public void populate(FormItemRepresentation rep) throws FormBuilderException {
        if (!(rep instanceof MIGPanelRepresentation)) {
            throw new FormBuilderException(i18n.RepNotOfType(rep.getClass().getName(), "MIGPanelRepresentation"));
        }
        super.populate(rep);
        MIGPanelRepresentation mprep = (MIGPanelRepresentation) rep;
        this.rows = mprep.getRows();
        this.borderWidth = mprep.getBorderWidth();
        this.cellpadding = mprep.getCellPadding();
        this.cellspacing = mprep.getCellSpacing();
        populate(this.table);
        this.table.clear();
        super.getItems().clear();
        if (mprep.getWidth() != null) {
            setWidth(mprep.getWidth());
        }
        if (mprep.getHeight() != null) {
            setHeight(mprep.getHeight());
        }
        if (mprep.getElements() != null) {
            for (int rowindex = 0; rowindex < mprep.getElements().size(); rowindex++) {
                List<FormItemRepresentation> row = mprep.getElements().get(rowindex);
                if(row != null) {
                    for (int cellindex = 0; cellindex < row.size(); cellindex++) {
                        FormItemRepresentation cell = row.get(cellindex);
                        FBFormItem subItem = super.createItem(cell);
                        this.table.setWidget(rowindex, cellindex, subItem);
                        int colspan = mprep.getColspan(rowindex, cellindex);
                        int rowspan = mprep.getRowspan(rowindex, cellindex);
                        if (colspan > 1) {
                            this.table.getFlexCellFormatter().setColSpan(rowindex, cellindex, colspan);
                        }
                        if (rowspan > 1) {
                            this.table.getFlexCellFormatter().setRowSpan(rowindex, cellindex, rowspan);
                        }
                        super.add(subItem);
                    }
                }
            }
        }
    }
    
    private void populate(FlexTable grid) {
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
        if (this.rows != null && this.rows > 0) {
            grid.addCell(this.rows);
        }
    }
    
    @Override
    public FBFormItem cloneItem() {
        MIGLayoutFormItem clone = super.cloneItem(new MIGLayoutFormItem());
        clone.borderWidth = this.borderWidth;
        clone.cellpadding = this.cellpadding;
        clone.cellspacing = this.cellspacing;
        clone.rows = this.rows;
        clone.title = this.title;
        clone.populate(clone.table);
        for (int row = 0; row < table.getRowCount(); row++) {
            for (int column = 0; column < table.getCellCount(row); column++) {
                FBFormItem item = (FBFormItem) this.table.getWidget(row, column);
                if (item != null) {
                    clone.table.addCell(row);
                    int colspan = table.getFlexCellFormatter().getColSpan(row, column);
                    clone.table.getFlexCellFormatter().setColSpan(row, column, colspan);
                    clone.table.setWidget(row, column, item.cloneItem());
                }
            }
        }
        return clone;
    }

    @Override
    public Widget cloneDisplay(Map<String, Object> data) {
        FlexTable ft = new FlexTable();
        populate(ft);
        for (int row = 0; row < table.getRowCount(); row++) {
            for (int column = 0; column < table.getCellCount(row); column++) {
                FBFormItem item = (FBFormItem) this.table.getWidget(row, column);
                if (item != null) {
                    ft.addCell(row);
                    int colspan = table.getFlexCellFormatter().getColSpan(row, column);
                    ft.getFlexCellFormatter().setColSpan(row, column, colspan);
                    ft.setWidget(row, column, item.cloneDisplay(data));
                }
            }
        }
        super.populateActions(ft.getElement());
        return ft;
    }

    protected boolean isPhantom(Widget widget) {
        return widget != null && widget instanceof PhantomPanel;
    }

    public void setSpan(FBFormItem item, Integer colspan, Integer rowspan) {
        for (int row = 0; row < table.getRowCount(); row++) {
            for (int col = 0; col < table.getCellCount(row); col++) {
                if (table.getWidget(row, col).equals(item)) {
                    table.getFlexCellFormatter().setColSpan(row, col, colspan);
                    table.getFlexCellFormatter().setRowSpan(row, col, rowspan);
                    break;
                }
            }
        }
    }
}
