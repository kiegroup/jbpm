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
