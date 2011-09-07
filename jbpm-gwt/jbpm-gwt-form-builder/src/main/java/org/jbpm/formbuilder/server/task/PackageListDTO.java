/*
 * Copyright 2011 JBoss Inc 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.formbuilder.server.task;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "packages")
public class PackageListDTO {

    private List<PackageDTO> _package;

    @XmlElement
    public List<PackageDTO> getPackage() {
        if (_package == null) {
            _package = new ArrayList<PackageDTO>();
        }
        return _package;
    }
    
    public PackageDTO getSelectedPackage(String packageName) {
        for (PackageDTO pkg : getPackage()) {
            if (pkg.getTitle().equals(packageName)) {
                return pkg;
            }
        }
        return null;
    }

    public void setPackage(List<PackageDTO> _package) {
        this._package = _package;
    }
}
