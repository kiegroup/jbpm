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
public class HeaderRepresentation extends FormItemRepresentation {

    private String value;
    private String styleClass;
    private String cssId;
    private String cssName;

    public HeaderRepresentation() {
        super("header");
    }
    
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getStyleClass() {
        return styleClass;
    }

    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }

    public String getCssId() {
        return cssId;
    }

    public void setCssId(String cssId) {
        this.cssId = cssId;
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
        data.put("styleClass", this.styleClass);
        data.put("cssId", this.cssId);
        data.put("cssName", this.cssName);
        return data;
    }
    
    @Override
    public void setDataMap(Map<String, Object> data) throws FormEncodingException {
        super.setDataMap(data);
        this.value = (String) data.get("value");
        this.styleClass = (String) data.get("styleClass");
        this.cssId = (String) data.get("cssId");
        this.cssName = (String) data.get("cssName");
    }

    
    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) return false;
        if (!(obj instanceof HeaderRepresentation)) return false;
        HeaderRepresentation other = (HeaderRepresentation) obj;
        boolean equals = (this.value == null && other.value == null) || (this.value != null && this.value.equals(other.value));
        if (!equals) return equals;
        equals = (this.styleClass == null && other.styleClass == null) || (this.styleClass != null && this.styleClass.equals(other.styleClass));
        if (!equals) return equals;
        equals = (this.cssId == null && other.cssId == null) || (this.cssId != null && this.cssId.equals(other.cssId));
        if (!equals) return equals;
        equals = (this.cssName == null && other.cssName == null) || (this.cssName != null && this.cssName.equals(other.cssName));
        return equals;
    }
    
    @Override
    public int hashCode() {
        int result = super.hashCode();
        int aux = this.value == null ? 0 : this.value.hashCode();
        result = 37 * result + aux;
        aux = this.styleClass == null ? 0 : this.styleClass.hashCode();
        result = 37 * result + aux;
        aux = this.cssId == null ? 0 : this.cssId.hashCode();
        result = 37 * result + aux;
        aux = this.cssName == null ? 0 : this.cssName.hashCode();
        result = 37 * result + aux;
        return result;
    }
}
