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

import org.jbpm.formbuilder.client.FormBuilderException;
import org.jbpm.formbuilder.client.command.BaseCommand;
import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.menu.FBMenuItem;
import org.jbpm.formbuilder.client.options.MainMenuOption;
import org.jbpm.formbuilder.shared.menu.FormEffectDescription;
import org.jbpm.formbuilder.shared.menu.MenuItemDescription;
import org.jbpm.formbuilder.shared.menu.MenuOptionDescription;
import org.jbpm.formbuilder.shared.menu.MenuService;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class GuvnorMenuService implements MenuService {

    public List<MenuOptionDescription> listOptions() {
        Gson gson = new Gson();
        URL url = getClass().getResource("/menuOptions.json");
        List<MenuOptionDescription> retval = new ArrayList<MenuOptionDescription>();
        try {
            File menuOptionsFile = new File(url.toURI());
            Collection<MenuOptionDescription> options = gson.fromJson(
                    new FileReader(menuOptionsFile), 
                new TypeToken<Collection<MenuOptionDescription>>() {}.getType());
            retval.addAll(options);
        } catch (URISyntaxException e) {
            //TODO throw error
        } catch (FileNotFoundException e) {
            //TODO throw error
        }
        return retval;
    }

    public Map<String, List<MenuItemDescription>> listItems() {
        Gson gson = new Gson();
        URL url = getClass().getResource("/menuItems.json");
        Map<String, List<MenuItemDescription>> retval = new HashMap<String, List<MenuItemDescription>>();
        try {
            File menuItemsFile = new File(url.toURI());
            Map<String, List<MenuItemDescription>> items = gson.fromJson(
                    new FileReader(menuItemsFile), 
                new TypeToken<Map<String, List<MenuItemDescription>>>() {}.getType());
            retval.putAll(items);
        } catch (URISyntaxException e) {
            //TODO throw error
        } catch (FileNotFoundException e) {
            //TODO throw error
        }
        return retval;
    }

    public void save(MenuItemDescription item) {
        Map<String, List<MenuItemDescription>> items = listItems();
        List<MenuItemDescription> customItems = items.get("Custom");
        if (customItems == null) {
            customItems = new ArrayList<MenuItemDescription>();
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

    private void saveToFile(Map<String, List<MenuItemDescription>> items)
            throws URISyntaxException, IOException {
        Gson gson = new Gson();
        URL url = getClass().getResource("/menuItems.json");
        String json = gson.toJson(items, new TypeToken<Map<String, List<MenuItemDescription>>>() {}.getType());
        File menuItemsFile = new File(url.toURI());
        FileWriter writer = new FileWriter(menuItemsFile);
        writer.write(json);
        writer.flush();
        writer.close();
    }

    public void delete(MenuItemDescription item) {
        Map<String, List<MenuItemDescription>> items = listItems();
        List<MenuItemDescription> customItems = items.get("Custom");
        if (customItems == null) {
            customItems = new ArrayList<MenuItemDescription>();
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
