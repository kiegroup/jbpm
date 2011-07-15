/**
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
package org.jbpm.formbuilder.shared.rep.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.graph.GraphEntry;

public class LineGraphRepresentation extends FormItemRepresentation {

    private List<List<String>> dataTable = new ArrayList<List<String>>();
    private List<Map.Entry<String, String>> dataStructure = new ArrayList<Map.Entry<String, String>>();
    private String graphTitle;
    private String graphXTitle;
    private String graphYTitle;
    
    public LineGraphRepresentation() {
        super("lineGraph");
    }

    public List<List<String>> getDataTable() {
        return dataTable;
    }

    public void setDataTable(List<List<String>> dataTable) {
        this.dataTable = dataTable;
    }

    public List<Map.Entry<String, String>> getDataStructure() {
        return dataStructure;
    }

    public boolean addColumn(String key, String value) {
        return dataStructure.add(new GraphEntry(key, value));
    }
    
    public boolean addTuple(int x, int y, Object obj) {
        List<String> list = new ArrayList<String>();
        list.add(String.valueOf(x));
        list.add(String.valueOf(y));
        list.add(String.valueOf(obj));
        return dataTable.add(list);
    }

    public void setDataStructure(List<Map.Entry<String, String>> dataStructure) {
        this.dataStructure = dataStructure;
    }



    public String getGraphTitle() {
        return graphTitle;
    }

    public void setGraphTitle(String graphTitle) {
        this.graphTitle = graphTitle;
    }

    public String getGraphXTitle() {
        return graphXTitle;
    }

    public void setGraphXTitle(String graphXTitle) {
        this.graphXTitle = graphXTitle;
    }

    public String getGraphYTitle() {
        return graphYTitle;
    }

    public void setGraphYTitle(String graphYTitle) {
        this.graphYTitle = graphYTitle;
    }
}
