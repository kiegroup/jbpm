/**
 * Copyright 2011 JBoss Inc 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.formbuilder.server.menu;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.jbpm.formbuilder.shared.form.FormEncodingException;
import org.jbpm.formbuilder.shared.form.FormEncodingFactory;
import org.jbpm.formbuilder.shared.form.FormRepresentationDecoder;
import org.jbpm.formbuilder.shared.form.FormRepresentationEncoder;
import org.jbpm.formbuilder.shared.menu.AbstractBaseMenuService;
import org.jbpm.formbuilder.shared.menu.MenuItemDescription;
import org.jbpm.formbuilder.shared.menu.MenuOptionDescription;
import org.jbpm.formbuilder.shared.menu.MenuServiceException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class GuvnorMenuService extends AbstractBaseMenuService {

    public List<MenuOptionDescription> listOptions() throws MenuServiceException {
    	Gson gson = new Gson();
        URL url = getClass().getResource("/menuOptions.json");
        List<MenuOptionDescription> retval = null;
        try {
            File file = new File(url.toURI());
            retval = gson.fromJson(new FileReader(file), 
            		new TypeToken<List<MenuOptionDescription>>(){}.getType());
        } catch (URISyntaxException e) {
            throw new MenuServiceException("Problem finding menu options json file", e); 
        } catch (FileNotFoundException e) {
            throw new MenuServiceException("No menu options json file found", e);
        }
        return retval;
    }

    public Map<String, List<MenuItemDescription>> listMenuItems() throws MenuServiceException {
        URL url = getClass().getResource("/menuItems.json");
        Map<String, List<MenuItemDescription>> retval = null;
        try {
        	FormRepresentationDecoder decoder = FormEncodingFactory.getDecoder();
        	File file = new File(url.toURI());
        	String json = FileUtils.readFileToString(file);
        	retval = decoder.decodeMenuItemsMap(json);
        } catch (FormEncodingException e) {
            throw new MenuServiceException("Problem parsing menu items json file", e);
        } catch (URISyntaxException e) {
            throw new MenuServiceException("Problem finding menu items json file", e);
        } catch (FileNotFoundException e) {
            throw new MenuServiceException("No menu items json file found", e);
        } catch (IOException e) {
            throw new MenuServiceException("Problem reading menu items json file", e);
        }
        return retval;
    }
    
    public void saveMenuItem(String groupName, MenuItemDescription item) throws MenuServiceException {
        Map<String, List<MenuItemDescription>> items = listMenuItems();
        addToMap(groupName, item, items);
        writeMenuItems(items);
    }
    
    public void deleteMenuItem(String groupName, MenuItemDescription item) throws MenuServiceException {
        Map<String, List<MenuItemDescription>> items = listMenuItems();
        removeFromMap(groupName, item, items);
        writeMenuItems(items);
    }

    public Map<String, String> getFormBuilderProperties() throws MenuServiceException {
        InputStream input = getClass().getResourceAsStream("/FormBuilder.properties");
        Properties props = new Properties();
        try {
            props.load(input);
        } catch (IOException e) {
            throw new MenuServiceException("Couldn't read FormBuilder.properties", e);
        }
        Map<String, String> retval = new HashMap<String, String>();
        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            retval.put(entry.getKey().toString(), entry.getValue().toString());
        }
        return retval;
    }
    
    private void writeMenuItems(Map<String, List<MenuItemDescription>> items) throws MenuServiceException {
        URL url = getClass().getResource("/menuItems.json");
        try {
            FormRepresentationEncoder encoder = FormEncodingFactory.getEncoder();
            File file = new File(url.toURI());
            String json = encoder.encodeMenuItemsMap(items);
            FileUtils.writeStringToFile(file, json);
        } catch (FormEncodingException e) {
            throw new MenuServiceException("Problem transforming menu items to json", e);
        } catch (URISyntaxException e) {
            throw new MenuServiceException("Problem finding menu items json file", e);
        } catch (FileNotFoundException e) {
            throw new MenuServiceException("No menu items json file found", e);
        } catch (IOException e) {
            throw new MenuServiceException("Problem writing menu items json file", e);
        }
    }
}
