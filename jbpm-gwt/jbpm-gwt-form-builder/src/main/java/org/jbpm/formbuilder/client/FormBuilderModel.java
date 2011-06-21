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

import org.jbpm.formbuilder.client.bus.MenuOptionAddedEvent;
import org.jbpm.formbuilder.client.bus.MenuOptionAddedEventHandler;
import org.jbpm.formbuilder.client.bus.MenuOptionRemoveEvent;
import org.jbpm.formbuilder.client.bus.MenuOptionRemoveEventHandler;
import org.jbpm.formbuilder.client.bus.NotificationEvent;
import org.jbpm.formbuilder.client.bus.NotificationEvent.Level;
import org.jbpm.formbuilder.client.bus.PreviewFormRepresentationEvent;
import org.jbpm.formbuilder.client.bus.PreviewFormRepresentationEventHandler;
import org.jbpm.formbuilder.client.command.BaseCommand;
import org.jbpm.formbuilder.client.command.EditFormRedoCommand;
import org.jbpm.formbuilder.client.command.EditFormUndoCommand;
import org.jbpm.formbuilder.client.command.PreviewFormAsFtlCommand;
import org.jbpm.formbuilder.client.command.PreviewFormAsXslCommand;
import org.jbpm.formbuilder.client.command.SaveFormCommand;
import org.jbpm.formbuilder.client.effect.AddItemFormEffect;
import org.jbpm.formbuilder.client.effect.DeleteItemFormEffect;
import org.jbpm.formbuilder.client.effect.DoneEffect;
import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.effect.RemoveEffect;
import org.jbpm.formbuilder.client.effect.ResizeEffect;
import org.jbpm.formbuilder.client.effect.SaveAsMenuOptionFormEffect;
import org.jbpm.formbuilder.client.effect.VarBindingEffect;
import org.jbpm.formbuilder.client.menu.FBMenuItem;
import org.jbpm.formbuilder.client.menu.items.AbsoluteLayoutMenuItem;
import org.jbpm.formbuilder.client.menu.items.CheckBoxMenuItem;
import org.jbpm.formbuilder.client.menu.items.ComboBoxMenuItem;
import org.jbpm.formbuilder.client.menu.items.CompleteButtonMenuItem;
import org.jbpm.formbuilder.client.menu.items.ErrorMenuItem;
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
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.shared.rep.FBValidation;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;
import org.jbpm.formbuilder.shared.rep.trans.LanguageException;
import org.jbpm.formbuilder.shared.task.TaskRef;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.user.client.rpc.impl.ReflectionHelper;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

public class FormBuilderModel implements FormBuilderService {

    private final EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
    
    private final String contextPath;
    
    public FormBuilderModel(String contextPath) {
        this.contextPath = contextPath;
        bus.addHandler(MenuOptionAddedEvent.TYPE, new MenuOptionAddedEventHandler() {
            public void onEvent(MenuOptionAddedEvent event) {
                saveMenuItem(event.getGroupName(), event.getMenuItem());
            }
        });
        bus.addHandler(MenuOptionRemoveEvent.TYPE, new MenuOptionRemoveEventHandler() {
            public void onEvent(MenuOptionRemoveEvent event) {
                deleteMenuItem(event.getGroupName(), event.getMenuItem());
            }
        });
        bus.addHandler(PreviewFormRepresentationEvent.TYPE, new PreviewFormRepresentationEventHandler() {
            public void onEvent(PreviewFormRepresentationEvent event) {
                saveForm(event.getRepresentation());
            }
        });
    }
    
    public Map<String, List<FBMenuItem>> getMenuItems() {
        /* TODO The whole idea is to get menu items definitions from a server
         * so that anyone can configure it to return the JSON they desire
         * and reconfigure it to have as many permissions to do things as
         * they may want.
        
        final Map<String, List<FBMenuItem>> menuItems = new HashMap<String, List<FBMenuItem>>();
        RequestBuilder request = new RequestBuilder(RequestBuilder.GET, GWT.getModuleBaseURL() + this.contextPath + "/menuItems");
        request.setCallback(new RequestCallback() {
            public void onResponseReceived(Request request, Response response) {
                Document xml = XMLParser.parse(response.getText());
                menuItems.putAll(readMenuMap(xml));
            }

            public void onError(Request request, Throwable exception) {
                
                bus.fireEvent(new NotificationEvent(Level.ERROR, 
                    "Couldn't find menu items", exception));
            }
        });
        request.send();
        */
        Map<String, List<FBMenuItem>> map = new HashMap<String, List<FBMenuItem>>();
        List<FBMenuItem> controls = new ArrayList<FBMenuItem>();
        List<FBMenuItem> visuals = new ArrayList<FBMenuItem>();
        List<FBMenuItem> layouts = new ArrayList<FBMenuItem>();

        List<FBFormEffect> effects = new ArrayList<FBFormEffect>();
        effects.add(new RemoveEffect());
        effects.add(new DoneEffect());
        effects.add(new ResizeEffect());
        effects.add(new SaveAsMenuOptionFormEffect());
        effects.add(new VarBindingEffect());
        
        List<FBFormEffect> effectsOptions = new ArrayList<FBFormEffect>();
        effectsOptions.add(new RemoveEffect());
        effectsOptions.add(new DoneEffect());
        effectsOptions.add(new ResizeEffect());
        effectsOptions.add(new SaveAsMenuOptionFormEffect());
        effectsOptions.add(new AddItemFormEffect());
        effectsOptions.add(new DeleteItemFormEffect());
        effectsOptions.add(new VarBindingEffect());
        
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
        layouts.add(new AbsoluteLayoutMenuItem(effects));
        map.put("Layout Components", layouts);
        
        return map;
    }

    private Map<String, List<FBMenuItem>> readMenuMap(Document xml) {
        Map<String, List<FBMenuItem>> menuItems = new HashMap<String, List<FBMenuItem>>();
        NodeList groups = xml.getElementsByTagName("menuGroup");
        for (int jindex = 0; jindex < groups.getLength(); jindex++) {
            Node groupNode = groups.item(jindex);
            String groupName = ((Element) groupNode).getAttribute("name");
            NodeList items = ((Element) groupNode).getElementsByTagName("menuItem");
            menuItems.put(groupName, readMenuItems(items));
        }
        return menuItems;
    }
    
    private List<FBMenuItem> readMenuItems(NodeList items) {
        List<FBMenuItem> menuItems = new ArrayList<FBMenuItem>();
        for (int index = 0; index < items.getLength(); index ++) {
            Node itemNode = items.item(index);
            String itemClassName = ((Element) itemNode).getAttribute("className");
            try {
                Class<?> klass = ReflectionHelper.loadClass(itemClassName);
                Object obj = ReflectionHelper.newInstance(klass);
                FBMenuItem menuItem = null;
                if (obj instanceof FBMenuItem) {
                    menuItem = (FBMenuItem) obj;
                } else {
                    throw new Exception(itemClassName + " not of type FBMenuItem");
                }
                NodeList effects = ((Element) itemNode).getElementsByTagName("effect");
                for (FBFormEffect effect : readItemEffects(effects)) {
                    menuItem.addEffect(effect);
                }
                menuItems.add(menuItem);
            } catch (Exception e) {
                menuItems.add(new ErrorMenuItem(e.getLocalizedMessage()));
            }
        }
        return menuItems;
    }
    
    private List<FBFormEffect> readItemEffects(NodeList effects) throws Exception {
        List<FBFormEffect> itemEffects = new ArrayList<FBFormEffect>();
        for (int i = 0; i < effects.getLength(); i++) {
            Node effectNode = effects.item(i);
            String effectClassName = ((Element) effectNode).getAttribute("className");
            Class<?> clazz = ReflectionHelper.loadClass(effectClassName);
            Object efobj = ReflectionHelper.newInstance(clazz);
            if (efobj instanceof FBFormEffect) {
                itemEffects.add((FBFormEffect) efobj);
            } else {
                throw new Exception(effectClassName + " not a valid FBFormEffect type");
            }
        }
        return itemEffects;
    }


    public List<MainMenuOption> getMenuOptions() {
        /* TODO
        final List<MainMenuOption> currentOptions = new ArrayList<MainMenuOption>();
        RequestBuilder request = new RequestBuilder(RequestBuilder.GET, GWT.getModuleBaseURL() + this.contextPath + "/menuOptions");
        request.setCallback(new RequestCallback() {
            public void onResponseReceived(Request request, Response response) {
                Document xml = XMLParser.parse(response.getText());
                currentOptions.addAll(readMenuOptions(xml.getElementsByTagName("menuOption")));
            }

            public void onError(Request request, Throwable exception) {
                bus.fireEvent(new NotificationEvent(Level.ERROR, 
                    "Couldn't find menu options", exception));
            }
        });
        request.send();*/
        
        List<MainMenuOption> retval = new ArrayList<MainMenuOption>();
        MainMenuOption previewOption = new MainMenuOption();
        previewOption.setHtml("Preview");
        
        List<MainMenuOption> previewMenu = new ArrayList<MainMenuOption>();

        MainMenuOption previewFtl = new MainMenuOption();
        previewFtl.setHtml("As FTL");
        previewFtl.setCommand(new PreviewFormAsFtlCommand());
        
        MainMenuOption previewXsl = new MainMenuOption();
        previewXsl.setHtml("As XSL");
        previewXsl.setCommand(new PreviewFormAsXslCommand());
        
        previewMenu.add(previewFtl);
        previewMenu.add(previewXsl);
        
        previewOption.setSubMenu(previewMenu);
        
        MainMenuOption editOption = new MainMenuOption();
        editOption.setHtml("Edit");
        
        List<MainMenuOption> editMenu = new ArrayList<MainMenuOption>();
        
        MainMenuOption editUndo = new MainMenuOption();
        editUndo.setHtml("Undo");
        editUndo.setCommand(new EditFormUndoCommand());
        
        MainMenuOption editRedo = new MainMenuOption();
        editRedo.setHtml("Redo");
        editRedo.setCommand(new EditFormRedoCommand());

        editMenu.add(editUndo);
        editMenu.add(editRedo);
        
        editOption.setSubMenu(editMenu);
        
        MainMenuOption saveOption = new MainMenuOption();
        saveOption.setHtml("Save");
        saveOption.setCommand(new SaveFormCommand());
        
        retval.add(previewOption);
        retval.add(editOption);
        retval.add(saveOption);
        return retval;
    }
    
    private List<MainMenuOption> readMenuOptions(NodeList menuOptions) {
        List<MainMenuOption> options = new ArrayList<MainMenuOption>();
        for (int index = 0; index < menuOptions.getLength(); index++) {
            Node menuNode = menuOptions.item(index);
            Element menuElement = (Element) menuNode;
            String name = menuElement.getAttribute("name");
            MainMenuOption option = new MainMenuOption();
            option.setHtml(name);
            if (menuElement.hasAttribute("commandClass")) {
                String className = menuElement.getAttribute("commandClass");
                try {
                    Class<?> klass = ReflectionHelper.loadClass(className);
                    Object obj = ReflectionHelper.newInstance(klass);
                    if (obj instanceof BaseCommand) {
                        option.setCommand((BaseCommand) obj);
                    } else {
                        option.setHtml(option.getHtml()+ "(typeError: " + className + " is invalid)");
                        option.setEnabled(false);
                    }
                } catch (Exception e) {
                    option.setHtml(option.getHtml() + "(error: " + e.getLocalizedMessage() + ")");
                    option.setEnabled(false);
                }
            } else {
                option.setSubMenu(readMenuOptions(menuElement.getElementsByTagName("menuOption")));
            }
            options.add(option);
        }
        return options;
    }
    
    public void saveForm(FormRepresentation form) {
        /*RequestBuilder request = new RequestBuilder(RequestBuilder.POST, GWT.getModuleBaseURL() + this.contextPath + "/menuItems");
        request.setCallback(new RequestCallback() {
            public void onResponseReceived(Request request, Response response) {
                Document xml = XMLParser.parse(response.getText());
                //TODO parse the form id and set it for future updating
            }

            public void onError(Request request, Throwable exception) {
                bus.fireEvent(new NotificationEvent(Level.ERROR, 
                    "Couldn't save form", exception));
            }
        });        
        try {
            request.setRequestData(form.translate("xml"));
            request.send();
        } catch (LanguageException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, "Couldn't generate form", e));
        } catch (RequestException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, "Couldn't send form to server", e));
        }*/
    }
    
    public String generateForm(FormRepresentation form, String language) {
        //TODO send request and wait for publishing
        String url = GWT.getModuleBaseURL() + this.contextPath + "/formPreview/" + form.getTaskId() + "/" + form.getLastModified();
        return url;
    }
    
    public void saveMenuItem(String groupName, final FBMenuItem item) {
        RequestBuilder request = new RequestBuilder(RequestBuilder.POST, GWT.getModuleBaseURL() + this.contextPath + "/menuItems");
        request.setCallback(new RequestCallback() {
            public void onResponseReceived(Request request, Response response) {
                int code = response.getStatusCode();
                if (code == Response.SC_CREATED) {
                    Document xml = XMLParser.parse(response.getText());
                    NodeList xmlItems = xml.getElementsByTagName("menuItem");
                    List<FBMenuItem> myItems = readMenuItems(xmlItems);
                    FBMenuItem myItem = myItems.get(0);
                    item.setItemId(myItem.getItemId());
                }
            }

            public void onError(Request request, Throwable exception) {
                bus.fireEvent(new NotificationEvent(Level.ERROR, "Couldn't generate menu item", exception));
            }
        });
        try {
            request.setRequestData(asXml(item));
            request.send();
        } catch (RequestException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, "Couldn't save menu item", e));
        }
    }
    
    public void deleteMenuItem(String groupName, FBMenuItem item) {
        //TODO this method should send a body
        RequestBuilder request = new RequestBuilder(RequestBuilder.DELETE, GWT.getModuleBaseURL() + this.contextPath + "/menuItems/" + item.getItemId());
        request.setCallback(new RequestCallback() {
            public void onResponseReceived(Request request, Response response) {
                int code = response.getStatusCode();
                if (code != Response.SC_ACCEPTED && code != Response.SC_NO_CONTENT && code != Response.SC_OK) {
                    bus.fireEvent(new NotificationEvent(Level.WARN, "Error deleting menu item on server (code = " + code + ")"));
                }
            }

            public void onError(Request request, Throwable exception) {
                bus.fireEvent(new NotificationEvent(Level.ERROR, "Error deleting menu item on server", exception));
            }
        });
        try {
            request.send();
        } catch (RequestException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, "Error deleting menu item on server", e));
        }
    }
    
    private String asXml(FBMenuItem item) {
        StringBuilder builder = new StringBuilder();
        builder.append("<menuItem>");
        builder.append("<itemId>").append(item.getItemId()).append("</itemId>");
        builder.append("<name>").append(item.getDescription().getText()).append("</name>");
        try {
            builder.append("<clone>").
                append(item.buildWidget().getRepresentation().translate("xml")).
                append("</clone>");
        } catch (LanguageException e) {
            builder.append("<clone>Exception:").append(e.getMessage()).append("</clone>");
        }
        builder.append("</menuItem>");
        return builder.toString();
    }
    
    public List<TaskRef> getExistingTasks(String filter) { //TODO actual implementation not done
        List<TaskRef> retval = new ArrayList<TaskRef>();
        TaskRef task1 = new TaskRef();
        task1.setTaskId("task1");
        task1.addInput("input1", "${hey}");
        task1.addInput("input2", "${why}");
        task1.addOutput("output1", "");
        task1.addOutput("output2", "");
        retval.add(task1);
        TaskRef task2 = new TaskRef();
        task2.addInput("input3", "${hey}");
        task2.addInput("input4", "${why}");
        task2.addOutput("output3", "");
        task2.addOutput("output4", "");
        retval.add(task2);
        return retval;
    }
    
    public List<FBValidation> getExistingValidations() throws FormBuilderException {
        // TODO implement
        return null;
    }

    public void updateTask(TaskRef task) throws FormBuilderException {
        //TODO implement
    }
}
