package org.jbpm.formbuilder.shared.rep.items;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.trans.LanguageException;
import org.jbpm.formbuilder.shared.rep.trans.LanguageFactory;

public class TableRepresentation extends FormItemRepresentation {

    private final List<List<FormItemRepresentation>> elements;
    
    private final Integer rows;
    private final Integer columns;

    private Integer borderWidth;
    private Integer cellPadding;
    private Integer cellSpacing;
    
    public TableRepresentation(Integer rows, Integer columns) {
        super();
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
        List<FormItemRepresentation> row = this.elements.get(rowNumber);
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
    public String translate(String language) throws LanguageException {
        return LanguageFactory.getInstance().getLanguage(language).table(this);
    }
}
