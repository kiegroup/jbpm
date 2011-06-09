package org.jbpm.formbuilder.client.toolbar;

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;

public class ToolBarView extends AbsolutePanel {

    public ToolBarView() {
        setSize("100%", "100%");
        Grid grid = new Grid(1, 1);
        grid.setBorderWidth(2);
        grid.setSize("100%", "100%");
        grid.setWidget(0, 0, new HTML("Reserved for toolbar data (like quicksave button or quickundo)"));
        add(grid);
    }
}
