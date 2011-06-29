package org.jbpm.formbuilder.shared.rep.items;

import java.util.Map;

import org.jbpm.formbuilder.shared.form.FormEncodingException;
import org.jbpm.formbuilder.shared.form.FormEncodingFactory;
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
    public void setDataMap(Map<String, Object> data) {
        super.setDataMap(data);
        this.inputName = (String) data.get("inputName");
        this.variableName = (String) data.get("variableName");
        try {
            this.loopBlock = (FormItemRepresentation) FormEncodingFactory.getDecoder().decode((Map<String, Object>) data.get("loopBlock"));
        } catch (FormEncodingException e) {
            this.loopBlock = null; //TODO see how to handle this error
        }
    }
}
