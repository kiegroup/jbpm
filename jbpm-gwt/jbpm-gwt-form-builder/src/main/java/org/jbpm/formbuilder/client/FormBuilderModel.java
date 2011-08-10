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
import org.jbpm.formbuilder.client.bus.ExistingValidationsResponseEvent;
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
import org.jbpm.formbuilder.client.bus.ui.RepresentationFactoryPopulatedEvent;
import org.jbpm.formbuilder.client.bus.ui.TaskSelectedEvent;
import org.jbpm.formbuilder.client.effect.FBFormEffect;
import org.jbpm.formbuilder.client.menu.FBMenuItem;
import org.jbpm.formbuilder.client.menu.items.CustomMenuItem;
import org.jbpm.formbuilder.client.messages.Constants;
import org.jbpm.formbuilder.client.options.MainMenuOption;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.client.validation.FBValidationItem;
import org.jbpm.formbuilder.shared.form.FormEncodingException;
import org.jbpm.formbuilder.shared.form.FormEncodingFactory;
import org.jbpm.formbuilder.shared.form.MockFormDefinitionService;
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
import com.google.gwt.user.client.ui.VerticalPanel;

public class FormBuilderModel implements FormBuilderService {

    private static final String DEFAULT_PACKAGE_NAME = "defaultPackage";

    private final EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
    private final Constants i18n = FormBuilderGlobals.getInstance().getI18n();
    
    private final String contextPath;
    private final XmlParseHelper helper = new XmlParseHelper();
    private final MockFormDefinitionService mockFormService = new MockFormDefinitionService();
    private String packageName = DEFAULT_PACKAGE_NAME;
    
    public FormBuilderModel(String contextPath) {
        this.contextPath = contextPath;
        //registered to save the menu items
        bus.addHandler(MenuItemAddedEvent.TYPE, new MenuItemAddedHandler() {
            @Override
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
            @Override
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
    
    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
    
    @Override
    public FormRepresentation getForm(final String formName) throws FormBuilderException {
        final String myFormName;
        if (!formName.startsWith("formDefinition_")) { 
            myFormName = "formDefinition_" + formName;
        } else {
            myFormName = formName;
        }
        String url = new StringBuilder(GWT.getModuleBaseURL()).append(this.contextPath).
            append("/formDefinitions/package/").append(this.packageName).append("/formDefinitionId/").
            append(myFormName).append("/").toString();
        RequestBuilder request = new RequestBuilder(RequestBuilder.GET, url);
        final List<FormRepresentation> list = new ArrayList<FormRepresentation>();
        request.setCallback(new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
                if (response.getStatusCode() == Response.SC_OK) {
                    list.addAll(helper.readForms(response.getText()));
                    bus.fireEvent(new LoadServerFormResponseEvent(list.isEmpty() ? null : list.iterator().next()));
                } else {
                    bus.fireEvent(new NotificationEvent(Level.ERROR, i18n.CouldntFindForm404(formName)));
                }
            }
            @Override
            public void onError(Request request, Throwable exception) {
                bus.fireEvent(new NotificationEvent(Level.ERROR, i18n.CouldntFindForm(formName), exception));
            }
        });
        try {
            request.send();
        } catch (RequestException e) {
            throw new FormBuilderException(e);
        }
        return list.isEmpty() ? null : list.iterator().next();
    }

    @Override
    public List<FormRepresentation> getForms() throws FormBuilderException {
        String url = new StringBuilder(GWT.getModuleBaseURL()).append(this.contextPath).
            append("/formDefinitions/package/").append(this.packageName).append("/").toString();
        RequestBuilder request = new RequestBuilder(RequestBuilder.GET, url);
        final List<FormRepresentation> list = new ArrayList<FormRepresentation>();
        request.setCallback(new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
                if (response.getStatusCode() == Response.SC_OK) {
                    list.addAll(helper.readForms(response.getText()));
                    bus.fireEvent(new LoadServerFormResponseEvent(list));
                } else {
                    bus.fireEvent(new NotificationEvent(Level.ERROR, i18n.CouldntFindForms404()));
                }
            }
            @Override
            public void onError(Request request, Throwable exception) {
                bus.fireEvent(new NotificationEvent(Level.ERROR, i18n.CouldntFindForms(), exception));
            }
        });
        try {
            request.send();
        } catch (RequestException e) {
            throw new FormBuilderException(e);
        }
        return list;
    }
    
    @Override
    public void populateRepresentationFactory() throws FormBuilderException {
        String url = GWT.getModuleBaseURL() + this.contextPath + "/representationMappings/";
        RequestBuilder request = new RequestBuilder(RequestBuilder.GET, url);
        request.setCallback(new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
                Map<String, String> repMap = helper.readPropertyMap(response.getText());
                for (Map.Entry<String, String> entry : repMap.entrySet()) {
                    RepresentationFactory.registerItemClassName(entry.getKey(), entry.getValue());
                }
                bus.fireEvent(new RepresentationFactoryPopulatedEvent());
            }
            @Override
            public void onError(Request request, Throwable exception) {
                bus.fireEvent(new NotificationEvent(Level.ERROR, i18n.CouldntReadRepresentationMappings(), exception));
            }
        });
        try {
            request.send();
        } catch (RequestException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, i18n.CouldntReadRepresentationMappings(), e));
        }
    }
    
    @Override
    public Map<String, List<FBMenuItem>> getMenuItems() {
        final Map<String, List<FBMenuItem>> menuItems = new HashMap<String, List<FBMenuItem>>();
        RequestBuilder request = new RequestBuilder(RequestBuilder.GET, GWT.getModuleBaseURL() + this.contextPath + "/menuItems/");
        request.setCallback(new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
            	if (response.getStatusCode() == Response.SC_OK) {
            		menuItems.putAll(helper.readMenuMap(response.getText()));
            		for (String groupName : menuItems.keySet()) {
            			for (FBMenuItem menuItem : menuItems.get(groupName)) {
            			    populateMockFormService(menuItem);
            				bus.fireEvent(new MenuItemFromServerEvent(menuItem, groupName));
            			}
            		}
            	} else {
            		bus.fireEvent(new NotificationEvent(Level.ERROR, i18n.CouldntFindMenuItems404()));
            	}
            }
            @Override
            public void onError(Request request, Throwable exception) {
                bus.fireEvent(new NotificationEvent(Level.ERROR, i18n.CouldntFindMenuItems(), exception));
            }
        });
        try {
            request.send();
        } catch (RequestException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, i18n.CouldntReadMenuItems(), e));
        }
        return menuItems;
    }
    
    private void populateMockFormService(FBMenuItem item) {
        String className = item.getClass().getName();
        List<String> effectClassNames = new ArrayList<String>();
        if (item.getFormEffects() != null) {
            for (FBFormEffect effect : item.getFormEffects()) {
                effectClassNames.add(effect.getClass().getName());
            }
        }
        mockFormService.putEffectsForItem(className, effectClassNames);
    }
    
    @Override
    public List<MainMenuOption> getMenuOptions() {
        final List<MainMenuOption> currentOptions = new ArrayList<MainMenuOption>();
        RequestBuilder request = new RequestBuilder(RequestBuilder.GET, GWT.getModuleBaseURL() + this.contextPath + "/menuOptions/");
        request.setCallback(new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
                currentOptions.addAll(helper.readMenuOptions(response.getText()));
                for (MainMenuOption option : currentOptions) {
                    bus.fireEvent(new MenuOptionAddedEvent(option));
                }
            }
            @Override
            public void onError(Request request, Throwable exception) {
                bus.fireEvent(new NotificationEvent(Level.ERROR, i18n.CouldntFindMenuOptions(), exception));
            }
        });
        try {
            request.send();
        } catch (RequestException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, i18n.CouldntReadMenuOptions(), e));
        }
        return currentOptions;
    }
    
    @Override
    public void saveFormItem(final FormItemRepresentation formItem, String formItemName) {
        RequestBuilder request = new RequestBuilder(RequestBuilder.POST, new StringBuilder().
                append(GWT.getModuleBaseURL()).append(this.contextPath).append("/formItems/package/").
                append(this.packageName).append("/").toString());
        request.setCallback(new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
                if (response.getStatusCode() == Response.SC_CONFLICT) {
                    bus.fireEvent(new NotificationEvent(Level.WARN, i18n.FormItemAlreadyUpdated()));
                } else if (response.getStatusCode() != Response.SC_CREATED) {
                    bus.fireEvent(new NotificationEvent(Level.WARN, i18n.SaveFormItemUnknownStatus(String.valueOf(response.getStatusCode()))));
                } else {
                    String name = helper.getFormItemId(response.getText());
                    bus.fireEvent(new NotificationEvent(Level.INFO, i18n.FormItemSaved(name)));
                }
            }
            @Override
            public void onError(Request request, Throwable exception) {
                bus.fireEvent(new NotificationEvent(Level.ERROR, i18n.CouldntSaveFormItem(), exception));
            }
        });
        try {
            request.setRequestData(helper.asXml(formItemName, formItem));
            request.send();
        } catch (RequestException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, i18n.CouldntSendFormItem(formItemName), e));
        } catch (FormEncodingException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, i18n.CouldntDecodeFormItem(formItemName), e));
        }
    }
    
    @Override
    public void saveForm(final FormRepresentation form) {
        RequestBuilder request = new RequestBuilder(RequestBuilder.POST, new StringBuilder(). 
                append(GWT.getModuleBaseURL()).append(this.contextPath). 
                append("/formDefinitions/package/").append(this.packageName).append("/").toString());
        request.setCallback(new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
                if (response.getStatusCode() == Response.SC_CONFLICT) {
                    bus.fireEvent(new NotificationEvent(Level.WARN, i18n.FormAlreadyUpdated()));
                } else if (response.getStatusCode() != Response.SC_CREATED) {
                    bus.fireEvent(new NotificationEvent(Level.WARN, i18n.SaveFormUnkwnownStatus(String.valueOf(response.getStatusCode()))));
                } else {
                    String name = helper.getFormId(response.getText());
                    form.setLastModified(System.currentTimeMillis());
                    form.setSaved(true);
                    form.setName(name);
                    bus.fireEvent(new FormSavedEvent(form));
                }
            }
            @Override
            public void onError(Request request, Throwable exception) {
                bus.fireEvent(new NotificationEvent(Level.ERROR, i18n.CouldntSaveForm(), exception));
            }
        });
        try {
            String json = FormEncodingFactory.getEncoder().encode(form);
            request.setRequestData(json);
            request.send();
        } catch (RequestException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, i18n.CouldntSendForm(), e));
        } catch (FormEncodingException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, i18n.CouldntDecodeForm(), e));
        }
    }
    
    @Override
    public void generateForm(FormRepresentation form, String language, Map<String, Object> inputs) {
        RequestBuilder request = new RequestBuilder(RequestBuilder.POST, 
                new StringBuilder(GWT.getModuleBaseURL()).append(this.contextPath).
                    append("/formPreview/lang/").append(language).toString());
        request.setCallback(new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
                String html = response.getText();
                bus.fireEvent(new PreviewFormResponseEvent(html));
            }
            @Override
            public void onError(Request request, Throwable exception) {
                bus.fireEvent(new NotificationEvent(Level.ERROR, i18n.CouldntPreviewForm(), exception));
            }
        });
        try {
            request.setRequestData(helper.asXml(form, inputs));
            request.send();
        } catch (RequestException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, i18n.CouldntSendForm(), e));
        } catch (FormEncodingException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, i18n.CouldntDecodeForm(), e));
        }
    }
    
    @Override
    public void loadFormTemplate(final FormRepresentation form, final String language) {
        final String url = new StringBuilder(GWT.getModuleBaseURL()).append(this.contextPath)
                                .append("/formTemplate/lang/").append(language).toString();
        RequestBuilder request = new RequestBuilder(RequestBuilder.POST, url);
        request.setCallback(new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
                String fileName = helper.getFileName(response.getText());
                FormPanel auxiliarForm = new FormPanel();
                auxiliarForm.setMethod("get");
                auxiliarForm.setAction(url);
                Hidden hidden1 = new Hidden("fileName");
                hidden1.setValue(fileName);
                Hidden hidden2 = new Hidden("formName");
                hidden2.setValue(form.getName() == null || "".equals(form.getName()) ? "template" : form.getName());
                VerticalPanel vPanel = new VerticalPanel();
                vPanel.add(hidden1);
                vPanel.add(hidden2);
                auxiliarForm.add(vPanel);
                RootPanel.get().add(auxiliarForm);
                auxiliarForm.submit();
            }
            @Override
            public void onError(Request request, Throwable exception) {
                bus.fireEvent(new NotificationEvent(Level.ERROR, i18n.CouldntExportTemplate(), exception));
            }
        });
        try {
            request.setRequestData(helper.asXml(form, null));
            request.send();
        } catch (RequestException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, i18n.CouldntSendForm(), e));
        } catch (FormEncodingException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, i18n.CouldntDecodeForm(), e));
        }
    }

    @Override
    public void saveMenuItem(final String groupName, final FBMenuItem item) {
        RequestBuilder request = new RequestBuilder(RequestBuilder.POST, 
                GWT.getModuleBaseURL() + this.contextPath + "/menuItems/" + groupName + "/");
        request.setCallback(new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
                int code = response.getStatusCode();
                NotificationEvent event;
                if (code == Response.SC_CREATED) {
                    event = new NotificationEvent(Level.INFO, i18n.MenuItemSaved(item.getItemId()));
                } else {
                    event = new NotificationEvent(Level.WARN, i18n.SaveMenuItemInvalidStatus(String.valueOf(code)));
                }
                bus.fireEvent(event);
            }
            @Override
            public void onError(Request request, Throwable exception) {
                bus.fireEvent(new NotificationEvent(Level.ERROR, i18n.CouldntGenerateMenuItem(), exception));
            }
        });
        try {
            request.setRequestData(helper.asXml(groupName, item));
            request.send();
        } catch (RequestException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, i18n.CouldntSaveMenuItem(), e));
        }
    }
    
    @Override
    public void deleteMenuItem(String groupName, FBMenuItem item) {
        RequestBuilder request = new RequestBuilder(RequestBuilder.DELETE, 
                GWT.getModuleBaseURL() + this.contextPath + "/menuItems/");
        request.setCallback(new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
                int code = response.getStatusCode();
                if (code != Response.SC_ACCEPTED && code != Response.SC_NO_CONTENT && code != Response.SC_OK) {
                    bus.fireEvent(new NotificationEvent(Level.WARN, i18n.DeleteMenuItemUnkownStatus(String.valueOf(code))));
                } else {
                    bus.fireEvent(new NotificationEvent(Level.INFO, i18n.MenuItemDeleted()));
                }
            }
            @Override
            public void onError(Request request, Throwable exception) {
                bus.fireEvent(new NotificationEvent(Level.ERROR, i18n.ErrorDeletingMenuItem(), exception));
            }
        });
        try {
            request.setRequestData(helper.asXml(groupName, item));
            request.send();
        } catch (RequestException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, i18n.ErrorDeletingMenuItem(), e));
        }
    }

    @Override
    public void deleteForm(FormRepresentation form) {
        RequestBuilder request = new RequestBuilder(RequestBuilder.DELETE, new StringBuilder().
                append(GWT.getModuleBaseURL()).append(this.contextPath).
                append("/formDefinitions/package/").append(this.packageName).
                append("/formDefinitionId/").append(form.getName()).toString());
        request.setCallback(new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
                int code = response.getStatusCode();
                if (code != Response.SC_ACCEPTED && code != Response.SC_NO_CONTENT && code != Response.SC_OK) {
                    bus.fireEvent(new NotificationEvent(Level.WARN, i18n.ErrorDeletingForm(String.valueOf(code))));
                } else {
                    bus.fireEvent(new NotificationEvent(Level.INFO, i18n.FormDeleted()));
                }
            }
            @Override
            public void onError(Request request, Throwable exception) {
                bus.fireEvent(new NotificationEvent(Level.ERROR, i18n.ErrorDeletingForm(""), exception));
            }
        });
        try {
            request.send();
        } catch (RequestException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, i18n.CouldntSendForm(), e));
        }
    }
    
    @Override
    public void deleteFormItem(String formItemName, FormItemRepresentation formItem) {
        RequestBuilder request = new RequestBuilder(RequestBuilder.DELETE, new StringBuilder().
                append(GWT.getModuleBaseURL()).append(this.contextPath).append("/formItems/package/").
                append(this.packageName).append("/formItemName/").append(formItemName).toString());
        request.setCallback(new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
                int code = response.getStatusCode();
                if (code != Response.SC_ACCEPTED && code != Response.SC_NO_CONTENT && code != Response.SC_OK) {
                    bus.fireEvent(new NotificationEvent(Level.WARN, i18n.ErrorDeletingFormItem(String.valueOf(code))));
                } else {
                    bus.fireEvent(new NotificationEvent(Level.INFO, i18n.FormItemDeleted()));
                }
            }
            @Override
            public void onError(Request request, Throwable exception) {
                bus.fireEvent(new NotificationEvent(Level.ERROR, i18n.ErrorDeletingFormItem(""), exception));
            }
        });
        try {
            request.send();
        } catch (RequestException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, i18n.CouldntSendFormItem(formItemName), e));
        }
    }
    
    @Override
    public List<TaskRef> getExistingIoAssociations(final String filter) {
        final List<TaskRef> retval = new ArrayList<TaskRef>();
        String url = GWT.getModuleBaseURL() + this.contextPath + "/ioAssociations/package/" + this.packageName + "/";
        RequestBuilder request = new RequestBuilder(RequestBuilder.GET, url);
        request.setCallback(new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
                retval.addAll(helper.readTasks(response.getText()));
                bus.fireEvent(new ExistingTasksResponseEvent(retval, filter));
            }
            @Override
            public void onError(Request request, Throwable exception) {
                bus.fireEvent(new NotificationEvent(Level.ERROR, i18n.CouldntReadTasks(), exception));
            }
        });
        try {
            if (filter != null && !"".equals(filter)) {
                request.setRequestData("q=" + URL.encodeQueryString(filter));
            }
            request.send();
        } catch (RequestException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, i18n.CouldntReadTasks(), e));
        }
        return retval;
    }
    
    @Override
    public List<FBValidationItem> getExistingValidations() throws FormBuilderException {
        final List<FBValidationItem> retval = new ArrayList<FBValidationItem>();
        String url = GWT.getModuleBaseURL() + this.contextPath + "/validations/";
        RequestBuilder request = new RequestBuilder(RequestBuilder.GET, url);
        request.setCallback(new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
                try {
                    retval.addAll(helper.readValidations(response.getText()));
                    bus.fireEvent(new ExistingValidationsResponseEvent(retval));
                } catch (Exception e) {
                    bus.fireEvent(new NotificationEvent(Level.ERROR, i18n.CouldntDecodeValidations(), e));
                }
            }
            @Override
            public void onError(Request request, Throwable exception) {
                bus.fireEvent(new NotificationEvent(Level.ERROR, i18n.CouldntReadValidations(), exception));
            }
        });
        try {
            request.send();
        } catch (RequestException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, i18n.CouldntReadValidations(), e));
        }
        return retval;
    }

    @Override
    public void selectIoAssociation(String pkgName, String processName, String taskName) {
        String url = new StringBuilder(GWT.getModuleBaseURL()).append(this.contextPath).
            append("/ioAssociation/package/").append(pkgName).append("/process/").
            append(processName).append("/task/").append(taskName).toString();
        RequestBuilder request = new RequestBuilder(RequestBuilder.GET, url);
        request.setCallback(new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
                List<TaskRef> tasks = helper.readTasks(response.getText());
                if (tasks.size() == 1) {
                    TaskRef singleTask = tasks.iterator().next();
                    bus.fireEvent(new TaskSelectedEvent(singleTask));
                }
            }
            @Override
            public void onError(Request request, Throwable exception) {
                bus.fireEvent(new NotificationEvent(Level.ERROR, i18n.CouldntReadSingleIO(), exception));
            } 
        });
        try {
            request.send();
        } catch (RequestException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, i18n.CouldntReadSingleIO(), e));
        }
    }
    
    @Override
    public FormRepresentation toBasicForm(TaskRef task) {
        return mockFormService.createFormFromTask(task);
    }
}
