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
package org.jbpm.formbuilder.shared.rep.items;

import java.util.Map;

import org.jbpm.formbuilder.shared.form.FormEncodingException;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;

import com.gwtent.reflection.client.Reflectable;

@Reflectable
public class LabelRepresentation extends FormItemRepresentation {

    private String value;
    private String id;
    private String cssName;

    public LabelRepresentation() {
        super("label");
    }
    
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCssName() {
        return cssName;
    }

    public void setCssName(String cssName) {
        this.cssName = cssName;
    }
    
    @Override
    public Map<String, Object> getDataMap() {
    	Map<String, Object> data = super.getDataMap();
    	data.put("value", this.value);
    	data.put("id", this.id);
    	data.put("cssName", this.cssName);
        return data;
    }
    
    @Override
    public void setDataMap(Map<String, Object> data) throws FormEncodingException {
        super.setDataMap(data);
        this.value = (String) data.get("value");
        this.id = (String) data.get("id");
        this.cssName = (String) data.get("cssName");
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) return false;
        if (!(obj instanceof LabelRepresentation)) return false;
        LabelRepresentation other = (LabelRepresentation) obj;
        boolean equals = (this.value == null && other.value == null) || (this.value != null && this.value.equals(other.value));
        if (!equals) return equals;
        equals = (this.id == null && other.id == null) || (this.id != null && this.id.equals(other.id));
        if (!equals) return equals;
        equals = (this.cssName == null && other.cssName == null) || (this.cssName != null && this.cssName.equals(other.cssName));
        return equals;
    }
    
    @Override
    public int hashCode() {
        int result = super.hashCode();
        int aux = this.value == null ? 0 : this.value.hashCode();
        result = 37 * result + aux;
        aux = this.id == null ? 0 : this.id.hashCode();
        result = 37 * result + aux;
        aux = this.cssName == null ? 0 : this.cssName.hashCode();
        result = 37 * result + aux;
        return result;
    }
}
