package org.jbpm.formbuilder.client.tasks;

import java.util.List;

import org.jbpm.formbuilder.client.FormBuilderService;
import org.jbpm.formbuilder.client.bus.ui.TaskSelectedEvent;
import org.jbpm.formbuilder.client.bus.ui.UpdateFormViewEvent;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.common.handler.RightClickEvent;
import org.jbpm.formbuilder.common.handler.RightClickHandler;
import org.jbpm.formbuilder.common.panels.ConfirmDialog;
import org.jbpm.formbuilder.shared.rep.FormRepresentation;
import org.jbpm.formbuilder.shared.task.TaskRef;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class SearchResultsView extends VerticalPanel {

    private final EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
    private final FormBuilderService server = FormBuilderGlobals.getInstance().getService();
    
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
                        final PopupPanel panel = new PopupPanel(true);
                        panel.setPopupPosition(event.getX(), event.getY());
                        MenuBar bar = new MenuBar(true);
                        bar.addItem("Select IO object", new Command() {
                            public void execute() {
                                bus.fireEvent(new TaskSelectedEvent(row.getIoRef()));
                                panel.hide();
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
        if (selectedTask != null) {
            TaskRow selectedRow = null;
            for (Widget widget : this) {
                TaskRow row = (TaskRow) widget;
                if (row.getIoRef().equals(selectedTask)) {
                    selectedRow = row;
                    break;
                }
            }
            clear();
            if (selectedRow == null) {
                selectedRow = new TaskRow(selectedTask, true);
            }
            selectedRow.getFocus().removeHandler();
            selectedRow.getBlur().removeHandler();
            selectedRow.showInputs();
            selectedRow.showOutputs();
            selectedRow.showMetaData();
            selectedRow.clearRightClickHandlers();
            final TaskRow row = selectedRow;
            selectedRow.addRightClickHandler(new RightClickHandler() {
                public void onRightClick(final RightClickEvent event) {
                    final PopupPanel panel = new PopupPanel(true);
                    panel.setPopupPosition(event.getX(), event.getY());
                    MenuBar bar = new MenuBar(true);
                    bar.addItem("Quick Form from IO object", new Command() {
                        public void execute() {
                            ConfirmDialog conf = new ConfirmDialog("Warning: this will delete all the contents of" +
                                    " your current form to create a simple form with all inputs and outputs from " +
                                    "the task. Proceed?");
                            conf.addOkButtonHandler(new ClickHandler() {
                                public void onClick(ClickEvent event) {
                                    FormRepresentation form = server.toBasicForm(row.getIoRef());
                                    bus.fireEvent(new UpdateFormViewEvent(form));
                                }
                            });
                            conf.setPopupPosition(event.getX(), event.getY());
                            conf.show();
                            panel.hide();
                        }
                    });
                    panel.add(bar);
                    panel.show();
                }
            });
            add(selectedRow);
        }
    }
}
