package org.jbpm.formbuilder.shared.rep.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.shared.form.FormEncodingException;
import org.jbpm.formbuilder.shared.form.FormEncodingFactory;
import org.jbpm.formbuilder.shared.form.FormRepresentationDecoder;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;

public class TableRepresentation extends FormItemRepresentation {

    private final List<List<FormItemRepresentation>> elements;
    
    private Integer rows;
    private Integer columns;

    private Integer borderWidth;
    private Integer cellPadding;
    private Integer cellSpacing;
    
    public TableRepresentation() {
        super("table");
        this.elements = new ArrayList<List<FormItemRepresentation>>();
    }
    
    public TableRepresentation(Integer rows, Integer columns) {
        super("table");
        this.rows = rows == null ? 1 : rows;
        this.columns = columns == null ? 1 : columns;
        this.elements = new ArrayList<List<FormItemRepresentation>>(this.rows);
        for (int index = 0; index < this.rows; index++) {
            List<FormItemRepresentation> row = new ArrayList<FormItemRepresentation>(this.columns);
            for (int subIndex = 0; subIndex < this.columns; subIndex++) {
                row.add(null);
            }
            this.elements.add(row);
            
        }
    }

    public Integer getBorderWidth() {
        return borderWidth;
    }

    public void setBorderWidth(Integer borderWidth) {
        this.borderWidth = borderWidth;
    }

    public Integer getCellPadding() {
        return cellPadding;
    }

    public void setCellPadding(Integer cellPadding) {
        this.cellPadding = cellPadding;
    }

    public Integer getCellSpacing() {
        return cellSpacing;
    }

    public void setCellSpacing(Integer cellSpacing) {
        this.cellSpacing = cellSpacing;
    }

    public List<List<FormItemRepresentation>> getElements() {
        return elements;
    }
    
    public void setElement(int rowNumber, int colNumber, FormItemRepresentation subRep) {
        while (this.elements.size() <= rowNumber) {
            this.elements.add(new ArrayList<FormItemRepresentation>());
            this.rows = this.elements.size();
        }
        List<FormItemRepresentation> row = this.elements.get(rowNumber);
        while (row.size() <= colNumber) {
            row.add(null);
            this.columns = row.size();
        }
        row.set(colNumber, subRep);
        this.elements.set(rowNumber, row);
    }

    public Integer getRows() {
        return rows;
    }

    public Integer getColumns() {
        return columns;
    }
    
    @Override
    public Map<String, Object> getDataMap() {
        Map<String, Object> data = super.getDataMap();
        data.put("borderWidth", this.borderWidth);
        data.put("cellPadding", this.cellPadding);
        data.put("cellSpacing", this.cellSpacing);
        data.put("rows", this.rows);
        data.put("columns", this.columns);
        List<List<Map<String, Object>>> mapElements = new ArrayList<List<Map<String, Object>>>();
        if (this.elements != null) {
            for (List<FormItemRepresentation> row : this.elements) {
                List<Map<String, Object>> mapRow = null;
                if (row != null) {
                    mapRow = new ArrayList<Map<String, Object>>();
                    for (FormItemRepresentation cell : row) {
                        mapRow.add(cell == null ? null : cell.getDataMap());
                    }
                }
                mapElements.add(mapRow);
            }
        }
        data.put("elements", mapElements);
        return data;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void setDataMap(Map<String, Object> data) {
        super.setDataMap(data);
        this.borderWidth = data.get("borderWidth") == null ? null : ((Number) data.get("borderWidth")).intValue();
        this.cellPadding = data.get("cellPadding") == null ? null : ((Number) data.get("cellPadding")).intValue();
        this.cellSpacing = data.get("cellSpacing") == null ? null : ((Number) data.get("cellSpacing")).intValue();
        this.columns = data.get("columns") == null ? null : ((Number) data.get("columns")).intValue();
        this.rows = data.get("rows") == null ? null : ((Number) data.get("rows")).intValue();
        this.elements.clear();
        FormRepresentationDecoder decoder = FormEncodingFactory.getDecoder();
        List<List<Map<String, Object>>> mapElements = (List<List<Map<String, Object>>>) data.get("elements");
        if (mapElements != null) {
            for (List<Map<String, Object>> mapRow : mapElements) {
                List<FormItemRepresentation> row = new ArrayList<FormItemRepresentation>();
                if (mapRow != null) {
                    for (Map<String, Object> mapCell : mapRow) {
                        try {
                            row.add((FormItemRepresentation) decoder.decode(mapCell));
                        } catch (FormEncodingException e) {
                            row.add(null); //TODO see how to manage this error
                        }
                    }
                }
                this.elements.add(row);
            }
        }
    }
}
