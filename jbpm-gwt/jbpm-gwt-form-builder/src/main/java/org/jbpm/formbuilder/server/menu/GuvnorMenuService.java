package org.jbpm.formbuilder.server.menu;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.menu.FBMenuItem;
import org.jbpm.formbuilder.client.options.MainMenuOption;
import org.jbpm.formbuilder.shared.menu.MenuService;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class GuvnorMenuService implements MenuService {

    public List<MainMenuOption> listOptions() {
        Gson gson = new Gson();
        URL url = getClass().getResource("/menuOptions.json");
        List<MainMenuOption> retval = new ArrayList<MainMenuOption>();
        try {
            File menuOptionsFile = new File(url.toURI());
            Collection<MainMenuOption> options = gson.fromJson(
                    new FileReader(menuOptionsFile), 
                new TypeToken<Collection<MainMenuOption>>() {}.getType());
            retval.addAll(options);
        } catch (URISyntaxException e) {
            //TODO throw error
        } catch (FileNotFoundException e) {
            //TODO throw error
        }
        return retval;
    }

    public Map<String, List<FBMenuItem>> listItems() {
        Gson gson = new Gson();
        URL url = getClass().getResource("/menuItems.json");
        Map<String, List<FBMenuItem>> retval = new HashMap<String, List<FBMenuItem>>();
        try {
            File menuItemsFile = new File(url.toURI());
            Map<String, List<FBMenuItem>> items = gson.fromJson(
                    new FileReader(menuItemsFile), 
                new TypeToken<Map<String, List<FBMenuItem>>>() {}.getType());
            retval.putAll(items);
        } catch (URISyntaxException e) {
            //TODO throw error
        } catch (FileNotFoundException e) {
            //TODO throw error
        }
        return retval;
    }

    public void save(FBMenuItem item) {
        Map<String, List<FBMenuItem>> items = listItems();
        List<FBMenuItem> customItems = items.get("Custom");
        if (customItems == null) {
            customItems = new ArrayList<FBMenuItem>();
        }
        customItems.add(item);
        items.put("Custom", customItems);
        try {
            saveToFile(items);
        } catch (URISyntaxException e) {
            //TODO throw error
        } catch (IOException e) {
            //TODO throw error
        }
    }

    private void saveToFile(Map<String, List<FBMenuItem>> items)
            throws URISyntaxException, IOException {
        Gson gson = new Gson();
        URL url = getClass().getResource("/menuItems.json");
        String json = gson.toJson(items, new TypeToken<Map<String, List<FBMenuItem>>>() {}.getType());
        File menuItemsFile = new File(url.toURI());
        FileWriter writer = new FileWriter(menuItemsFile);
        writer.write(json);
        writer.flush();
        writer.close();
    }

    public void delete(FBMenuItem item) {
        Map<String, List<FBMenuItem>> items = listItems();
        List<FBMenuItem> customItems = items.get("Custom");
        if (customItems == null) {
            customItems = new ArrayList<FBMenuItem>();
        }
        customItems.remove(item);
        items.put("Custom", customItems);
        try {
            saveToFile(items);
        } catch (URISyntaxException e) {
            //TODO throw error
        } catch (IOException e) {
            //TODO throw error
        }
    }

}
