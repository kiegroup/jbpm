package org.jbpm.formbuilder.server.menu;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.shared.menu.MenuItemDescription;
import org.jbpm.formbuilder.shared.menu.MenuOptionDescription;
import org.jbpm.formbuilder.shared.menu.MenuService;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class GuvnorMenuService implements MenuService {

    public List<MenuOptionDescription> listOptions() {
        return read("/menuOptions.json", new TypeToken<List<MenuOptionDescription>>(){});
    }

    public Map<String, List<MenuItemDescription>> listItems() {
        return read("/menuItems.json", new TypeToken<Map<String, List<MenuItemDescription>>>(){});
    }
    
    public void save(MenuItemDescription item) {
        Map<String, List<MenuItemDescription>> items = listItems();
        List<MenuItemDescription> customItems = items.get("Custom");
        if (customItems == null) {
            customItems = new ArrayList<MenuItemDescription>();
        }
        customItems.add(item);
        items.put("Custom", customItems);
        write("/menuItems.json", items);
    }
    
    public void delete(MenuItemDescription item) {
        Map<String, List<MenuItemDescription>> items = listItems();
        List<MenuItemDescription> customItems = items.get("Custom");
        if (customItems == null) {
            customItems = new ArrayList<MenuItemDescription>();
        }
        customItems.remove(item);
        items.put("Custom", customItems);
        write("/menuItems.json", items);
    }

    @SuppressWarnings("unchecked")
    protected <T extends Object> T read(String jsonFilePath, TypeToken<T> token) {
        Gson gson = new Gson();
        URL url = getClass().getResource(jsonFilePath);
        T retval = null;
        try {
            File file = new File(url.toURI());
            Object value = gson.fromJson(new FileReader(file), token.getType());
            retval = (T) value;
        } catch (URISyntaxException e) {
            //TODO throw error
        } catch (FileNotFoundException e) {
            //TODO throw error
        }
        return retval;
    }

    private <T extends Object> void write(String jsonFilePath, T items) {
        Gson gson = new Gson();
        URL url = getClass().getResource(jsonFilePath);
        String json = gson.toJson(items, new TypeToken<T>() {}.getType());
        try {
            File menuItemsFile = new File(url.toURI());
            FileWriter writer = new FileWriter(menuItemsFile);
            writer.write(json);
            writer.flush();
            writer.close();
        } catch (URISyntaxException e) {
            //TODO throw error
        } catch (IOException e) {
            //TODO throw error
        }
    }
}
