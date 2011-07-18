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

import org.jbpm.formbuilder.client.bus.ui.NotificationEvent;
import org.jbpm.formbuilder.client.bus.ui.NotificationEvent.Level;
import org.jbpm.formbuilder.client.command.DisposeDropController;
import org.jbpm.formbuilder.client.edition.EditionPresenter;
import org.jbpm.formbuilder.client.edition.EditionView;
import org.jbpm.formbuilder.client.form.FormEncodingClientFactory;
import org.jbpm.formbuilder.client.layout.LayoutPresenter;
import org.jbpm.formbuilder.client.layout.LayoutView;
import org.jbpm.formbuilder.client.menu.MenuPresenter;
import org.jbpm.formbuilder.client.menu.MenuView;
import org.jbpm.formbuilder.client.notification.NotificationsPresenter;
import org.jbpm.formbuilder.client.notification.NotificationsView;
import org.jbpm.formbuilder.client.options.OptionsPresenter;
import org.jbpm.formbuilder.client.options.OptionsView;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.client.tasks.TasksPresenter;
import org.jbpm.formbuilder.client.tasks.TasksView;
import org.jbpm.formbuilder.client.toolbar.ToolBarPresenter;
import org.jbpm.formbuilder.client.toolbar.ToolBarView;
import org.jbpm.formbuilder.client.tree.TreePresenter;
import org.jbpm.formbuilder.client.tree.TreeView;
import org.jbpm.formbuilder.shared.form.FormEncodingFactory;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.event.shared.EventBus;

public class FormBuilderController {

    private final EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
    
    /**
     * Initiates gwt-dnd drag controller and sub views and presenters
     * @param model
     * @param view
     */
    public FormBuilderController(FormBuilderService model, FormBuilderView view) {
        super();
        GWT.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            public void onUncaughtException(Throwable exception) {
                bus.fireEvent(new NotificationEvent(Level.ERROR, "An error ocurred in the UI", exception));
            }
        });
        FormEncodingFactory.register(FormEncodingClientFactory.getEncoder(), FormEncodingClientFactory.getDecoder());
        PickupDragController dragController = new PickupDragController(view, true);
        dragController.registerDropController(new DisposeDropController(view));
        FormBuilderGlobals.getInstance().registerDragController(dragController);
        view.setNotificationsView(createNotifications());
        populateRepresentationFactory(model);
        view.setMenuView(createMenu(model));
        view.setEditionView(createEdition());
        view.setLayoutView(createLayout());
        view.setOptionsView(createOptions(model));
        view.setTasksView(createTasks(model));
        view.setToolBarView(createToolBar());
        view.setTreeView(createTree());
    }

    private void populateRepresentationFactory(FormBuilderService model) {
        try {
            model.populateRepresentationFactory();
        } catch (FormBuilderException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, "Problem obtaining representation - ui component mapping", e));
        }
    }

    private EditionView createEdition() {
        EditionView view = new EditionView();
        new EditionPresenter(view);
        return view;
    }

    private MenuView createMenu(FormBuilderService model) {
        MenuView view = new MenuView();
        try {
            new MenuPresenter(model.getMenuItems(), view);
        } catch (FormBuilderException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, "Problem creating menu items", e));
        }
        return view;
    }

    private LayoutView createLayout() {
        LayoutView view = new LayoutView();
        new LayoutPresenter(view);
        return view;
    }
    
    private OptionsView createOptions(FormBuilderService model) {
        OptionsView view = new OptionsView();
        try {
            new OptionsPresenter(model.getMenuOptions(), view);
        } catch (FormBuilderException e) {
            bus.fireEvent(new NotificationEvent(Level.ERROR, "Problem creating menu options", e));
        }
        return view;
    }
    
    private TasksView createTasks(FormBuilderService model) {
        TasksView view = new TasksView();
        new TasksPresenter(model, view);
        return view;
    }
    
    private ToolBarView createToolBar() {
        ToolBarView view = new ToolBarView();
        new ToolBarPresenter(view);
        return view;
    }
    
    private NotificationsView createNotifications() {
        NotificationsView view = new NotificationsView();
        new NotificationsPresenter(view);
        return view;
    }
    
    private TreeView createTree() {
        TreeView view = new TreeView();
        new TreePresenter(view);
        return view;
    }

}
