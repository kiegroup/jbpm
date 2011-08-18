package org.jbpm.formbuilder.client.options;

import java.util.List;

import com.google.gwt.user.client.ui.MenuItem;

public interface OptionsView {

    interface Presenter {
        
    };
    
    void addItem(MainMenuOption option);

    List<MenuItem> getItems();
    
}
