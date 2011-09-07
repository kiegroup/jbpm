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
package org.jbpm.formbuilder.server.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.jbpm.formbuilder.shared.menu.MenuItemDescription;

public class MenuGroupDTO {

    private List<MenuItemDTO> _menuItem = new ArrayList<MenuItemDTO>();
    private String _name;
    
    public MenuGroupDTO() {
        // jaxb needs default constructors
    }
    
    public MenuGroupDTO(String name, List<MenuItemDescription> items) {
        this._name = name;
        for (MenuItemDescription item : items) {
            _menuItem.add(new MenuItemDTO(item));
        }
    }

    @XmlElement
    public List<MenuItemDTO> getMenuItem() {
        return _menuItem;
    }

    public void setMenuItem(List<MenuItemDTO> menuItem) {
        this._menuItem = menuItem;
    }

    @XmlAttribute 
    public String getName() {
        return _name;
    }

    public void setName(String name) {
        this._name = name;
    }
}
