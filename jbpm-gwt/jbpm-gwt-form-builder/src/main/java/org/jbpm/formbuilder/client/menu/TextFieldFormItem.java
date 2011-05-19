package org.jbpm.formbuilder.client.menu;

import com.google.gwt.user.client.ui.TextBox;

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

}
