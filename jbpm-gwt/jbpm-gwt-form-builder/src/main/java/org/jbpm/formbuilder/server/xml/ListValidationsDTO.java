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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.jbpm.formbuilder.shared.menu.ValidationDescription;

@XmlRootElement (name = "validations")
public class ListValidationsDTO {

    private List<ValidationDTO> _validation;

    public ListValidationsDTO() {
        // jaxb needs a default constructor
    }
    
    public ListValidationsDTO(List<ValidationDescription> validations) {
        if (validations != null) {
            for (ValidationDescription val : validations) {
                getValidation().add(new ValidationDTO(val));
            }
        }
    }
    
    public List<ValidationDTO> getValidation() {
        if (_validation == null) {
            _validation = new ArrayList<ValidationDTO>();
        }
        return _validation;
    }

    public void setValidation(List<ValidationDTO> validation) {
        this._validation = validation;
    }
}
