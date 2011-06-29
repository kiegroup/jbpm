package org.jbpm.formbuilder.shared.rep.items;

import java.util.Map;

import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;

public class ImageRepresentation extends FormItemRepresentation {

    private String altText;
    private String url;
    private String id;

    public ImageRepresentation() {
        super("image");
    }

    public String getAltText() {
        return altText;
    }

    public void setAltText(String altText) {
        this.altText = altText;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    @Override
    public Map<String, Object> getDataMap() {
    	Map<String, Object> data = super.getDataMap();
    	data.put("altText", this.altText);
        data.put("url", this.url);
        data.put("id", this.id);
        return data;
    }
    
    @Override
    public void setDataMap(Map<String, Object> data) {
        super.setDataMap(data);
        this.altText = (String) data.get("altText");
        this.url = (String) data.get("url");
        this.id = (String) data.get("id");
        
    }
}
