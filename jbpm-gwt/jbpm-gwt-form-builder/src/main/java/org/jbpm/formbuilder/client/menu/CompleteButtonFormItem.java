package org.jbpm.formbuilder.client.menu;

import com.google.gwt.user.client.ui.Button;

public class CompleteButtonFormItem extends FormItem {

    public CompleteButtonFormItem() {
        getPanel().add(new Button("Complete"));
    }
    
    @Override
    public String asCode(String type) {
        // TODO Auto-generated method stub
        return null;
    }

}
