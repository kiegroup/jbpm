package org.jbpm.formbuilder.shared.menu;

import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.menu.FBMenuItem;
import org.jbpm.formbuilder.client.options.MainMenuOption;

public interface MenuService {

    List<MainMenuOption> listOptions();
    
    Map<String, List<FBMenuItem>> listItems();
    
    void save(FBMenuItem item);
    
    void delete(FBMenuItem item);
}
