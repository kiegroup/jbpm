/**
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
package org.jbpm.formbuilder.server.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import org.jbpm.formbuilder.shared.task.TaskPropertyRef;

@XmlType public class PropertyDTO {

    private String _name;
    private String _source;
    
    public PropertyDTO() {
        // jaxb needs a default constructor
    }
    
    public PropertyDTO(TaskPropertyRef ref) {
        this._name = ref.getName();
        this._source = ref.getSourceExpresion();
    }
    
    @XmlAttribute 
    public String getName() {
        return _name;
    }
    
    public void setName(String name) {
        this._name = name;
    }
    
    @XmlAttribute 
    public String getSource() {
        return _source;
    }

    public void setSource(String source) {
        this._source = source;
    }
}
