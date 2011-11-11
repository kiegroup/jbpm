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
package org.jbpm.formbuilder.shared.api;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.formbuilder.common.reflect.ReflectionHelper;
import org.jbpm.formbuilder.shared.form.FormEncodingException;

import com.gwtent.reflection.client.Reflectable;

@Reflectable
public class FBScript implements Mappable {

    private String documentation;
    private String id;
    
    private FBScriptHelper helper;
    
    private String type;
    private String src;
    private String content;
    private String invokeFunction;

    public String getDocumentation() {
        return documentation;
    }

    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getContent() {
        if (helper != null) {
            return helper.asScriptContent();
        }
        return content;
    }
    
    public void setHelper(FBScriptHelper helper) {
        this.helper = helper;
    }
    
    public FBScriptHelper getHelper() {
        return helper;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getInvokeFunction() {
        return invokeFunction;
    }

    public void setInvokeFunction(String invokeFunction) {
        this.invokeFunction = invokeFunction;
    }

    @Override
    public Map<String, Object> getDataMap() {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("@className", getClass().getName());
        data.put("documentation", this.documentation);
        data.put("id", this.id);
        data.put("type", this.type);
        data.put("src", this.src);
        data.put("content", this.content);
        if (getHelper() != null) {
            data.put("helper", getHelper().getDataMap());
        }
        data.put("invokeFunction", this.invokeFunction);
        return data;
    }

    @Override
    public void setDataMap(Map<String, Object> dataMap) throws FormEncodingException {
        this.documentation = (String) dataMap.get("documentation");
        this.id = (String) dataMap.get("id");
        this.type = (String) dataMap.get("type");
        this.src = (String) dataMap.get("src");
        this.content = (String) dataMap.get("content");
        @SuppressWarnings("unchecked")
        Map<String, Object> helperMap = (Map<String, Object>) dataMap.get("helper");
        if (helperMap != null) {
            try {
                String helperClass = (String) helperMap.get("@className");
                this.helper = (FBScriptHelper) ReflectionHelper.newInstance(helperClass);
            } catch (Exception e) {
                throw new FormEncodingException("Problem creating helper", e);
            }
            this.helper.setDataMap(helperMap);
        }
        this.invokeFunction = (String) dataMap.get("invokeFunction");
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (!(obj instanceof FBScript)) return false;
        FBScript other = (FBScript) obj;
        boolean equals = (this.documentation == null && other.documentation == null) || 
            (this.documentation != null && this.documentation.equals(other.documentation));
        if (!equals) return equals;
        equals = (this.id == null && other.id == null) || (this.id != null && this.id.equals(other.id));
        if (!equals) return equals;
        equals = (this.type == null && other.type == null) || (this.type != null && this.type.equals(other.type));
        if (!equals) return equals;
        equals = (this.src == null && other.src == null) || (this.src != null && this.src.equals(other.src));
        if (!equals) return equals;
        equals = (this.content == null && other.content == null) || (this.content != null && this.content.equals(other.content));
        if (!equals) return equals;
        equals = (this.invokeFunction == null && other.invokeFunction == null) || 
            (this.invokeFunction != null && this.invokeFunction.equals(other.invokeFunction));
        if (!equals) return equals;
        return equals;
    }
    
    @Override
    public int hashCode() {
        int result = super.hashCode();
        int aux = this.documentation == null ? 0 : this.documentation.hashCode();
        result = 37 * result + aux;
        aux = this.id == null ? 0 : this.id.hashCode();
        result = 37 * result + aux;
        aux = this.type == null ? 0 : this.type.hashCode();
        result = 37 * result + aux;
        aux = this.src == null ? 0 : this.src.hashCode();
        result = 37 * result + aux;
        aux = this.content == null ? 0 : this.content.hashCode();
        result = 37 * result + aux;
        aux = this.invokeFunction == null ? 0 : this.invokeFunction.hashCode();
        result = 37 * result + aux;
        return result;
    }
}
