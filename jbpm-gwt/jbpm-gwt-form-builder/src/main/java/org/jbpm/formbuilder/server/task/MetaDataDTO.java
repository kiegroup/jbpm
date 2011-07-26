package org.jbpm.formbuilder.server.task;

import javax.xml.bind.annotation.XmlElement;

public class MetaDataDTO {

    private String _title;
    private String _uuid;
    private String _format;

    @XmlElement
    public String getTitle() {
        return _title;
    }

    public void setTitle(String title) {
        this._title = title;
    }

    @XmlElement
    public String getUuid() {
        return _uuid;
    }

    public void setUuid(String uuid) {
        this._uuid = uuid;
    }

    @XmlElement
    public String getFormat() {
        return _format;
    }

    public void setFormat(String format) {
        this._format = format;
    }
}
