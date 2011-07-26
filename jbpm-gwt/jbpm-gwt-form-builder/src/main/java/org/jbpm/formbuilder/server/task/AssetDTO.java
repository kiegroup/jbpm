package org.jbpm.formbuilder.server.task;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "asset")
public class AssetDTO {

    private MetaDataDTO _metadata;
    private String _sourceLink;

    @XmlElement
    public MetaDataDTO getMetadata() {
        return _metadata;
    }

    public void setMetadata(MetaDataDTO metadata) {
        this._metadata = metadata;
    }

    @XmlElement
    public String getSourceLink() {
        return _sourceLink;
    }

    public void setSourceLink(String sourceLink) {
        this._sourceLink = sourceLink;
    }
}
