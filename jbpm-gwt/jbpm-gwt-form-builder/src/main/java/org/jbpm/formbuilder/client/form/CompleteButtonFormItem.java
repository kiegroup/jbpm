package org.jbpm.formbuilder.client.form;

import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;

import com.google.gwt.user.client.ui.Button;

public class CompleteButtonFormItem extends FBFormItem {

    private Button button = new Button("Complete");

    private String height;
    private String width;
    private String innerText;
    private String cssStyleName;
    
    public CompleteButtonFormItem(List<FBFormEffect> formEffects) {
        super(formEffects);
        add(button);
    }
    
    @Override
    public String asCode(String type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void saveValues(Map<String, Object> asPropertiesMap) {
        this.height = asPropertiesMap.get("height").toString();
        this.width = asPropertiesMap.get("width").toString();
        this.innerText = asPropertiesMap.get("innerText").toString();
        this.cssStyleName = asPropertiesMap.get("cssStyleName").toString();
        
        button.setHeight(this.height);
        button.setWidth(this.width);
        button.setText(this.innerText);
        button.setStyleName(this.cssStyleName);
    }

    @Override
    public Map<String, Object> getFormItemPropertiesMap() {
        Map<String, Object> map = super.getFormItemPropertiesMap();
        map.put("height", this.height);
        map.put("width", this.width);
        map.put("innerText", this.innerText);
        map.put("cssStyleName", this.cssStyleName);
        return map;
    }
    
    @Override
    public FormItemRepresentation getRepresentation() {
        // TODO Auto-generated method stub
        return null;
    }
}
