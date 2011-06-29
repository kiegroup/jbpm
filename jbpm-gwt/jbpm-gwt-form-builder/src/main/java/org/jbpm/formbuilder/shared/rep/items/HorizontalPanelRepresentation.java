package org.jbpm.formbuilder.shared.rep.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.shared.form.FormEncodingException;
import org.jbpm.formbuilder.shared.form.FormEncodingFactory;
import org.jbpm.formbuilder.shared.form.FormRepresentationDecoder;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;

public class HorizontalPanelRepresentation extends FormItemRepresentation {

    private Integer borderWidth;
    private Integer spacing;
    private String cssClassName;
    private String horizontalAlignment;
    private String verticalAlignment;
    private String title;
    private String id;
    private List<FormItemRepresentation> items = new ArrayList<FormItemRepresentation>();
    
    public HorizontalPanelRepresentation() {
        super("horizontalPanel");
    }

    public Integer getBorderWidth() {
        return borderWidth;
    }

    public void setBorderWidth(Integer borderWidth) {
        this.borderWidth = borderWidth;
    }

    public Integer getSpacing() {
        return spacing;
    }

    public void setSpacing(Integer spacing) {
        this.spacing = spacing;
    }

    public String getCssClassName() {
        return cssClassName;
    }

    public void setCssClassName(String cssClassName) {
        this.cssClassName = cssClassName;
    }

    public String getHorizontalAlignment() {
        return horizontalAlignment;
    }

    public void setHorizontalAlignment(String horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
    }

    public String getVerticalAlignment() {
        return verticalAlignment;
    }

    public void setVerticalAlignment(String verticalAlignment) {
        this.verticalAlignment = verticalAlignment;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public List<FormItemRepresentation> getItems() {
        return items;
    }
    
    public void addItem(FormItemRepresentation item) {
        items.add(item);
    }
    
    public void setItems(List<FormItemRepresentation> items) {
        this.items = items;
    }
    
    @Override
    public Map<String, Object> getDataMap() {
        Map<String, Object> data = super.getDataMap();
        data.put("borderWidth", this.borderWidth);
        data.put("spacing", this.spacing);
        data.put("cssClassName", this.cssClassName);
        data.put("horizontalAlignment", this.horizontalAlignment);
        data.put("verticalAlignment", this.verticalAlignment);
        data.put("title", this.title);
        data.put("id", this.id);
        List<Map<String, Object>> mapItems = new ArrayList<Map<String, Object>>();
        if (this.items != null) {
            for (FormItemRepresentation item : this.items) {
                mapItems.add(item == null ? null : item.getDataMap());
            }
        }
        data.put("items", mapItems);
        return data;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void setDataMap(Map<String, Object> data) {
        super.setDataMap(data);
        this.borderWidth = data.get("borderWidth") == null ? null : ((Number) data.get("borderWidth")).intValue();
        this.spacing = data.get("spacing") == null ? null : ((Number) data.get("spacing")).intValue();
        this.cssClassName = (String) data.get("cssClassName");
        this.horizontalAlignment = (String) data.get("horizontalAlignment");
        this.verticalAlignment = (String) data.get("verticalAlignment");
        this.title = (String) data.get("title");
        this.id = (String) data.get("id");
        this.items.clear();
        FormRepresentationDecoder decoder = FormEncodingFactory.getDecoder();
        List<Map<String, Object>> mapItems = (List<Map<String, Object>>) data.get("items");
        if (mapItems != null) {
            for (Map<String, Object> mapItem : mapItems) {
                try {
                    this.items.add((FormItemRepresentation) decoder.decode(mapItem));
                } catch (FormEncodingException e) {
                    //TODO see what to do with this error
                }
            }
        }
    }
}
