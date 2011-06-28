package org.jbpm.formbuilder.server.menu;

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.jbpm.formbuilder.shared.menu.MenuItemDescription;
import org.jbpm.formbuilder.shared.menu.MenuOptionDescription;

public class GuvnorMenuServiceTest extends TestCase {

    public void testListOptions() throws Exception {
        GuvnorMenuService service = new GuvnorMenuService();
        List<MenuOptionDescription> options = service.listOptions();
        assertNotNull("options shouldn't be null", options);
        assertFalse("options shouldn't be empty", options.isEmpty());
    }
    
    public void testListItems() throws Exception {
        GuvnorMenuService service = new GuvnorMenuService();
        Map<String, List<MenuItemDescription>> items = service.listItems();
        assertNotNull("items shouldn't be null", items);
        assertFalse("items shouldn't be empty", items.isEmpty());
        for (String key : items.keySet()) {
            assertNotNull("items of key " + key + " shouldn't be null", items.get(key));
            assertFalse("items of key " + key + " shouldn't be empty", items.get(key).isEmpty());
        }
    }
}
