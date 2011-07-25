package org.jbpm.formbuilder.server.task;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "asset")
public class AssetDTO {

    private MetaDataDTO _metadata;
    private String _sourceLink;

    public MetaDataDTO getMetadata() {
        return _metadata;
    }

    public void setMetadata(MetaDataDTO metadata) {
        this._metadata = metadata;
    }

    public String getSourceLink() {
        return _sourceLink;
    }

    public void setSourceLink(String sourceLink) {
        this._sourceLink = sourceLink;
    }
}
