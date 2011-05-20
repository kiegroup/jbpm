package org.jbpm.formbuilder.client.form;

import java.util.Map;


import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;

public class CompleteButtonFormItem extends FBFormItem {

    public CompleteButtonFormItem() {
        add(new Button("Complete"));
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
