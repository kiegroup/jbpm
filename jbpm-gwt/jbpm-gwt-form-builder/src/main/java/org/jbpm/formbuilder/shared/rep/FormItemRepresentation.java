package org.jbpm.formbuilder.shared.rep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.form.FormEncodingClientFactory;
import org.jbpm.formbuilder.shared.form.FormEncodingException;
import org.jbpm.formbuilder.shared.form.FormRepresentationDecoder;

public abstract class FormItemRepresentation implements Mappable {
    
    private List<FBValidation> itemValidations = new ArrayList<FBValidation>();
    private OutputData output;
    private InputData input;
    
    private String width;
    private String height;
    
    private final String typeId;
    private final String itemClassName;
    
    private List<String> effectClasses = new ArrayList<String>();
    
    public FormItemRepresentation(String typeId) {
        this.typeId = typeId;
        this.itemClassName = RepresentationFactory.getItemClassName(getClass().getName());
    }

    public List<FBValidation> getItemValidations() {
        return itemValidations;
    }

    public void setItemValidations(List<FBValidation> itemValidations) {
        this.itemValidations = itemValidations;
    }

    public OutputData getOutput() {
        return output;
    }

    public void setOutput(OutputData output) {
        this.output = output;
    }

    public InputData getInput() {
        return input;
    }

    public void setInput(InputData input) {
        this.input = input;
    }
    
    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getTypeId() {
        return typeId;
    }
    
    public String getItemClassName() {
        return itemClassName;
    }
    
    public List<String> getEffectClasses() {
        return effectClasses;
    }
    
    public void setEffectClasses(List<String> effectClasses) {
        this.effectClasses = effectClasses;
    }
    
    public boolean addEffectClass(Class<?> clazz) {
        return effectClasses.add(clazz.getName());
    }

    public Map<String, Object> getDataMap() {
    	Map<String, Object> data = new HashMap<String, Object>();
    	
    	data.put("@className", getClass().getName());
        List<Object> validationsMap = new ArrayList<Object>();
        if (this.itemValidations != null) {
	        for (FBValidation valid : this.itemValidations) {
	        	Map<String, Object> map = valid.getDataMap();
	        	validationsMap.add(map);
	        }
        }
        data.put("itemValidations", validationsMap);
        data.put("effectClasses", new ArrayList<Object>(this.effectClasses));
        data.put("output", this.output == null ? null : this.output.getDataMap());
        data.put("input", this.input == null ? null : this.input.getDataMap());
        data.put("width", this.width);
        data.put("height", this.height);
        data.put("typeId", this.typeId);
    	return data;
    }
    
    @SuppressWarnings("unchecked")
    public void setDataMap(Map<String, Object> data) throws FormEncodingException {
        FormRepresentationDecoder decoder = FormEncodingClientFactory.getDecoder();
        List<Object> validationsMap = (List<Object>) data.get("itemValidations");
        this.itemValidations.clear();
        if (validationsMap != null) {
	        for (Object obj : validationsMap) {
	            Map<String, Object> validMap = (Map<String, Object>) obj;
	        	FBValidation validation = (FBValidation) decoder.decode(validMap);
	        	this.itemValidations.add(validation);
	        }
        }
        List<Object> effectClassesObj = (List<Object>) data.get("effectClasses");
        effectClasses.clear();
        if (effectClassesObj != null) {
            for (Object obj : effectClassesObj) {
                effectClasses.add(obj.toString());
            }
        }
        this.output = (OutputData) decoder.decode((Map<String, Object>) data.get("output"));
        this.input = (InputData) decoder.decode((Map<String, Object>) data.get("input"));
        this.width = (String) data.get("width");
        this.height = (String) data.get("height");
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (!(obj instanceof FormItemRepresentation)) return false;
        FormItemRepresentation other = (FormItemRepresentation) obj;
        boolean equals = (this.typeId == null && other.typeId == null) || (this.typeId != null && this.typeId.equals(other.typeId));
        if (!equals) return equals;
        equals = (this.output == null && other.output == null) || (this.output != null && this.output.equals(other.output));
        if (!equals) return equals;
        equals = (this.input == null && other.input == null) || (this.input != null && this.input.equals(other.input));
        if (!equals) return equals;
        equals = (this.effectClasses == null && other.effectClasses == null) || 
            (this.effectClasses != null && this.effectClasses.equals(other.effectClasses));
        if (!equals) return equals;
        equals = (this.itemValidations == null && other.itemValidations == null) || 
            (this.itemValidations != null && this.itemValidations.equals(other.itemValidations));
        if (!equals) return equals;
        equals = (this.width == null && other.width == null) || (this.width != null && this.width.equals(other.width));
        if (!equals) return equals;
        equals = (this.height == null && other.height == null) || (this.height != null && this.height.equals(other.height));
        if (!equals) return equals;
        return equals;
    }
    
    @Override
    public int hashCode() {
        int result = super.hashCode();
        int aux = this.typeId == null ? 0 : this.typeId.hashCode();
        result = 37 * result + aux;
        aux = this.output == null ? 0 : this.output.hashCode();
        result = 37 * result + aux;
        aux = this.input == null ? 0 : this.input.hashCode();
        result = 37 * result + aux;
        aux = this.effectClasses == null ? 0 : this.effectClasses.hashCode();
        result = 37 * result + aux;
        aux = this.itemValidations == null ? 0 : this.itemValidations.hashCode();
        result = 37 * result + aux;
        aux = this.width == null ? 0 : this.width.hashCode();
        result = 37 * result + aux;
        aux = this.height == null ? 0 : this.height.hashCode();
        result = 37 * result + aux;
        return result;
    }
}
