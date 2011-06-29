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
    
    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) return false;
        if (!(obj instanceof ImageRepresentation)) return false;
        ImageRepresentation other = (ImageRepresentation) obj;
        boolean equals = (this.altText == null && other.altText == null) || (this.altText != null && this.altText.equals(other.altText));
        if (!equals) return equals;
        equals = (this.url == null && other.url == null) || (this.url != null && this.url.equals(other.url));
        if (!equals) return equals;
        equals = (this.id == null && other.id == null) || (this.id != null && this.id.equals(other.id));
        return equals;
    }
    
    @Override
    public int hashCode() {
        int result = super.hashCode();
        int aux = this.altText == null ? 0 : this.altText.hashCode();
        result = 37 * result + aux;
        aux = this.url == null ? 0 : this.url.hashCode();
        result = 37 * result + aux;
        aux = this.id == null ? 0 : this.id.hashCode();
        result = 37 * result + aux;
        return result;
    }
}
