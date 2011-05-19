package org.jbpm.formbuilder.client.menu;

import java.util.Map;

import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class TextFieldFormItem extends FormItem {

    private final TextBox textBox = new TextBox();
    
    public TextFieldFormItem() {
        super();
        getPanel().add(textBox);
    }
    
    @Override
    public String asCode(String type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Widget createInplaceEditor() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void saveValues(Map<String, Object> asPropertiesMap) {
        // TODO Auto-generated method stub
        
    }

}
