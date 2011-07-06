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

import org.jbpm.formbuilder.client.bus.MenuItemAddedEvent;
import org.jbpm.formbuilder.client.bus.MenuItemAddedEventHandler;
import org.jbpm.formbuilder.client.bus.MenuItemFromServerEvent;
import org.jbpm.formbuilder.client.bus.MenuItemRemoveEvent;
import org.jbpm.formbuilder.client.bus.MenuItemRemoveEventHandler;
import org.jbpm.formbuilder.client.bus.MenuOptionAddedEvent;
import org.jbpm.formbuilder.client.bus.NotificationEvent;
import org.jbpm.formbuilder.client.bus.NotificationEvent.Level;
import org.jbpm.formbuilder.client.bus.PreviewFormRepresentationEvent;
import org.jbpm.formbuilder.client.bus.PreviewFormRepresentationEventHandler;
import org.jbpm.formbuilder.client.command.BaseCommand;
import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.form.FormEncodingClientFactory;
import org.jbpm.formbuilder.client.menu.FBMenuItem;
import org.jbpm.formbuilder.client.menu.items.CustomOptionMenuItem;
import org.jbpm.formbuilder.client.menu.items.ErrorMenuItem;
import org.jbpm.formbuilder.client.options.MainMenuOption;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.client.validation.FBValidationItem;
import org.jbpm.formbuilder.client.validation.NotEmptyValidationItem;
import org.jbpm.formbuilder.shared.form.FormEncodingException;
import org.jbpm.formbuilder.shared.form.FormRepresentationDecoder;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;
import org.jbpm.formbuilder.shared.task.TaskRef;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
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
        bus.addHandler(MenuItemAddedEvent.TYPE, new MenuItemAddedEventHandler() {
            public void onEvent(MenuItemAddedEvent event) {
                saveMenuItem(event.getGroupName(), event.getMenuItem());
            }
        });
        bus.addHandler(MenuItemRemoveEvent.TYPE, new MenuItemRemoveEventHandler() {
            public void onEvent(MenuItemRemoveEvent event) {
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
        final Map<String, List<FBMenuItem>> menuItems = new HashMap<String, List<FBMenuItem>>();
        RequestBuilder request = new RequestBuilder(RequestBuilder.GET, GWT.getModuleBaseURL() + this.contextPath + "/menuItems/");
        request.setCallback(new RequestCallback() {
            public void onResponseReceived(Request request, Response response) {
            	if (response.getStatusCode() == Response.SC_OK) {
            		Document xml = XMLParser.parse(response.getText());
            		menuItems.putAll(readMenuMap(xml));
            		for (String groupName : menuItems.keySet()) {
            			for (FBMenuItem menuItem : menuItems.get(groupName)) {
            				bus.fireEvent(new MenuItemFromServerEvent(menuItem, groupName));
            			}
            		}
            	} else {
            		bus.fireEvent(new NotificationEvent(Level.ERROR, "Couldn't find menu items: response status 404"));
            	}
            }
            public void onError(Request request, Throwable exception) {
                bus.fireEvent(new NotificationEvent(Level.ERROR, "Couldn't find menu items", exception));
            }
        });
        try {
            request.send();
        } catch (RequestException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, "Couldn't read menuItems", e));
        }
        return menuItems;
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
                if (obj instanceof CustomOptionMenuItem) {
                    CustomOptionMenuItem customItem = (CustomOptionMenuItem) obj;
                    String optionName = ((Element) itemNode).getAttribute("optionName");
                    FBFormItem cloneableItem = FBFormItem.createItem(makeRepresentation(itemNode));
                    customItem.setCloneableItem(cloneableItem);
                    customItem.setNewMenuOptionName(optionName);
                    menuItem = customItem;
                } else if (obj instanceof FBMenuItem) {
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
    
    private FormItemRepresentation makeRepresentation(Node itemNode) {
        NodeList list = ((Element) itemNode).getElementsByTagName("itemJson");
        FormItemRepresentation rep = null;
        if (list.getLength() > 0) {
            Node node = list.item(0);
            String json = node.getFirstChild().getNodeValue();
            FormRepresentationDecoder decoder = FormEncodingClientFactory.getDecoder();
            try {
                rep = (FormItemRepresentation) decoder.decodeItem(json);
            } catch (FormEncodingException e) {
                bus.fireEvent(new NotificationEvent(Level.ERROR, "Couldn't load form representation from server", e));
            }
        }
        return rep;
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
        final List<MainMenuOption> currentOptions = new ArrayList<MainMenuOption>();
        RequestBuilder request = new RequestBuilder(RequestBuilder.GET, GWT.getModuleBaseURL() + this.contextPath + "/menuOptions/");
        request.setCallback(new RequestCallback() {
            public void onResponseReceived(Request request, Response response) {
                Document xml = XMLParser.parse(response.getText());
                currentOptions.addAll(readMenuOptions(xml.getElementsByTagName("menuOptions").item(0).getChildNodes()));
                for (MainMenuOption option : currentOptions) {
                    bus.fireEvent(new MenuOptionAddedEvent(option));
                }
            }

            public void onError(Request request, Throwable exception) {
                bus.fireEvent(new NotificationEvent(Level.ERROR, 
                    "Couldn't find menu options", exception));
            }
        });
        try {
            request.send();
        } catch (RequestException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, "Couldn't read menuOptions", e));
        }
        return currentOptions;
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
                option.setSubMenu(readMenuOptions(menuElement.getChildNodes()));
            }
            options.add(option);
        }
        return options;
    }
    
    public void saveForm(FormRepresentation form) {
        RequestBuilder request = new RequestBuilder(RequestBuilder.POST, GWT.getModuleBaseURL() + this.contextPath + "/package/defaultPackage/forms/");
        request.setCallback(new RequestCallback() {
            public void onResponseReceived(Request request, Response response) {
                if (response.getStatusCode() == Response.SC_CONFLICT) {
                    bus.fireEvent(new NotificationEvent(Level.WARN, "Form already updated in server. Try refreshing your form"));
                } else if (response.getStatusCode() != Response.SC_CREATED) {
                    bus.fireEvent(new NotificationEvent(Level.WARN, "Unkown status for saveForm(...): " + response.getStatusCode()));
                }
            }
            public void onError(Request request, Throwable exception) {
                bus.fireEvent(new NotificationEvent(Level.ERROR, "Couldn't save form", exception));
            }
        });
        try {
            String json = FormEncodingClientFactory.getEncoder().encode(form);
            request.setRequestData(json);
            request.send();
        } catch (RequestException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, "Couldn't send form to server", e));
        } catch (FormEncodingException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, "Couldn't decode form", e));
        }
    }
    
    public String generateForm(FormRepresentation form, String language) {
        saveForm(form);
        return new StringBuilder(GWT.getModuleBaseURL()).append(this.contextPath).
                append("/formPreview/").append(form.getTaskId()).append("/lang/").
                append(language).toString();
    }
    
    public void saveMenuItem(final String groupName, final FBMenuItem item) {
        RequestBuilder request = new RequestBuilder(RequestBuilder.POST, 
                GWT.getModuleBaseURL() + this.contextPath + "/defaultPackage/menuItems/" + groupName + "/");
        request.setCallback(new RequestCallback() {
            public void onResponseReceived(Request request, Response response) {
                int code = response.getStatusCode();
                if (code == Response.SC_CREATED) {
                    Document xml = XMLParser.parse(response.getText());
                    NodeList xmlItems = xml.getElementsByTagName("menuItemId");
                    String menuItemId = xmlItems.item(0).getNodeValue();
                    bus.fireEvent(new NotificationEvent(Level.INFO, "Menu item " + menuItemId + " saved successfully."));
                }
            }

            public void onError(Request request, Throwable exception) {
                bus.fireEvent(new NotificationEvent(Level.ERROR, "Couldn't generate menu item", exception));
            }
        });
        try {
            request.setRequestData(asXml(groupName, item));
            request.send();
        } catch (RequestException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, "Couldn't save menu item", e));
        }
    }
    
    public void deleteMenuItem(String groupName, FBMenuItem item) {
        RequestBuilder request = new RequestBuilder(RequestBuilder.DELETE, 
                GWT.getModuleBaseURL() + this.contextPath + "/defaultPackage/menuItems/");
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
            request.setRequestData(asXml(groupName, item));
            request.send();
        } catch (RequestException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, "Error deleting menu item on server", e));
        }
    }
    
    private String asXml(String groupName, FBMenuItem item) {
        StringBuilder builder = new StringBuilder();
        builder.append("<menuItem>");
        builder.append("<groupName>").append(groupName).append("</groupName>");
        builder.append("<name>").append(item.getDescription().getText()).append("</name>");
        try {
            String json = FormEncodingClientFactory.getEncoder().encode(item.buildWidget().getRepresentation());
            String jsonTag = new StringBuilder("<clone><![CDATA[").append(json).append("]]></clone>").toString();
            builder.append(jsonTag);
        } catch (FormEncodingException e) {
            builder.append("<clone error=\"true\">Exception:").append(e.getMessage()).append("</clone>");
        }
        for (FBFormEffect effect : item.getFormEffects()) {
            builder.append("<effect className=\"").append(effect.getClass().getName()).append("\" />");
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
    
    public List<FBValidationItem> getExistingValidations() throws FormBuilderException {
        // TODO actual implementation not done 
        List<FBValidationItem> retval = new ArrayList<FBValidationItem>();
        retval.add(new NotEmptyValidationItem());
        return retval;
    }

    public void updateTask(TaskRef task) throws FormBuilderException {
        //TODO implement
    }
}
