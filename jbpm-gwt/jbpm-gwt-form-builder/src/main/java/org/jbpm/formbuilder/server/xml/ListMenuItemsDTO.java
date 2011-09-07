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
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.jbpm.formbuilder.shared.menu.MenuItemDescription;

@XmlRootElement (name ="menuGroups") public class ListMenuItemsDTO {

    private List<MenuGroupDTO> _menuGroup = new ArrayList<MenuGroupDTO>();
    
    public ListMenuItemsDTO() {
        // jaxb needs a default constructor
    }
    
    public ListMenuItemsDTO(Map<String, List<MenuItemDescription>> items) {
        for (String group : items.keySet()) {
            _menuGroup.add(new MenuGroupDTO(group, items.get(group)));
        }
    }

    @XmlElement
    public List<MenuGroupDTO> getMenuGroup() {
        return _menuGroup;
    }

    public void setMenuGroup(List<MenuGroupDTO> menuGroup) {
        this._menuGroup = menuGroup;
    }
}
