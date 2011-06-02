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
package org.jbpm.formbuilder.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.command.SaveFormAsFtlCommand;
import org.jbpm.formbuilder.client.command.SaveFormAsXslCommand;
import org.jbpm.formbuilder.client.effect.AddItemFormEffect;
import org.jbpm.formbuilder.client.effect.DeleteItemFormEffect;
import org.jbpm.formbuilder.client.effect.DoneEffect;
import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.effect.RemoveEffect;
import org.jbpm.formbuilder.client.effect.SaveAsMenuOptionFormEffect;
import org.jbpm.formbuilder.client.menu.FBMenuItem;
import org.jbpm.formbuilder.client.menu.items.CheckBoxMenuItem;
import org.jbpm.formbuilder.client.menu.items.ComboBoxMenuItem;
import org.jbpm.formbuilder.client.menu.items.CompleteButtonMenuItem;
import org.jbpm.formbuilder.client.menu.items.FileInputMenuItem;
import org.jbpm.formbuilder.client.menu.items.HTMLMenuItem;
import org.jbpm.formbuilder.client.menu.items.HeaderMenuItem;
import org.jbpm.formbuilder.client.menu.items.HiddenMenuItem;
import org.jbpm.formbuilder.client.menu.items.HorizontalLayoutMenuItem;
import org.jbpm.formbuilder.client.menu.items.ImageMenuItem;
import org.jbpm.formbuilder.client.menu.items.LabelMenuItem;
import org.jbpm.formbuilder.client.menu.items.PasswordFieldMenuItem;
import org.jbpm.formbuilder.client.menu.items.RadioButtonMenuItem;
import org.jbpm.formbuilder.client.menu.items.TableLayoutMenuItem;
import org.jbpm.formbuilder.client.menu.items.TextAreaMenuItem;
import org.jbpm.formbuilder.client.menu.items.TextFieldMenuItem;
import org.jbpm.formbuilder.client.options.MainMenuOption;

public class FormBuilderModel {

    public Map<String, List<FBMenuItem>> getMenuItems() {
        /* TODO The whole idea is to get menu items definitions from a server
         * so that anyone can configure it to return the JSON they desire
         * and reconfigure it to have as many permissions to do things as
         * they may want.
         
        final List<String> classNames = new ArrayList<String>();
        RequestBuilder request = new RequestBuilder(RequestBuilder.GET, GWT.getModuleBaseURL() + "api/menuItems");
        request.setCallback(new RequestCallback() {
            public void onResponseReceived(Request request, Response response) {
                String json = response.getText();
                JSONValue value = JSONParser.parseLenient(json);
                if (value.isArray() != null) {
                    JSONArray array = value.isArray();
                    int size = array.size();
                    for (int i = 0; i < size; i++) {
                        classNames.add(array.get(i).isString().stringValue());
                    }
                } else {
                    classNames.add(value.isString().stringValue());
                }
            }
            
            public void onError(Request request, Throwable exception) {
                Window.alert("Couldn't find menu items");
            }
        });
        for (String className : classNames) {
            try {
                Class<?> kclass = ReflectionHelper.loadClass(className);
                Object obj = ReflectionHelper.newInstance(kclass);
                if (obj instanceof FBMenuItem) {
                    list.add((FBMenuItem) obj);
                } else {
                    list.add(new ErrorMenuItem(className + " not of type FBMenuItem"));
                }
            } catch (Exception e) {
                list.add(new ErrorMenuItem("Couldn't instantiate " + className);
            }
        }
        */
        Map<String, List<FBMenuItem>> map = new HashMap<String, List<FBMenuItem>>();
        List<FBMenuItem> controls = new ArrayList<FBMenuItem>();
        List<FBMenuItem> visuals = new ArrayList<FBMenuItem>();
        List<FBMenuItem> layouts = new ArrayList<FBMenuItem>();

        List<FBFormEffect> effects = new ArrayList<FBFormEffect>();
        effects.add(new RemoveEffect());
        effects.add(new DoneEffect());
        effects.add(new SaveAsMenuOptionFormEffect());
        
        List<FBFormEffect> effectsOptions = new ArrayList<FBFormEffect>();
        effectsOptions.add(new RemoveEffect());
        effectsOptions.add(new DoneEffect());
        effectsOptions.add(new SaveAsMenuOptionFormEffect());
        effectsOptions.add(new AddItemFormEffect());
        effectsOptions.add(new DeleteItemFormEffect());
        
        visuals.add(new HeaderMenuItem(effects));
        visuals.add(new LabelMenuItem(effects));
        visuals.add(new ImageMenuItem(effects));
        visuals.add(new HTMLMenuItem(effects));
        map.put("Visual Components", visuals);
        
        controls.add(new ComboBoxMenuItem(effectsOptions));
        controls.add(new TextFieldMenuItem(effects));
        controls.add(new PasswordFieldMenuItem(effects));
        controls.add(new CompleteButtonMenuItem(effects));
        controls.add(new TextAreaMenuItem(effects));
        controls.add(new HiddenMenuItem(effects));
        controls.add(new FileInputMenuItem(effects));
        controls.add(new CheckBoxMenuItem(effects));
        controls.add(new RadioButtonMenuItem(effects));
        map.put("Control Components", controls);
        
        layouts.add(new HorizontalLayoutMenuItem(effects));
        layouts.add(new TableLayoutMenuItem(effects));
        map.put("Layout Components", layouts);
        
        return map;
    }

    public List<MainMenuOption> getCurrentOptions() {
        List<MainMenuOption> retval = new ArrayList<MainMenuOption>();
        MainMenuOption saveOption = new MainMenuOption();
        saveOption.setHtml("Save");
        
        List<MainMenuOption> saveMenu = new ArrayList<MainMenuOption>();

        MainMenuOption saveFtl = new MainMenuOption();
        saveFtl.setHtml("As FTL");
        saveFtl.setCommand(new SaveFormAsFtlCommand());
        
        MainMenuOption saveXsl = new MainMenuOption();
        saveXsl.setHtml("As XSL");
        saveXsl.setCommand(new SaveFormAsXslCommand());
        
        saveMenu.add(saveFtl);
        saveMenu.add(saveXsl);
        
        saveOption.setSubMenu(saveMenu);
        retval.add(saveOption);
        return retval;
    }
}
