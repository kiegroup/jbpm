package org.jbpm.formbuilder.server.task;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "packages")
public class PackageListDTO {

    private List<PackageDTO> _package;

    public List<PackageDTO> getPackage() {
        if (_package == null) {
            _package = new ArrayList<PackageDTO>();
        }
        return _package;
    }

    public void setPackage(List<PackageDTO> _package) {
        this._package = _package;
    }
}
