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
import java.util.List;

import org.jbpm.formbuilder.client.effect.AddItemFormEffect;
import org.jbpm.formbuilder.client.effect.DoneEffect;
import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.effect.RemoveEffect;
import org.jbpm.formbuilder.client.menu.ComboBoxMenuItem;
import org.jbpm.formbuilder.client.menu.CompleteButtonMenuItem;
import org.jbpm.formbuilder.client.menu.FBMenuItem;
import org.jbpm.formbuilder.client.menu.HeaderMenuItem;
import org.jbpm.formbuilder.client.menu.HorizontalLayoutMenuItem;
import org.jbpm.formbuilder.client.menu.LabelMenuItem;
import org.jbpm.formbuilder.client.menu.PasswordFieldMenuItem;
import org.jbpm.formbuilder.client.menu.TableLayoutMenuItem;
import org.jbpm.formbuilder.client.menu.TextFieldMenuItem;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;

public class FormBuilderModel {

    public List<FBMenuItem> getMenuItems() {
        List<FBMenuItem> list = new ArrayList<FBMenuItem>();
        /*final List<String> classNames = new ArrayList<String>();
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
                // TODO Auto-generated method stub
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
                
                //TODO Auto-generated method stub
            }
        }*/
        List<FBFormEffect> effects = new ArrayList<FBFormEffect>();
        effects.add(new RemoveEffect());
        effects.add(new DoneEffect());
        
        List<FBFormEffect> effectsOptions = new ArrayList<FBFormEffect>();
        effectsOptions.add(new RemoveEffect());
        effectsOptions.add(new DoneEffect());
        effectsOptions.add(new AddItemFormEffect());
        
        list.add(new HeaderMenuItem(effects));
        list.add(new LabelMenuItem(effects));
        list.add(new ComboBoxMenuItem(effectsOptions));
        list.add(new TextFieldMenuItem(effects));
        list.add(new PasswordFieldMenuItem(effects));
        list.add(new CompleteButtonMenuItem(effects));
        list.add(new HorizontalLayoutMenuItem(effects));
        list.add(new TableLayoutMenuItem(effects));
        
        return list;
    }

    public List<MainMenuOption> getCurrentOptions() {
        // TODO Auto-generated method stub
        List<MainMenuOption> retval = new ArrayList<MainMenuOption>();
        MainMenuOption saveOption = new MainMenuOption();
        saveOption.setHtml("Save");
        saveOption.setCommand(new Command() {
            public void execute() {
                Window.alert("HERE BE DRAGONS");
            }
        });
        retval.add(saveOption);
        return retval;
    }
}
