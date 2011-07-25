package org.jbpm.formbuilder.server.task;

import java.util.ArrayList;
import java.util.List;

public class PackageDTO {

private String _title;
    
    private List<String> _assets;

    public String getTitle() {
        return _title;
    }

    public void setTitle(String title) {
        this._title = title;
    }

    public List<String> getAssets() {
        if (_assets == null) {
             _assets = new ArrayList<String>(); 
        }
        return _assets;
    }

    public void setAssets(List<String> assets) {
        this._assets = assets;
    }
}
