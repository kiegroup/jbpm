package org.jbpm.formbuilder.client;

import org.jbpm.formbuilder.client.edition.EditionView;
import org.jbpm.formbuilder.client.layout.LayoutView;
import org.jbpm.formbuilder.client.menu.MenuView;
import org.jbpm.formbuilder.client.notification.NotificationsView;
import org.jbpm.formbuilder.client.options.OptionsView;
import org.jbpm.formbuilder.client.tasks.TasksView;
import org.jbpm.formbuilder.client.toolbar.ToolBarView;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.RootPanel;

public class FormBuilderView extends AbsolutePanel {

    private final Grid mainGrid = new Grid(3, 1);
    private final Grid toolGrid = new Grid(1, 3);
    private final Grid editGrid = new Grid(2, 1);
    private final Grid layoutGrid = new Grid(2, 1);
    
    public FormBuilderView() {
        layoutGrid.getCellFormatter().setHeight(1, 0, "70px");
        
        mainGrid.getCellFormatter().setHeight(0, 0, "50px");
        int mainHeight = RootPanel.getBodyElement().getClientHeight() - 100;
        mainGrid.getCellFormatter().setHeight(1, 0, "" + mainHeight + "px");
        mainGrid.getCellFormatter().setHeight(2, 0, "50px");
        
        toolGrid.getColumnFormatter().setWidth(0, "25%");
        toolGrid.getColumnFormatter().setWidth(1, "50%");
        toolGrid.getColumnFormatter().setWidth(2, "25%");
        toolGrid.setHeight("100%");
        
        int menuHeight = mainHeight / 2;
        editGrid.setSize("100%", "100%");
        editGrid.setCellPadding(0);
        editGrid.setCellSpacing(0);
        editGrid.getCellFormatter().setHeight(0, 0, "" + menuHeight + "px");
        editGrid.getCellFormatter().setHeight(1, 0, "50%");
        
        toolGrid.setWidget(0, 0, editGrid);
        toolGrid.setWidget(0, 1, layoutGrid);
        mainGrid.setWidget(1, 0, toolGrid);
        add(mainGrid);
    }

    public void setMenuView(MenuView menuView) {
        editGrid.setWidget(0, 0, menuView);
    }
    
    public void setEditionView(EditionView editionView) {
        editGrid.setWidget(1, 0, editionView);
    }

    public void setLayoutView(LayoutView layoutView) {
        layoutGrid.setWidget(0, 0, layoutView);
    }
    
    public void setOptionsView(OptionsView optionsView) {
        mainGrid.setWidget(0, 0, optionsView);
    }
    
    public void setTasksView(TasksView tasksView) {
        toolGrid.setWidget(0, 2, tasksView);
    }
    
    public void setToolBarView(ToolBarView toolBarView) {
        layoutGrid.setWidget(1, 0, toolBarView);
    }
    
    public void setNotificationsView(NotificationsView notificationsView) {
        mainGrid.setWidget(2, 0, notificationsView);
    }
}
