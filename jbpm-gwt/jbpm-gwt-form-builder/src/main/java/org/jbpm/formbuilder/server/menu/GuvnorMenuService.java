/*
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
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.jbpm.formapi.shared.form.FormEncodingException;
import org.jbpm.formapi.shared.form.FormEncodingFactory;
import org.jbpm.formapi.shared.form.FormRepresentationDecoder;
import org.jbpm.formapi.shared.form.FormRepresentationEncoder;
import org.jbpm.formapi.shared.menu.MenuItemDescription;
import org.jbpm.formapi.shared.menu.MenuOptionDescription;
import org.jbpm.formapi.shared.menu.ValidationDescription;
import org.jbpm.formbuilder.shared.menu.AbstractBaseMenuService;
import org.jbpm.formbuilder.shared.menu.MenuServiceException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class GuvnorMenuService extends AbstractBaseMenuService {

    @Override
    public List<MenuOptionDescription> listOptions() throws MenuServiceException {
    	Gson gson = new Gson();
        List<MenuOptionDescription> retval = null;
        try {
            File file = new File(asURI("/menuOptions.json"));
            retval = gson.fromJson(createReader(file), new TypeToken<List<MenuOptionDescription>>(){}.getType());
        } catch (URISyntaxException e) {
            throw new MenuServiceException("Problem finding menu options json file", e); 
        } catch (FileNotFoundException e) {
            throw new MenuServiceException("No menu options json file found", e);
        } catch (Exception e) {
            throw new MenuServiceException("Unexpected error", e);
        }
        return retval;
    }

    @Override
    public Map<String, List<MenuItemDescription>> listMenuItems() throws MenuServiceException {
        Map<String, List<MenuItemDescription>> retval = null;
        try {
        	FormRepresentationDecoder decoder = FormEncodingFactory.getDecoder();
        	File file = new File(asURI("/menuItems.json"));
        	String json = readFile(file);
        	retval = decoder.decodeMenuItemsMap(json);
        } catch (FormEncodingException e) {
            throw new MenuServiceException("Problem parsing menu items json file", e);
        } catch (URISyntaxException e) {
            throw new MenuServiceException("Problem finding menu items json file", e);
        } catch (FileNotFoundException e) {
            throw new MenuServiceException("No menu items json file found", e);
        } catch (IOException e) {
            throw new MenuServiceException("Problem reading menu items json file", e);
        } catch (Exception e) {
            throw new MenuServiceException("Unexpected error", e);
        }
        return retval;
    }

    @Override
    public List<ValidationDescription> listValidations() throws MenuServiceException {
        Gson gson = new Gson();
        List<ValidationDescription> retval = null;
        try {
            File file = new File(asURI("/validations.json"));
            retval = gson.fromJson(createReader(file), new TypeToken<List<ValidationDescription>>(){}.getType());
        } catch (URISyntaxException e) {
            throw new MenuServiceException("Problem finding validations json file", e); 
        } catch (FileNotFoundException e) {
            throw new MenuServiceException("No validations json file found", e);
        } catch (Exception e) {
            throw new MenuServiceException("Unexpected error", e);
        }
        return retval;
    }
    
    @Override
    public void saveMenuItem(String groupName, MenuItemDescription item) throws MenuServiceException {
        Map<String, List<MenuItemDescription>> items = listMenuItems();
        addToMap(groupName, item, items);
        writeMenuItems(items);
    }
    
    @Override
    public void deleteMenuItem(String groupName, MenuItemDescription item) throws MenuServiceException {
        Map<String, List<MenuItemDescription>> items = listMenuItems();
        removeFromMap(groupName, item, items);
        writeMenuItems(items);
    }

    @Override
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
        try {
            FormRepresentationEncoder encoder = FormEncodingFactory.getEncoder();
            File file = new File(asURI("/menuItems.json"));
            String json = encoder.encodeMenuItemsMap(items);
            writeFile(file, json);
        } catch (FormEncodingException e) {
            throw new MenuServiceException("Problem transforming menu items to json", e);
        } catch (URISyntaxException e) {
            throw new MenuServiceException("Problem finding menu items json file", e);
        } catch (FileNotFoundException e) {
            throw new MenuServiceException("No menu items json file found", e);
        } catch (IOException e) {
            throw new MenuServiceException("Problem writing menu items json file", e);
        } catch (Exception e) {
            throw new MenuServiceException("Unexpected error", e);
        }
    }

    protected void writeFile(File file, String json) throws FileNotFoundException, IOException {
        FileUtils.writeStringToFile(file, json);
    }
    
    protected URI asURI(String path) throws URISyntaxException {
        URL url = getClass().getResource(path);
        return url.toURI();
    }
    
    protected Reader createReader(File file) throws FileNotFoundException, IOException {
        return new FileReader(file);
    }

    protected String readFile(File file) throws FileNotFoundException, IOException {
        return FileUtils.readFileToString(file);
    }
}
