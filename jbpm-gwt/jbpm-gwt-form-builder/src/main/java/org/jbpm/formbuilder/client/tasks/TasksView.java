package org.jbpm.formbuilder.client.tasks;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;

public class TasksView extends AbsolutePanel {

    public TasksView() {
        setSize("100%", "100%");
        Grid grid = new Grid(1, 1);
        grid.setBorderWidth(2);
        grid.setSize("100%", "100%");
        grid.setWidget(0, 0, new HTML("Reserved for process definitions and task definitions data"));
        add(grid);
    }
}
