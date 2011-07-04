package org.jbpm.formbuilder.shared.rep.items;

import java.util.Map;

import org.jbpm.formbuilder.shared.form.FormEncodingException;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;

public class OptionRepresentation extends FormItemRepresentation {

    private String label;
    private String value;

    public OptionRepresentation() {
        super("option");
    }
    
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    @Override
    public Map<String, Object> getDataMap() {
        Map<String, Object> data = super.getDataMap();
        data.put("label", this.label);
        data.put("value", this.value);
        return data;
    }
    
    @Override
    public void setDataMap(Map<String, Object> data) throws FormEncodingException {
    	super.setDataMap(data);
    	this.label = (String) data.get("label");
    	this.value = (String) data.get("value");
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) return false;
        if (!(obj instanceof OptionRepresentation)) return false;
        OptionRepresentation other = (OptionRepresentation) obj;
        boolean equals = (this.label == null && other.label == null) || (this.label != null && this.label.equals(other.label));
        if (!equals) return equals;
        equals = (this.value == null && other.value == null) || (this.value != null && this.value.equals(other.value));
        return equals;
    }
    
    @Override
    public int hashCode() {
        int result = super.hashCode();
        int aux = this.label == null ? 0 : this.label.hashCode();
        result = 37 * result + aux;
        aux = this.value == null ? 0 : this.value.hashCode();
        result = 37 * result + aux;
        return result;
    }
}
