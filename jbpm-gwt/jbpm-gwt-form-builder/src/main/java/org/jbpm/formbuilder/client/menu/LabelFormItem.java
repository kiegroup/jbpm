package org.jbpm.formbuilder.client.menu;

import com.google.gwt.user.client.ui.Label;

public class LabelFormItem extends FormItem {

    private final Label label = new Label("Label");
    
    public LabelFormItem() {
        getPanel().add(label);
    }
    
    @Override
    public String asCode(String type) {
        // TODO Auto-generated method stub
        return null;
    }

}
