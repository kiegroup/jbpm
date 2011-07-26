package org.jbpm.formbuilder.server.task;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class PackageDTO {

    private String _title;
    
    private List<String> _assets;
    private MetaDataDTO _metadata;

    @XmlElement
    public String getTitle() {
        return _title;
    }

    public void setTitle(String title) {
        this._title = title;
    }

    @XmlElement
    public List<String> getAssets() {
        if (_assets == null) {
             _assets = new ArrayList<String>(); 
        }
        return _assets;
    }

    public void setAssets(List<String> assets) {
        this._assets = assets;
    }
    
    @XmlElement
    public MetaDataDTO getMetadata() {
        return _metadata;
    }
    
    public void setMetadata(MetaDataDTO metadata) {
        this._metadata = metadata;
    }
}
