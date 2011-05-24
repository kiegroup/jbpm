package org.jbpm.formbuilder.client.form;

import java.util.Map;

import com.google.gwt.user.client.ui.PasswordTextBox;

public class PasswordFieldFormItem extends FBFormItem {

    private final PasswordTextBox textBox = new PasswordTextBox();
    
    public PasswordFieldFormItem() {
        super();
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
}
