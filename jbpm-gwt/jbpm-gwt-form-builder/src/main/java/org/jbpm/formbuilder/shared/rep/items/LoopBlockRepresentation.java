package org.jbpm.formbuilder.shared.rep.items;

import java.util.Map;

import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.JsonUtil;

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
    public Map<String, Object> getData() {
        Map<String, Object> data = super.getData();
        data.put("inputName", this.inputName);
        data.put("variableName", this.variableName);
        data.put("loopBlock", this.loopBlock == null ? null : this.loopBlock.getData());
        return data;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void setData(Map<String, Object> data) {
        super.setData(data);
        this.inputName = (String) data.get("inputName");
        this.variableName = (String) data.get("variableName");
        this.loopBlock = (FormItemRepresentation) JsonUtil.fromMap((Map<String, Object>) data.get("loopBlock"));
    }
}
