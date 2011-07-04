package org.jbpm.formbuilder.server.menu;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.jbpm.formbuilder.server.form.FormEncodingServerFactory;
import org.jbpm.formbuilder.shared.form.FormEncodingException;
import org.jbpm.formbuilder.shared.form.FormRepresentationDecoder;
import org.jbpm.formbuilder.shared.form.FormRepresentationEncoder;
import org.jbpm.formbuilder.shared.menu.MenuItemDescription;
import org.jbpm.formbuilder.shared.menu.MenuOptionDescription;
import org.jbpm.formbuilder.shared.menu.MenuService;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class GuvnorMenuService implements MenuService {

    public List<MenuOptionDescription> listOptions() {
    	Gson gson = new Gson();
        URL url = getClass().getResource("/menuOptions.json");
        List<MenuOptionDescription> retval = null;
        try {
            File file = new File(url.toURI());
            retval = gson.fromJson(new FileReader(file), 
            		new TypeToken<List<MenuOptionDescription>>(){}.getType());
        } catch (URISyntaxException e) {
            //TODO throw error
        } catch (FileNotFoundException e) {
            //TODO throw error
        }
        return retval;
    }

    public Map<String, List<MenuItemDescription>> listItems() {
        URL url = getClass().getResource("/menuItems.json");
        Map<String, List<MenuItemDescription>> retval = null;
        try {
        	FormRepresentationDecoder decoder = FormEncodingServerFactory.getDecoder();
        	File file = new File(url.toURI());
        	String json = FileUtils.readFileToString(file);
        	retval = decoder.decodeMenuItemsMap(json);
        } catch (FormEncodingException e) {
            //TODO throw error
        } catch (URISyntaxException e) {
            //TODO throw error
        } catch (FileNotFoundException e) {
        	//TODO throw error
        } catch (IOException e) {
        	//TODO throw error
        }
        return retval;
    }
    
    public void save(String groupName, MenuItemDescription item) {
        String group = groupName == null ? "Custom" : groupName;
        Map<String, List<MenuItemDescription>> items = listItems();
        List<MenuItemDescription> customItems = items.get(group);
        if (customItems == null) {
            customItems = new ArrayList<MenuItemDescription>();
        }
        customItems.add(item);
        items.put(group, customItems);
        writeMenuItems(items);
    }
    
    public void delete(String groupName, MenuItemDescription item) {
        String group = groupName == null ? "Custom" : groupName;
        Map<String, List<MenuItemDescription>> items = listItems();
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
        writeMenuItems(items);
    }

    private void writeMenuItems(Map<String, List<MenuItemDescription>> items) {
        URL url = getClass().getResource("/menuItems.json");
        try {
            FormRepresentationEncoder encoder = FormEncodingServerFactory.getEncoder();
            File file = new File(url.toURI());
            String json = encoder.encodeMenuItemsMap(items);
            FileUtils.writeStringToFile(file, json);
        } catch (FormEncodingException e) {
            //TODO throw error
        } catch (URISyntaxException e) {
            //TODO throw error
        } catch (FileNotFoundException e) {
            //TODO throw error
        } catch (IOException e) {
            //TODO throw error
        }
    }
}
