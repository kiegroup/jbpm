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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.jbpm.formbuilder.client.effect.DoneEffect;
import org.jbpm.formbuilder.client.effect.RemoveEffect;
import org.jbpm.formbuilder.client.menu.items.CustomMenuItem;
import org.jbpm.formbuilder.server.form.FormEncodingServerFactory;
import org.jbpm.formbuilder.shared.api.FormItemRepresentation;
import org.jbpm.formbuilder.shared.form.FormEncodingFactory;
import org.jbpm.formbuilder.shared.form.FormRepresentationDecoder;
import org.jbpm.formbuilder.shared.menu.FormEffectDescription;
import org.jbpm.formbuilder.shared.menu.MenuItemDescription;
import org.jbpm.formbuilder.shared.menu.MenuOptionDescription;
import org.jbpm.formbuilder.shared.menu.ValidationDescription;

public class GuvnorMenuServiceTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        FormEncodingFactory.register(FormEncodingServerFactory.getEncoder(), FormEncodingServerFactory.getDecoder());
    }
    
    public void testListOptions() throws Exception {
        GuvnorMenuService service = new GuvnorMenuService();
        List<MenuOptionDescription> options = service.listOptions();
        assertNotNull("options shouldn't be null", options);
        assertFalse("options shouldn't be empty", options.isEmpty());
    }
    
    public void testListItems() throws Exception {
        GuvnorMenuService service = new GuvnorMenuService();
        Map<String, List<MenuItemDescription>> items = service.listMenuItems();
        assertNotNull("items shouldn't be null", items);
        assertFalse("items shouldn't be empty", items.isEmpty());
        for (String key : items.keySet()) {
            assertNotNull("items of key " + key + " shouldn't be null", items.get(key));
            assertFalse("items of key " + key + " shouldn't be empty", items.get(key).isEmpty());
        }
    }
    
    public void testListValidations() throws Exception {
        GuvnorMenuService service = new GuvnorMenuService();
        List<ValidationDescription> validations = service.listValidations();
        assertNotNull("validations shouldn't be null", validations);
        assertFalse("validations should'nt be empty", validations.isEmpty());
        for (ValidationDescription desc : validations) {
            assertNotNull("validations shouldn't contain null elements", desc);
            assertNotNull("validation className shouldn't be null", desc.getClassName());
            assertFalse("validation className shouldn't be empty", "".equals(desc.getClassName()));
        }
    }
    
    public void testSaveMenuItem() throws Exception {
        GuvnorMenuService service = new GuvnorMenuService();
        FormRepresentationDecoder decoder = FormEncodingFactory.getDecoder();
        File dbFile = new File(getClass().getResource("/menuItems.json").getFile());
        String jsonInitial = FileUtils.readFileToString(dbFile);
        Map<String, List<MenuItemDescription>> descsInitial = decoder.decodeMenuItemsMap(jsonInitial);
        MenuItemDescription desc = new MenuItemDescription();
        desc.setClassName(CustomMenuItem.class.getName());
        List<FormEffectDescription> effects = new ArrayList<FormEffectDescription>();
        FormEffectDescription effDesc1 = new FormEffectDescription();
        effDesc1.setClassName(RemoveEffect.class.getName());
        effects.add(effDesc1);
        FormEffectDescription effDesc2 = new FormEffectDescription();
        effDesc2.setClassName(DoneEffect.class.getName());
        effects.add(effDesc2);
        desc.setEffects(effects);
        File file = new File(getClass().getResource("testSaveMenuItem.json").getFile());
        String json = FileUtils.readFileToString(file);
        FormItemRepresentation itemRepresentation = decoder.decodeItem(json);
        desc.setName("test component");
        desc.setItemRepresentation(itemRepresentation);
        
        String groupName = "Test Components";
        service.saveMenuItem(groupName, desc);
        
        String jsonResult = FileUtils.readFileToString(dbFile);
        
        Map<String, List<MenuItemDescription>> descsResult = decoder.decodeMenuItemsMap(jsonResult);
        assertNotNull("saved menu items shouldn't be null", descsResult);
        assertNotNull("saved menu items should contain a list of " + groupName, descsResult.get(groupName));
        assertFalse(groupName + " list should not be empty", descsResult.get(groupName).isEmpty());
        assertFalse("descsInitial and descsResult should not be the same", descsInitial.equals(descsResult));
        
        service.deleteMenuItem(groupName, desc);
        
        String jsonFinal = FileUtils.readFileToString(dbFile);
        Map<String, List<MenuItemDescription>> descsFinal = decoder.decodeMenuItemsMap(jsonFinal);
        
        assertEquals("descsInitial and descsFinal should be the same", descsInitial.entrySet(), descsFinal.entrySet());
    }
    
    public void testGetFormBuilderProperties() throws Exception {
        GuvnorMenuService service = new GuvnorMenuService();
        Map<String, String> props = service.getFormBuilderProperties();
        assertNotNull("props shouldn't be null", props);
        assertFalse("props shouldn't be empty", props.isEmpty());
    }
}
