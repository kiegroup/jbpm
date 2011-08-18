package org.jbpm.formbuilder.client.edition;

import java.util.Map;

import org.jbpm.formbuilder.client.form.FBFormItem;

public interface EditionView {

    public interface Presenter {
        
        void onSaveChanges(Map<String, Object> oldProps, Map<String, Object> newProps, FBFormItem itemSelected);

        void onResetChanges(FBFormItem fakeItem, Map<String, Object> newItems);
        
    };
    
    void populate(final FBFormItem itemSelected);
    
    void clear();
    
    void selectTab();
}
