package org.jbpm.formbuilder.shared.rep.graph;

import java.util.Map.Entry;

public class GraphEntry implements Entry<String, String> {

    private final String key;
    private String value;
    
    public GraphEntry(String key, String value) {
        this.key = key;
        this.value = value;
    }
    
    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public String setValue(String value) {
        String aux = this.value;
        this.value = value;
        return aux;
    }

}
