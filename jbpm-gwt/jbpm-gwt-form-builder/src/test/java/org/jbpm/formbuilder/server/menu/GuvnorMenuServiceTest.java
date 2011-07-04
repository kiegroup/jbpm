package org.jbpm.formbuilder.server.menu;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.jbpm.formbuilder.client.effect.DoneEffect;
import org.jbpm.formbuilder.client.effect.RemoveEffect;
import org.jbpm.formbuilder.client.menu.items.CustomOptionMenuItem;
import org.jbpm.formbuilder.server.form.FormEncodingServerFactory;
import org.jbpm.formbuilder.shared.form.FormRepresentationDecoder;
import org.jbpm.formbuilder.shared.menu.FormEffectDescription;
import org.jbpm.formbuilder.shared.menu.MenuItemDescription;
import org.jbpm.formbuilder.shared.menu.MenuOptionDescription;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;

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
    
    public void testSaveMenuItem() throws Exception {
        GuvnorMenuService service = new GuvnorMenuService();
        FormRepresentationDecoder decoder = FormEncodingServerFactory.getDecoder();
        File dbFile = new File(getClass().getResource("/menuItems.json").getFile());
        String jsonInitial = FileUtils.readFileToString(dbFile);
        Map<String, List<MenuItemDescription>> descsInitial = decoder.decodeMenuItemsMap(jsonInitial);
        MenuItemDescription desc = new MenuItemDescription();
        desc.setClassName(CustomOptionMenuItem.class.getName());
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
        service.save(groupName, desc);
        
        String jsonResult = FileUtils.readFileToString(dbFile);
        
        Map<String, List<MenuItemDescription>> descsResult = decoder.decodeMenuItemsMap(jsonResult);
        assertNotNull("saved menu items shouldn't be null", descsResult);
        assertNotNull("saved menu items should contain a list of " + groupName, descsResult.get(groupName));
        assertFalse(groupName + " list should not be empty", descsResult.get(groupName).isEmpty());
        assertFalse("descsInitial and descsResult should not be the same", descsInitial.equals(descsResult));
        
        service.delete(groupName, desc);
        
        String jsonFinal = FileUtils.readFileToString(dbFile);
        Map<String, List<MenuItemDescription>> descsFinal = decoder.decodeMenuItemsMap(jsonFinal);
        
        assertEquals("descsInitial and descsFinal should be the same", descsInitial.entrySet(), descsFinal.entrySet());
    }
}
