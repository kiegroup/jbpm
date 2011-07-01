package org.jbpm.formbuilder.shared.menu;

import java.util.List;
import java.util.Map;

public interface MenuService {

    List<MenuOptionDescription> listOptions();
    
    Map<String, List<MenuItemDescription>> listItems();
    
    void save(String groupName, MenuItemDescription item) throws MenuServiceException;
    
    void delete(MenuItemDescription item);
}
