package org.jbpm.formbuilder.client;

import org.jbpm.formbuilder.client.bus.NotificationEvent;
import org.jbpm.formbuilder.client.bus.NotificationEvent.Level;
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
import com.google.gwt.event.shared.EventBus;

public class FormBuilderController {

    private final EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
    
    public FormBuilderController(FormBuilderService model, FormBuilderView view) {
        super();
        PickupDragController dragController = new PickupDragController(view, true);
        FormBuilderGlobals.getInstance().registerDragController(dragController);
        dragController.registerDropController(new DisposeDropController(view));
        view.setNotificationsView(createNotifications());
        view.setMenuView(createMenu(model));
        view.setEditionView(createEdition());
        view.setLayoutView(createLayout());
        view.setOptionsView(createOptions(model));
        view.setTasksView(createTasks(model));
        view.setToolBarView(createToolBar());
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

}
