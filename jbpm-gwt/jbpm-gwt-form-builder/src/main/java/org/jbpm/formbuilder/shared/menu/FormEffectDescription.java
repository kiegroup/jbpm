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
package org.jbpm.formbuilder.shared.menu;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.formbuilder.shared.rep.Mappable;

public class FormEffectDescription implements Mappable {

    private String className;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
    
    public Map<String, Object> getDataMap() {
        Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("className", this.className);
        return dataMap;
    }
    
    public void setDataMap(Map<String, Object> dataMap) {
        this.className = (String) dataMap.get("className");
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (!(obj instanceof FormEffectDescription)) return false;
        FormEffectDescription other = (FormEffectDescription) obj;
        return (this.className == null && other.className == null) || (this.className != null && this.className.equals(other.className));
    }
    
    @Override
    public int hashCode() {
        int result = super.hashCode();
        int aux = this.className == null ? 0 : this.className.hashCode();
        result = 37 * result + aux;
        return result;
    }
}
