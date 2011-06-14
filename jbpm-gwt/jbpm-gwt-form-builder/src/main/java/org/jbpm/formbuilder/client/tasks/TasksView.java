package org.jbpm.formbuilder.client.tasks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.bus.TaskSelectedEvent;
import org.jbpm.formbuilder.client.bus.ui.TaskNameFilterEvent;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.shared.task.TaskPropertyRef;
import org.jbpm.formbuilder.shared.task.TaskRef;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class TasksView extends AbsolutePanel {

    private VerticalPanel panel = new VerticalPanel();
    private final EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
    
    private TextBox taskName = new TextBox();
    
    private VerticalPanel dataPanel = new VerticalPanel();
    private PopupPanel taskOptionsPanel = new PopupPanel();
    private VerticalPanel taskInputPanel = new VerticalPanel();
    private VerticalPanel taskOutputPanel = new VerticalPanel();
    private List<TaskPropertyRef> taskInputs = null;
    private List<TaskPropertyRef> taskOutputs = null;
    private Map<String, TaskRef> possibleTasks = new HashMap<String, TaskRef>();
    
    public TasksView() {
        setSize("100%", "100%");
        panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
        Grid grid = new Grid(1, 1);
        grid.setBorderWidth(2);
        grid.setSize("100%", "100%");
        grid.setWidget(0, 0, panel);
        add(grid);
        panel.add(taskName());
        HorizontalPanel ioTasks = new HorizontalPanel();
        ioTasks.add(taskInputs());
        ioTasks.add(new HTML("&nbsp;"));
        ioTasks.add(taskOutputs());
        panel.add(ioTasks);
        panel.add(data());
    }
    
    private HorizontalPanel taskName() {
        HorizontalPanel hPanel = new HorizontalPanel();
        hPanel.add(new Label("Task Name:"));
        taskName.addKeyPressHandler(new KeyPressHandler() {
            public void onKeyPress(KeyPressEvent event) {
                String newValue = taskName.getValue();
                System.out.println("Value changed to " + newValue);
                bus.fireEvent(new TaskNameFilterEvent(newValue));
            }
        });
        taskName.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                String newValue = taskName.getValue();
                TaskRef selectedTask = possibleTasks.get(newValue);
                bus.fireEvent(new TaskSelectedEvent(selectedTask));
                taskOptionsPanel.hide();
            }
        });
        hPanel.add(taskName);
        return hPanel;
    }
    
    private VerticalPanel taskInputs() {
        return taskInputPanel;
    }

    private VerticalPanel taskOutputs() {
        return taskOutputPanel;
    }
    
    private VerticalPanel data() {
        return dataPanel;
    }

    public void setTaskCombo(List<TaskRef> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            taskOptionsPanel.hide();
        } else {
            final ListBox taskSuggestions = new ListBox();
            taskSuggestions.addChangeHandler(new ChangeHandler() {
                public void onChange(ChangeEvent event) {
                    int index = taskSuggestions.getSelectedIndex();
                    String value = taskSuggestions.getItemText(index);
                    taskName.setValue(value, true);
                    taskOptionsPanel.hide();
                    TaskRef selectedTask = possibleTasks.get(value);
                    bus.fireEvent(new TaskSelectedEvent(selectedTask));
                }
            });
            taskSuggestions.clear();
            taskSuggestions.setVisibleItemCount(tasks.size() > 20 ? 20 : tasks.size());
            for (TaskRef task : tasks) {
                possibleTasks.put(task.getTaskId(), task);
                taskSuggestions.addItem(task.getTaskId(), task.getTaskId() + " (from " + task.getProcessId() + ")");
            }
            taskOptionsPanel.setPopupPosition(taskName.getAbsoluteLeft(), taskName.getAbsoluteTop() + taskName.getOffsetHeight());
            taskOptionsPanel.setPixelSize(taskName.getOffsetWidth(), tasks.size() * taskName.getOffsetHeight());
            taskOptionsPanel.setWidget(taskSuggestions);
            taskOptionsPanel.show();
        }
    }

    public void setTaskInputs(List<TaskPropertyRef> inputs) {
        this.taskInputs = inputs;
        if (taskInputPanel.getWidgetCount() != 0) {
            taskInputPanel.clear();
        }
        taskInputPanel.add(new HTML("<strong>Inputs</strong>"));
        if (inputs != null) {
            for (TaskPropertyRef input : taskInputs) {
                Label label = new Label(input.getName() + " = " + input.getSourceExpresion());
                taskInputPanel.add(label);
            }
        }
    }

    public void setTaskOutputs(List<TaskPropertyRef> outputs) {
        this.taskOutputs = outputs;
        if (taskOutputPanel.getWidgetCount() != 0) {
            taskOutputPanel.clear();
        }
        taskOutputPanel.add(new HTML("<strong>Outputs</strong>"));
        if (outputs != null) {
            for (TaskPropertyRef output : taskOutputs) {
                Label label = new Label(output.getName() + " = " + output.getSourceExpresion());
                taskOutputPanel.add(label);
            }
        }
    }
    
    public void setData(Map<String, String> data) {
        if (dataPanel.getWidgetCount() != 0) {
            dataPanel.clear();
        }
        Grid grid = new Grid(data.size() + 1, 2);
        grid.setWidget(0, 0, new HTML("<strong>Property name</strong>"));
        grid.setWidget(0, 0, new HTML("<strong>Property value</strong>"));
        int i = 1;
        for (Map.Entry<String, String> entry : data.entrySet()) {
            grid.setWidget(i, 0, new Label(entry.getKey()));
            grid.setWidget(i, 1, new Label(entry.getValue()));
            i++;
        }
    }
}
