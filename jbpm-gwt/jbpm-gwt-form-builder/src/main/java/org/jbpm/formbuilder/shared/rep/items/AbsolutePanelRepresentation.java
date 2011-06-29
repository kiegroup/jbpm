package org.jbpm.formbuilder.shared.rep.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.shared.form.FormEncodingException;
import org.jbpm.formbuilder.shared.form.FormEncodingFactory;
import org.jbpm.formbuilder.shared.form.FormRepresentationDecoder;
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
        Map<String, Object> data = super.getDataMap();
        data.put("id", this.id);
        List<Map<String, Object>> mapItems = new ArrayList<Map<String, Object>>();
        if (this.items != null) {
            for (Map.Entry<Position, FormItemRepresentation> entry : this.items.entrySet()) {
                FormItemRepresentation item = entry.getValue();
                Position pos = entry.getKey();
                Map<String, Object> itemData = item == null ? null : item.getDataMap();
                itemData.put("x", pos.getX());
                itemData.put("y", pos.getY());
                mapItems.add(itemData);
            }
        }
        data.put("items", mapItems);
        return data;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void setDataMap(Map<String, Object> data) {
        super.setDataMap(data);
        this.id = (String) data.get("id");
        this.items.clear();
        List<Map<String, Object>> mapItems = (List<Map<String, Object>>) data.get("items");
        FormRepresentationDecoder decoder = FormEncodingFactory.getDecoder();
        if (mapItems != null) {
            for (Map<String, Object> entry : mapItems) {
                try {
                    int x = entry.get("x") == null ? 0 : ((Number) entry.get("x")).intValue();
                    int y = entry.get("y") == null ? 0 : ((Number) entry.get("y")).intValue();
                    Position pos = new Position(x, y);
                    FormItemRepresentation item = (FormItemRepresentation) decoder.decode(entry);
                    this.items.put(pos, item);
                } catch (FormEncodingException e) {
                    //TODO see what to do with this error
                }
            }
        }
    }
}
