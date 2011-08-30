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

import org.jbpm.formbuilder.client.bus.ui.EmbededIOReferenceEvent;
import org.jbpm.formbuilder.client.bus.ui.NotificationEvent;
import org.jbpm.formbuilder.client.bus.ui.NotificationEvent.Level;
import org.jbpm.formbuilder.client.bus.ui.RepresentationFactoryPopulatedEvent;
import org.jbpm.formbuilder.client.bus.ui.RepresentationFactoryPopulatedHandler;
import org.jbpm.formbuilder.client.bus.ui.UpdateFormViewEvent;
import org.jbpm.formbuilder.client.command.DisposeDropController;
import org.jbpm.formbuilder.client.edition.EditionViewImpl;
import org.jbpm.formbuilder.client.form.FormEncodingClientFactory;
import org.jbpm.formbuilder.client.layout.LayoutViewImpl;
import org.jbpm.formbuilder.client.menu.AnimatedMenuViewImpl;
import org.jbpm.formbuilder.client.messages.I18NConstants;
import org.jbpm.formbuilder.client.notification.NotificationsViewImpl;
import org.jbpm.formbuilder.client.options.OptionsViewImpl;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.client.tasks.IoAssociationViewImpl;
import org.jbpm.formbuilder.client.toolbar.ToolBarViewImpl;
import org.jbpm.formbuilder.client.tree.TreeViewImpl;
import org.jbpm.formbuilder.shared.api.FormRepresentation;
import org.jbpm.formbuilder.shared.form.FormEncodingException;
import org.jbpm.formbuilder.shared.form.FormEncodingFactory;
import org.jbpm.formbuilder.shared.form.FormRepresentationDecoder;
import org.jbpm.formbuilder.shared.task.TaskPropertyRef;
import org.jbpm.formbuilder.shared.task.TaskRef;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.ui.RootPanel;

public class FormBuilderController {

    private final EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
    private final I18NConstants i18n = FormBuilderGlobals.getInstance().getI18n();
    private final RestyFormBuilderModel model;
    private final FormBuilderView view;
    
    private final FormExporter formExporter;
    
    /**
     * Initiates gwt-dnd drag controller and sub views and presenters
     * @param fbModel
     * @param fbView
     */
    public FormBuilderController(final RootPanel rootPanel, RestyFormBuilderModel fbModel, FormBuilderView fbView) {
        super();
        this.model = fbModel;
        this.view = fbView;
        GWT.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            @Override
            public void onUncaughtException(Throwable exception) {
                bus.fireEvent(new NotificationEvent(Level.ERROR, i18n.ErrorInTheUI(), exception));
            }
        });
        FormEncodingFactory.register(FormEncodingClientFactory.getEncoder(), FormEncodingClientFactory.getDecoder());
        PickupDragController dragController = new PickupDragController(view, true);
        dragController.registerDropController(new DisposeDropController(view));
        FormBuilderGlobals.getInstance().registerDragController(dragController);
        
        this.formExporter = new FormExporter();
        this.formExporter.start();
        
        view.setNotificationsView(new NotificationsViewImpl());
        view.setMenuView(new AnimatedMenuViewImpl());
        model.getMenuItems();
        view.setEditionView(new EditionViewImpl());
        view.setTreeView(new TreeViewImpl());
        view.setLayoutView(new LayoutViewImpl());
        view.setOptionsView(new OptionsViewImpl());
        model.getMenuOptions();
        view.setIoAssociationView(new IoAssociationViewImpl());
        view.setToolBarView(new ToolBarViewImpl());
        bus.addHandler(RepresentationFactoryPopulatedEvent.TYPE, new RepresentationFactoryPopulatedHandler() {
            @Override
            public void onEvent(RepresentationFactoryPopulatedEvent event) {
                List<GwtEvent<?>> events = setDataPanel(rootPanel);
                setViewPanel(rootPanel);
                //events are fired deferred since they might need that ui components are already attached
                fireEvents(events);
            }
        });
        populateRepresentationFactory(fbModel);
    }

    private void fireEvents(List<GwtEvent<?>> events) {
        if (events != null) {
            for (GwtEvent<?> event : events) {
                bus.fireEvent(event);
            }
        }
    }
    
    private List<GwtEvent<?>> setDataPanel(RootPanel rootPanel) {
        List<GwtEvent<?>> retval = new ArrayList<GwtEvent<?>>();
        String innerHTML = rootPanel.getElement().getInnerHTML();
        if (innerHTML != null && !"".equals(innerHTML)) {
            try {
                JSONValue json = JSONParser.parseStrict(innerHTML);
                if (json.isObject() != null) {
                    TaskRef task = null;
                    String profileName = null;
                    String pkgName = null;
                    JSONObject jsonObj = json.isObject();
                    if (jsonObj.get("embedded") != null && jsonObj.get("embedded").isString() != null) {
                        profileName = jsonObj.get("embedded").isString().stringValue();
                    }
                    JSONValue jsonPkg = jsonObj.get("packageName");
                    if (jsonPkg != null && jsonPkg.isString() != null) {
                        pkgName = jsonPkg.isString().stringValue();
                        if (pkgName != null && !"".equals(pkgName)) {
                            model.setPackageName(pkgName);
                        }
                    }
                    if (jsonObj.get("task") != null && jsonObj.get("task").isObject() != null) {
                        task = toTask(jsonObj.get("task").isObject());
                    }
                    if (jsonObj.get("formjson") != null && jsonObj.get("formjson").isString() != null) {
                        FormRepresentation form = toForm(jsonObj.get("formjson").isString().stringValue());
                        if (form != null) {
                            retval.add(new UpdateFormViewEvent(form));
                            if (task == null && hasTaskAssigned(form)) {
                                model.selectIoAssociation(pkgName, form.getProcessName(), form.getTaskId());
                            }
                        }
                    }
                    retval.add(new EmbededIOReferenceEvent(task, profileName));
                }
            } catch (Exception e) {
                GWT.log("Problem parsing init content", e);
            }
        }
        return retval;
    }
    
    private boolean hasTaskAssigned(FormRepresentation form) {
        boolean notNull = form != null && form.getProcessName() != null && form.getTaskId() != null;
        return notNull && !"".equals(form.getProcessName().trim()) && !"".equals(form.getTaskId().trim());
    }
    
    private FormRepresentation toForm(String json) {
        FormRepresentationDecoder decoder = FormEncodingFactory.getDecoder();
        FormRepresentation form = null;
        try {
            form = decoder.decode(json);
        } catch (FormEncodingException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, i18n.CouldntLoadFromEmbeded(), e));
        }
        return form;
    }
    
    private TaskRef toTask(JSONObject json) {
        TaskRef retval = null;
        if (json != null) {
            retval = new TaskRef();
            retval.setInputs(getIOData(json.get("inputs").isArray()));
            retval.setOutputs(getIOData(json.get("outputs").isArray()));
            Map<String, String> metaData = new HashMap<String, String>();
            JSONObject jsonMetaData = json.get("metaData") == null ? null : json.get("metaData").isObject();
            if (jsonMetaData != null) {
                for (String key : jsonMetaData.keySet()) {
                    metaData.put(key, jsonMetaData.get(key).isString().stringValue());
                }
            }
            retval.setMetaData(metaData);
            if (json.get("packageName") != null && json.get("packageName").isString() != null) {
                retval.setPackageName(json.get("packageName").isString().stringValue());
            }
            if (json.get("processId") != null && json.get("processId").isString() != null) {
                retval.setProcessId(json.get("processId").isString().stringValue());
            }
            if (json.get("taskId") != null && json.get("taskId").isString() != null) {
                retval.setTaskId(json.get("taskId").isString().stringValue());
            }
        }
        return retval;
    }

    private List<TaskPropertyRef> getIOData(JSONArray jsonIO) {
        List<TaskPropertyRef> retval = new ArrayList<TaskPropertyRef>();
        if (jsonIO != null) {
            for (int index = 0; index < jsonIO.size(); index++) {
                JSONObject jsonIo = jsonIO.get(index).isObject();
                TaskPropertyRef io = new TaskPropertyRef();
                if (jsonIo.get("name") != null && jsonIo.get("name").isString() != null) {
                    io.setName(jsonIo.get("name").isString().stringValue());
                }
                if (jsonIo.get("sourceExpression") != null && jsonIo.get("sourceExpression").isString() != null) {
                    io.setSourceExpresion(jsonIo.get("sourceExpression").isString().stringValue());
                }
                retval.add(io);
            }
        }
        return retval;
    }

    private void setViewPanel(RootPanel rootPanel) {
        rootPanel.getElement().setInnerHTML("");
        rootPanel.getElement().getStyle().setVisibility(Visibility.VISIBLE);
        rootPanel.add(view);
    }
    
    private void populateRepresentationFactory(FormBuilderService model) {
        try {
            model.populateRepresentationFactory();
        } catch (FormBuilderException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, i18n.ProblemLoadingRepresentationFactory(), e));
        }
    }
}
