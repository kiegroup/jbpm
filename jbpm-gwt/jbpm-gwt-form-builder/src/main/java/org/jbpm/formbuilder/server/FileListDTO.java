package org.jbpm.formbuilder.server;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "files")
public class FileListDTO {

    private List<String> _file = new ArrayList<String>();

    public FileListDTO() {
        // jaxb needs a default constructor
    }
    
    public FileListDTO(List<String> file) {
        super();
        if (file != null) {
            for (String f : file) {
                this._file.add(f);
            }
        }
    }

    @XmlElement
    public List<String> getFile() {
        return _file;
    }

    public void setFile(List<String> file) {
        this._file = file;
    }
}
