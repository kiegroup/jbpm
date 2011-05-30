package org.jbpm.formbuilder.client.form;

import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;

import com.google.gwt.user.client.ui.TextBox;

public class TextFieldFormItem extends FBFormItem {

    private final TextBox textBox = new TextBox();
    
    public TextFieldFormItem(List<FBFormEffect> formEffects) {
        super(formEffects);
        add(textBox);
    }
    
    @Override
    public String asCode(String type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void saveValues(Map<String, Object> asPropertiesMap) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public FormItemRepresentation getRepresentation() {
        // TODO Auto-generated method stub
        return null;
    }

}
