package org.jbpm.formbuilder.shared.rep.items;

import java.util.Map;

import org.jbpm.formbuilder.client.form.FormEncodingClientFactory;
import org.jbpm.formbuilder.shared.form.FormEncodingException;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;

public class LoopBlockRepresentation extends FormItemRepresentation {

    private String inputName;
    private String variableName;
    private FormItemRepresentation loopBlock;
    
    public LoopBlockRepresentation() {
        super("loopBlock");
    }

    public String getInputName() {
        return inputName;
    }

    public void setInputName(String inputName) {
        this.inputName = inputName;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public FormItemRepresentation getLoopBlock() {
        return loopBlock;
    }

    public void setLoopBlock(FormItemRepresentation loopBlock) {
        this.loopBlock = loopBlock;
    }
    
    @Override
    public Map<String, Object> getDataMap() {
        Map<String, Object> data = super.getDataMap();
        data.put("inputName", this.inputName);
        data.put("variableName", this.variableName);
        data.put("loopBlock", this.loopBlock == null ? null : this.loopBlock.getDataMap());
        return data;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void setDataMap(Map<String, Object> data) throws FormEncodingException {
        super.setDataMap(data);
        this.inputName = (String) data.get("inputName");
        this.variableName = (String) data.get("variableName");
        this.loopBlock = (FormItemRepresentation) FormEncodingClientFactory.getDecoder().decode((Map<String, Object>) data.get("loopBlock"));
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) return false;
        if (!(obj instanceof LoopBlockRepresentation)) return false;
        LoopBlockRepresentation other = (LoopBlockRepresentation) obj;
        boolean equals = (this.inputName == null && other.inputName == null) || 
            (this.inputName != null && this.inputName.equals(other.inputName));
        if (!equals) return equals;
        equals = (this.variableName == null && other.variableName == null) || 
            (this.variableName != null && this.variableName.equals(other.variableName));
        if (!equals) return equals;
        equals = (this.loopBlock == null && other.loopBlock == null) || 
            (this.loopBlock != null && this.loopBlock.equals(other.loopBlock));
        return equals;
    }
    
    @Override
    public int hashCode() {
        int result = super.hashCode();
        int aux = this.inputName == null ? 0 : this.inputName.hashCode();
        result = 37 * result + aux;
        aux = this.variableName == null ? 0 : this.variableName.hashCode();
        result = 37 * result + aux;
        aux = this.loopBlock == null ? 0 : this.loopBlock.hashCode();
        result = 37 * result + aux;
        return result;
    }
}
