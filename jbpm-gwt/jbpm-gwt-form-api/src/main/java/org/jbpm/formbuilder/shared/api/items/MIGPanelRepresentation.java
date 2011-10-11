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
package org.jbpm.formbuilder.shared.api.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.shared.api.FormItemRepresentation;

import com.gwtent.reflection.client.Reflectable;

@Reflectable
public class MIGPanelRepresentation extends FormItemRepresentation {

    class Cell {
        private int row;
        private int cellNumber;
        public Cell(int row, int cellNumber) {
            super();
            this.row = row;
            this.cellNumber = cellNumber;
        }
        public int getRow() {
            return row;
        }
        public void setRow(int row) {
            this.row = row;
        }
        public int getCellNumber() {
            return cellNumber;
        }
        public void setCellNumber(int cellNumber) {
            this.cellNumber = cellNumber;
        }
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + cellNumber;
            result = prime * result + row;
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            Cell other = (Cell) obj;
            if (!getOuterType().equals(other.getOuterType())) return false;
            if (cellNumber != other.cellNumber) return false;
            if (row != other.row) return false;
            return true;
        }
        private MIGPanelRepresentation getOuterType() {
            return MIGPanelRepresentation.this;
        }
    }
    
    private final List<List<FormItemRepresentation>> elements;
    private final Map<Cell, Integer> colspans = new HashMap<Cell, Integer>();

    private Integer rows;
    private Integer borderWidth;
    private Integer cellSpacing;
    private Integer cellPadding;
    private String title;
    
    public MIGPanelRepresentation() {
        super("migPanel");
        this.elements = new ArrayList<List<FormItemRepresentation>>();
    }

    public Integer getRows() {
        return rows;
    }

    public void setRows(Integer rows) {
        this.rows = rows;
    }

    public Integer getBorderWidth() {
        return borderWidth;
    }

    public void setBorderWidth(Integer borderWidth) {
        this.borderWidth = borderWidth;
    }

    public Integer getCellSpacing() {
        return cellSpacing;
    }

    public void setCellSpacing(Integer cellSpacing) {
        this.cellSpacing = cellSpacing;
    }

    public Integer getCellPadding() {
        return cellPadding;
    }

    public void setCellPadding(Integer cellPadding) {
        this.cellPadding = cellPadding;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setElement(int rowNumber, int cellNumber, FormItemRepresentation subRep, int colspan) {
        while (this.elements.size() <= rowNumber) {
            this.elements.add(new ArrayList<FormItemRepresentation>());
            this.rows = this.elements.size();
        }
        List<FormItemRepresentation> row = this.elements.get(rowNumber);
        while (row.size() <= cellNumber) {
            row.add(null);
        }
        row.set(cellNumber, subRep);
        this.elements.set(rowNumber, row);
        this.colspans.put(new Cell(rowNumber, cellNumber), colspan);
    }

    public List<List<FormItemRepresentation>> getElements() {
        return this.elements;
    }
    
    public int getColspan(int rowNumber, int cellNumber) {
        Cell index = new Cell(rowNumber, cellNumber);
        return colspans.get(index);
    }
}
