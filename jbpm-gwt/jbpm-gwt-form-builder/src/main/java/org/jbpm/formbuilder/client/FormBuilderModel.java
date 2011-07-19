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

import org.jbpm.formbuilder.client.bus.ExistingTasksResponseEvent;
import org.jbpm.formbuilder.client.bus.LoadServerFormResponseEvent;
import org.jbpm.formbuilder.client.bus.MenuItemAddedEvent;
import org.jbpm.formbuilder.client.bus.MenuItemAddedHandler;
import org.jbpm.formbuilder.client.bus.MenuItemFromServerEvent;
import org.jbpm.formbuilder.client.bus.MenuItemRemoveEvent;
import org.jbpm.formbuilder.client.bus.MenuItemRemoveHandler;
import org.jbpm.formbuilder.client.bus.MenuOptionAddedEvent;
import org.jbpm.formbuilder.client.bus.PreviewFormResponseEvent;
import org.jbpm.formbuilder.client.bus.ui.FormSavedEvent;
import org.jbpm.formbuilder.client.bus.ui.NotificationEvent;
import org.jbpm.formbuilder.client.bus.ui.NotificationEvent.Level;
import org.jbpm.formbuilder.client.menu.FBMenuItem;
import org.jbpm.formbuilder.client.menu.items.CustomMenuItem;
import org.jbpm.formbuilder.client.options.MainMenuOption;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.client.validation.FBValidationItem;
import org.jbpm.formbuilder.client.validation.NotEmptyValidationItem;
import org.jbpm.formbuilder.shared.form.FormEncodingException;
import org.jbpm.formbuilder.shared.form.FormEncodingFactory;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;
import org.jbpm.formbuilder.shared.rep.RepresentationFactory;
import org.jbpm.formbuilder.shared.task.TaskRef;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.RootPanel;

public class FormBuilderModel implements FormBuilderService {

    private final EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
    
    private final String contextPath;
    private final XmlParseHelper helper = new XmlParseHelper();
    
    public FormBuilderModel(String contextPath) {
        this.contextPath = contextPath;
        //registered to save the menu items
        bus.addHandler(MenuItemAddedEvent.TYPE, new MenuItemAddedHandler() {
            public void onEvent(MenuItemAddedEvent event) {
                FBMenuItem item = event.getMenuItem();
                saveMenuItem(event.getGroupName(), item);
                if (item instanceof CustomMenuItem) {
                    CustomMenuItem customItem = (CustomMenuItem) item;
                    String formItemName = customItem.getOptionName();
                    FormItemRepresentation formItem = customItem.getRepresentation();
                    saveFormItem(formItem, formItemName);
                }
            }
        });
        //registered to delete the menu items
        bus.addHandler(MenuItemRemoveEvent.TYPE, new MenuItemRemoveHandler() {
            public void onEvent(MenuItemRemoveEvent event) {
                FBMenuItem item = event.getMenuItem();
                deleteMenuItem(event.getGroupName(), item);
                if (item instanceof CustomMenuItem) {
                    CustomMenuItem customItem = (CustomMenuItem) item;
                    String formItemName = customItem.getOptionName();
                    FormItemRepresentation formItem = customItem.getRepresentation();
                    deleteFormItem(formItemName, formItem);
                }
            }
        });
    }
    
    public FormRepresentation getForm(final String formName) throws FormBuilderException {
        final String myFormName;
        if (!formName.startsWith("formDefinition_")) { 
            myFormName = "formDefinition_" + formName;
        } else {
            myFormName = formName;
        }
        String url = new StringBuilder(GWT.getModuleBaseURL()).append(this.contextPath).
            append("/formDefinitions/package/defaultPackage/formDefinitionId/").
            append(myFormName).append("/").toString();
        RequestBuilder request = new RequestBuilder(RequestBuilder.GET, url);
        final List<FormRepresentation> list = new ArrayList<FormRepresentation>();
        request.setCallback(new RequestCallback() {
            public void onResponseReceived(Request request, Response response) {
                if (response.getStatusCode() == Response.SC_OK) {
                    list.addAll(helper.readForms(response.getText()));
                    bus.fireEvent(new LoadServerFormResponseEvent(list.isEmpty() ? null : list.iterator().next()));
                } else {
                    bus.fireEvent(new NotificationEvent(Level.ERROR, "Couldn't find form " + formName + ": response status 404"));
                }
            }
            
            public void onError(Request request, Throwable exception) {
                bus.fireEvent(new NotificationEvent(Level.ERROR, "Couldn't find form " + formName, exception));
            }
        });
        try {
            request.send();
        } catch (RequestException e) {
            throw new FormBuilderException(e);
        }
        return list.isEmpty() ? null : list.iterator().next();
    }

    public List<FormRepresentation> getForms() throws FormBuilderException {
        String url = new StringBuilder(GWT.getModuleBaseURL()).append(this.contextPath).
            append("/formDefinitions/package/defaultPackage/").toString();
        RequestBuilder request = new RequestBuilder(RequestBuilder.GET, url);
        final List<FormRepresentation> list = new ArrayList<FormRepresentation>();
        request.setCallback(new RequestCallback() {
            public void onResponseReceived(Request request, Response response) {
                if (response.getStatusCode() == Response.SC_OK) {
                    list.addAll(helper.readForms(response.getText()));
                    bus.fireEvent(new LoadServerFormResponseEvent(list));
                } else {
                    bus.fireEvent(new NotificationEvent(Level.ERROR, "Couldn't find forms: response status 404"));
                }
            }
            public void onError(Request request, Throwable exception) {
                bus.fireEvent(new NotificationEvent(Level.ERROR, "Couldn't find forms", exception));
            }
        });
        try {
            request.send();
        } catch (RequestException e) {
            throw new FormBuilderException(e);
        }
        return list;
    }
    
    public void populateRepresentationFactory() throws FormBuilderException {
        String url = GWT.getModuleBaseURL() + this.contextPath + "/representationMappings/";
        RequestBuilder request = new RequestBuilder(RequestBuilder.GET, url);
        request.setCallback(new RequestCallback() {
            public void onResponseReceived(Request request, Response response) {
                Map<String, String> repMap = helper.readPropertyMap(response.getText());
                for (Map.Entry<String, String> entry : repMap.entrySet()) {
                    RepresentationFactory.registerItemClassName(entry.getKey(), entry.getValue());
                }
            }
            public void onError(Request request, Throwable exception) {
                bus.fireEvent(new NotificationEvent(Level.ERROR, "Couldn't read representation mappings", exception));
            }
        });
        try {
            request.send();
        } catch (RequestException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, "Couldn't read representation mappings", e));
        }
    }
    
    public Map<String, List<FBMenuItem>> getMenuItems() {
        final Map<String, List<FBMenuItem>> menuItems = new HashMap<String, List<FBMenuItem>>();
        RequestBuilder request = new RequestBuilder(RequestBuilder.GET, GWT.getModuleBaseURL() + this.contextPath + "/menuItems/");
        request.setCallback(new RequestCallback() {
            public void onResponseReceived(Request request, Response response) {
            	if (response.getStatusCode() == Response.SC_OK) {
            		menuItems.putAll(helper.readMenuMap(response.getText()));
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

    public List<MainMenuOption> getMenuOptions() {
        final List<MainMenuOption> currentOptions = new ArrayList<MainMenuOption>();
        RequestBuilder request = new RequestBuilder(RequestBuilder.GET, GWT.getModuleBaseURL() + this.contextPath + "/menuOptions/");
        request.setCallback(new RequestCallback() {
            public void onResponseReceived(Request request, Response response) {
                currentOptions.addAll(helper.readMenuOptions(response.getText()));
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

    public void saveFormItem(final FormItemRepresentation formItem, String formItemName) {
        RequestBuilder request = new RequestBuilder(RequestBuilder.POST, 
                GWT.getModuleBaseURL() + this.contextPath + "/formItems/package/defaultPackage/");
        request.setCallback(new RequestCallback() {
            public void onResponseReceived(Request request, Response response) {
                if (response.getStatusCode() == Response.SC_CONFLICT) {
                    bus.fireEvent(new NotificationEvent(Level.WARN, "formItem already updated in server. Try refreshing your view"));
                } else if (response.getStatusCode() != Response.SC_CREATED) {
                    bus.fireEvent(new NotificationEvent(Level.WARN, "Unkown status for saveFormItem: HTTP " + response.getStatusCode()));
                } else {
                    String name = helper.getFormItemId(response.getText());
                    bus.fireEvent(new NotificationEvent(Level.INFO, "Form item " + name + " saved successfully"));
                }
            }
            public void onError(Request request, Throwable exception) {
                bus.fireEvent(new NotificationEvent(Level.ERROR, "Couldn't save form item", exception));
            }
        });
        try {
            request.setRequestData(helper.asXml(formItemName, formItem));
            request.send();
        } catch (RequestException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, "Couldn't send form item " + formItemName + " to server", e));
        } catch (FormEncodingException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, "Couldn't decode form item " + formItemName, e));
        }
    }
    
    public void saveForm(final FormRepresentation form) {
        RequestBuilder request = new RequestBuilder(RequestBuilder.POST, 
                GWT.getModuleBaseURL() + this.contextPath + 
                "/formDefinitions/package/defaultPackage/");
        request.setCallback(new RequestCallback() {
            public void onResponseReceived(Request request, Response response) {
                if (response.getStatusCode() == Response.SC_CONFLICT) {
                    bus.fireEvent(new NotificationEvent(Level.WARN, "Form already updated in server. Try refreshing your form"));
                } else if (response.getStatusCode() != Response.SC_CREATED) {
                    bus.fireEvent(new NotificationEvent(Level.WARN, "Unkown status for saveForm: HTTP " + response.getStatusCode()));
                } else {
                    String name = helper.getFormId(response.getText());
                    form.setLastModified(System.currentTimeMillis());
                    form.setSaved(true);
                    form.setName(name);
                    bus.fireEvent(new FormSavedEvent(form));
                }
            }
            public void onError(Request request, Throwable exception) {
                bus.fireEvent(new NotificationEvent(Level.ERROR, "Couldn't save form", exception));
            }
        });
        try {
            String json = FormEncodingFactory.getEncoder().encode(form);
            request.setRequestData(json);
            request.send();
        } catch (RequestException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, "Couldn't send form to server", e));
        } catch (FormEncodingException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, "Couldn't decode form", e));
        }
    }
    
    public void generateForm(FormRepresentation form, String language, Map<String, Object> inputs) {
        RequestBuilder request = new RequestBuilder(RequestBuilder.POST, 
                GWT.getModuleBaseURL() + this.contextPath + 
                "/formPreview/lang/" + language);
        request.setCallback(new RequestCallback() {
            public void onResponseReceived(Request request, Response response) {
                String html = response.getText();
                bus.fireEvent(new PreviewFormResponseEvent(html));
            }
            public void onError(Request request, Throwable exception) {
                bus.fireEvent(new NotificationEvent(Level.ERROR, "Couldn't preview form", exception));
            }
        });
        try {
            request.setRequestData(helper.asXml(form, inputs));
            request.send();
        } catch (RequestException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, "Couldn't send form to server", e));
        } catch (FormEncodingException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, "Couldn't decode form", e));
        }
    }
    
    public void loadFormTemplate(final FormRepresentation form, final String language) {
        RequestBuilder request = new RequestBuilder(RequestBuilder.POST,
                GWT.getModuleBaseURL() + this.contextPath +
                "/formTemplate/lang/" + language);
        request.setCallback(new RequestCallback() {
            public void onResponseReceived(Request request, Response response) {
                String fileName = helper.getFileName(response.getText()); //TODO response should return fileName, not file
                FormPanel auxiliarForm = new FormPanel();
                auxiliarForm.setMethod("get");
                auxiliarForm.setAction(GWT.getModuleBaseURL() + contextPath + "/formTemplate/lang/" + language);
                Hidden hidden1 = new Hidden("fileName");
                hidden1.setValue(fileName);
                Hidden hidden2 = new Hidden("formName");
                hidden2.setValue(form.getName());
                auxiliarForm.add(hidden1);
                auxiliarForm.add(hidden2);
                RootPanel.get().add(auxiliarForm);
                auxiliarForm.submit();
            }
            public void onError(Request request, Throwable exception) {
                bus.fireEvent(new NotificationEvent(Level.ERROR, "Couldn't export template", exception));
            }
        });
        try {
            request.setRequestData(helper.asXml(form, null));
            request.send();
        } catch (RequestException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, "Couldn't send form to server", e));
        } catch (FormEncodingException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, "Couldn't decode form", e));
        }
    }
    
    public void saveMenuItem(final String groupName, final FBMenuItem item) {
        RequestBuilder request = new RequestBuilder(RequestBuilder.POST, 
                GWT.getModuleBaseURL() + this.contextPath + "/defaultPackage/menuItems/" + groupName + "/");
        request.setCallback(new RequestCallback() {
            public void onResponseReceived(Request request, Response response) {
                int code = response.getStatusCode();
                NotificationEvent event;
                if (code == Response.SC_CREATED) {
                    event = new NotificationEvent(Level.INFO, "Menu item " + item.getItemId() + " saved successfully.");
                } else {
                    event = new NotificationEvent(Level.WARN, "Invalid status for saveMenuItem: HTTP " + code);
                }
                bus.fireEvent(event);
            }

            public void onError(Request request, Throwable exception) {
                bus.fireEvent(new NotificationEvent(Level.ERROR, "Couldn't generate menu item", exception));
            }
        });
        try {
            request.setRequestData(helper.asXml(groupName, item));
            request.send();
        } catch (RequestException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, "Couldn't save menu item", e));
        }
    }
    
    public void deleteMenuItem(String groupName, FBMenuItem item) {
        RequestBuilder request = new RequestBuilder(RequestBuilder.DELETE, 
                GWT.getModuleBaseURL() + this.contextPath + "/menuItems/");
        request.setCallback(new RequestCallback() {
            public void onResponseReceived(Request request, Response response) {
                int code = response.getStatusCode();
                if (code != Response.SC_ACCEPTED && code != Response.SC_NO_CONTENT && code != Response.SC_OK) {
                    bus.fireEvent(new NotificationEvent(Level.WARN, "Error deleting menu item on server (code = " + code + ")"));
                } else {
                    bus.fireEvent(new NotificationEvent(Level.INFO, "menu item deleted successfully"));
                }
            }

            public void onError(Request request, Throwable exception) {
                bus.fireEvent(new NotificationEvent(Level.ERROR, "Error deleting menu item on server", exception));
            }
        });
        try {
            request.setRequestData(helper.asXml(groupName, item));
            request.send();
        } catch (RequestException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, "Error deleting menu item on server", e));
        }
    }
    
    public void deleteForm(FormRepresentation form) {
        RequestBuilder request = new RequestBuilder(RequestBuilder.DELETE,
                GWT.getModuleBaseURL() + this.contextPath + 
                "/formDefinitions/package/defaultPackage/formDefinitionId/" + form.getName());
        request.setCallback(new RequestCallback() {
            public void onResponseReceived(Request request, Response response) {
                int code = response.getStatusCode();
                if (code != Response.SC_ACCEPTED && code != Response.SC_NO_CONTENT && code != Response.SC_OK) {
                    bus.fireEvent(new NotificationEvent(Level.WARN, "Error deleting form on server (code = " + code + ")"));
                } else {
                    bus.fireEvent(new NotificationEvent(Level.INFO, "form deleted successfully"));
                }
            }
            public void onError(Request request, Throwable exception) {
                bus.fireEvent(new NotificationEvent(Level.ERROR, "Error deleting form on server", exception));
            }
        });
        try {
            request.send();
        } catch (RequestException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, "Couldn't send form " + form.getName() + " deletion to server", e));
        }
    }
    
    public void deleteFormItem(String formItemName, FormItemRepresentation formItem) {
        RequestBuilder request = new RequestBuilder(RequestBuilder.DELETE, 
                GWT.getModuleBaseURL() + this.contextPath + "/formItems/package/defaultPackage/formItemName/" + formItemName);
        request.setCallback(new RequestCallback() {
            public void onResponseReceived(Request request, Response response) {
                int code = response.getStatusCode();
                if (code != Response.SC_ACCEPTED && code != Response.SC_NO_CONTENT && code != Response.SC_OK) {
                    bus.fireEvent(new NotificationEvent(Level.WARN, "Error deleting form item on server (code = " + code + ")"));
                } else {
                    bus.fireEvent(new NotificationEvent(Level.INFO, "form item deleted successfully"));
                }
            }
            public void onError(Request request, Throwable exception) {
                bus.fireEvent(new NotificationEvent(Level.ERROR, "Error deleting form item on server", exception));
            }
        });
        try {
            request.send();
        } catch (RequestException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, "Couldn't send form item " + formItemName + " deletion to server", e));
        }
    }
    
    
    public List<TaskRef> getExistingTasks(final String filter) {
        final List<TaskRef> retval = new ArrayList<TaskRef>();
        String url = GWT.getModuleBaseURL() + this.contextPath + "/tasks/package/defaultPackage/";
        if (filter != null && !"".equals(filter)) {
            url = url + "q=" + URL.encodeQueryString(filter);
        }
        RequestBuilder request = new RequestBuilder(RequestBuilder.GET, url);
        request.setCallback(new RequestCallback() {
            public void onResponseReceived(Request request, Response response) {
                retval.addAll(helper.readTasks(response.getText()));
                bus.fireEvent(new ExistingTasksResponseEvent(retval, filter));
            }
            public void onError(Request request, Throwable exception) {
                bus.fireEvent(new NotificationEvent(Level.ERROR, "Couldn't read tasks", exception));
            }
        });
        try {
            request.send();
        } catch (RequestException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, "Couldn't read tasks", e));
        }
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
