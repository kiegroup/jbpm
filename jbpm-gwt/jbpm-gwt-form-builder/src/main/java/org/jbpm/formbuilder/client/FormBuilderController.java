package org.jbpm.formbuilder.client;

import org.jbpm.formbuilder.client.command.DisposeDropController;
import org.jbpm.formbuilder.client.edition.EditionPresenter;
import org.jbpm.formbuilder.client.edition.EditionView;
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

import com.allen_sauer.gwt.dnd.client.PickupDragController;

public class FormBuilderController {

    public FormBuilderController(FormBuilderService model, FormBuilderView view) {
        super();
        PickupDragController dragController = new PickupDragController(view, true);
        FormBuilderGlobals.getInstance().registerDragController(dragController);
        dragController.registerDropController(new DisposeDropController(view));
        view.setMenuView(createMenu(model));
        view.setEditionView(createEdition());
        view.setLayoutView(createLayout());
        view.setOptionsView(createOptions(model));
        view.setTasksView(createTasks());
        view.setToolBarView(createToolBar());
        view.setNotificationsView(createNotifications());
    }

    private EditionView createEdition() {
        EditionView view = new EditionView();
        new EditionPresenter(view);
        return view;
    }

    private MenuView createMenu(FormBuilderService model) {
        MenuView view = new MenuView();
        new MenuPresenter(model.getMenuItems(), view);
        return view;
    }

    private LayoutView createLayout() {
        LayoutView view = new LayoutView();
        new LayoutPresenter(view);
        return view;
    }
    
    private OptionsView createOptions(FormBuilderService model) {
        OptionsView view = new OptionsView();
        new OptionsPresenter(model.getMenuOptions(), view);
        return view;
    }
    
    private TasksView createTasks() {
        TasksView view = new TasksView();
        new TasksPresenter(view);
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

}
