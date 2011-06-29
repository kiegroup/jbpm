package org.jbpm.formbuilder.shared.rep.items;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;

public class AbsolutePanelRepresentation extends FormItemRepresentation {

    public static class Position {
        private final int x;
        private final int y;
        
        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        public int getX() {
            return x;
        }
        
        public int getY() {
            return y;
        }
    }
    
    private Map<Position, FormItemRepresentation> items = new HashMap<Position, FormItemRepresentation>();

    private String id;

    public AbsolutePanelRepresentation() {
        super("absolutePanel");
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void addItem(FormItemRepresentation rep, int x, int y) {
        items.put(new Position(x, y), rep);
    }
    
    public Map<Position, FormItemRepresentation> getItems() {
        return items;
    }
    
    @Override
    public Map<String, Object> getDataMap() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public void setDataMap(Map<String, Object> data) {
        // TODO Auto-generated method stub
        
    }
}
