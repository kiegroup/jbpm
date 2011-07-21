package org.jbpm.formbuilder.client.tasks;

import java.util.List;

import org.jbpm.formbuilder.client.bus.ui.TaskSelectedEvent;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.common.handler.RightClickEvent;
import org.jbpm.formbuilder.common.handler.RightClickHandler;
import org.jbpm.formbuilder.shared.task.TaskRef;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class SearchResultsView extends VerticalPanel {

    private final EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
    
    public SearchResultsView() {
    }

    public void setTasks(List<TaskRef> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            clear();
            add(new Label("No IO references found"));
        } else {
            clear();
            boolean even = false;
            for (TaskRef task : tasks) {
                final TaskRow row = new TaskRow(task, even);
                even = !even;
                row.addRightClickHandler(new RightClickHandler() {
                    public void onRightClick(RightClickEvent event) {
                        PopupPanel panel = new PopupPanel(true);
                        panel.setPopupPosition(event.getX(), event.getY());
                        MenuBar bar = new MenuBar();
                        bar.addItem("Select IO object", new Command() {
                            public void execute() {
                                bus.fireEvent(new TaskSelectedEvent(row.getIoRef()));
                            }
                        });
                        panel.add(bar);
                        panel.show();
                    }
                });
                add(row);
                
            }
        }
    }

    public void setSelectedTask(TaskRef selectedTask) {
        TaskRow selectedRow = null;
        for (Widget widget : this) {
            TaskRow row = (TaskRow) widget;
            if (row.getIoRef().equals(selectedTask)) {
                selectedRow = row;
                break;
            }
        }
        clear();
        selectedRow.getFocus().removeHandler();
        selectedRow.getBlur().removeHandler();
        selectedRow.showInputs();
        selectedRow.showOutputs();
        selectedRow.showMetaData();
        add(selectedRow);
    }
}
