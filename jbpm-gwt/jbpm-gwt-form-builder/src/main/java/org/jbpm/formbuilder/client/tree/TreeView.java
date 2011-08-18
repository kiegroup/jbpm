package org.jbpm.formbuilder.client.tree;

import org.jbpm.formbuilder.client.form.FBCompositeItem;
import org.jbpm.formbuilder.client.form.FBFormItem;

public interface TreeView {

    public interface Presenter {
        
    };
    
    void removeFormItem(FBFormItem item);

    void addFormItem(FBFormItem item, FBCompositeItem parentItem);

}
