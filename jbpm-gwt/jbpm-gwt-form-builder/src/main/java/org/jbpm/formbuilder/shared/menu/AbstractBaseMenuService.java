package org.jbpm.formbuilder.shared.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractBaseMenuService implements MenuService {

    protected void removeFromMap(String groupName, MenuItemDescription item, Map<String, List<MenuItemDescription>> items) {
        String group = groupName == null ? "Custom" : groupName;
        List<MenuItemDescription> customItems = items.get(group);
        if (customItems == null) {
            customItems = new ArrayList<MenuItemDescription>();
        }
        customItems.remove(item);
        if (customItems.isEmpty()) {
            items.remove(group);
        } else {
            items.put(group, customItems);
        }
    }

    protected void addToMap(String groupName, MenuItemDescription item, Map<String, List<MenuItemDescription>> items) {
        String group = groupName == null ? "Custom" : groupName;
        List<MenuItemDescription> customItems = items.get(group);
        if (customItems == null) {
            customItems = new ArrayList<MenuItemDescription>();
        }
        customItems.add(item);
        items.put(group, customItems);
    }

}
