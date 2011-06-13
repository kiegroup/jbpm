package org.jbpm.formbuilder.client.effect;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.formbuilder.client.bus.TaskSelectedEvent;
import org.jbpm.formbuilder.client.bus.TaskSelectedHandler;
import org.jbpm.formbuilder.client.bus.UndoableEvent;
import org.jbpm.formbuilder.client.bus.UndoableEventHandler;
import org.jbpm.formbuilder.client.form.FBFormItem;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.client.resources.FormBuilderResources;
import org.jbpm.formbuilder.shared.rep.Formatter;
import org.jbpm.formbuilder.shared.rep.InputData;
import org.jbpm.formbuilder.shared.rep.OutputData;
import org.jbpm.formbuilder.shared.task.TaskPropertyRef;
import org.jbpm.formbuilder.shared.task.TaskRef;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class TaskRelationEffect extends FBFormEffect {

    private final EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
    
    private TaskRef selectedTask = null;
    private TaskPropertyRef input = null;
    private TaskPropertyRef output = null;
    
    public TaskRelationEffect() {
        super(createImage(), true);
        bus.addHandler(TaskSelectedEvent.TYPE, new TaskSelectedHandler() {
            public void onSelectedTask(TaskSelectedEvent event) {
                if (event.getSelectedTask() != null) {
                    selectedTask = event.getSelectedTask();
                }
            }
        });
    }

    public static Image createImage() {
        Image img = new Image(FormBuilderResources.INSTANCE.taskRelatedEffect());
        img.setAltText("Edit Task information associated with this item");
        img.setTitle("Edit Task information associated with this item");
        return img;
    }
    
    @Override
    protected void createStyles() {
        FBFormItem item = getItem();
        InputData in = new InputData();
        in.setName(this.input.getName());
        in.setValue(this.input.getSourceExpresion());
        in.setMimeType("multipart/form-data");
        in.setFormatter(new Formatter() {
            public Object format(Object object) {
                return object;
            }
        });
        item.setInput(in);
        OutputData out = new OutputData();
        out.setName(this.output.getName());
        out.setValue(this.output.getSourceExpresion());
        out.setMimeType("multipart/form-data");
        out.setFormatter(new Formatter() {
            public Object format(Object object) {
                return object;
            }
        });
        item.setOutput(out);
    }

    @Override
    public PopupPanel createPanel() {
        final PopupPanel panel = new PopupPanel();

        HTML title = new HTML("<strong>Item references to selected task</strong>");
        title.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        final ListBox inputList = new ListBox();
        for (TaskPropertyRef input : selectedTask.getInputs()) {
            inputList.addItem(input.getName() + " (" + input.getSourceExpresion() + ")", input.getName());
        }
        final ListBox outputList = new ListBox();
        for (TaskPropertyRef output : selectedTask.getOutputs()) {
            outputList.addItem(output.getName() + " (" + output.getSourceExpresion() + ")", output.getName());
        }

        Grid grid = new Grid(2,2);
        grid.setWidget(0, 0, new Label("Input:"));
        grid.setWidget(0, 1, inputList);
        grid.setWidget(1, 0, new Label("Output:"));
        grid.setWidget(1, 1, outputList);
        
        Button applyButton = new Button("apply");
        applyButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                Map<String, Object> dataSnapshot = new HashMap<String, Object>();
                dataSnapshot.put("newInput", selectedTask.getInput(inputList.getValue(inputList.getSelectedIndex())));
                dataSnapshot.put("oldInput", input);
                dataSnapshot.put("newOutput", selectedTask.getOutput(outputList.getValue(outputList.getSelectedIndex())));
                dataSnapshot.put("oldOutput", output);
                bus.fireEvent(new UndoableEvent(dataSnapshot, new UndoableEventHandler() {
                    public void onEvent(UndoableEvent event) { }
                    public void undoAction(UndoableEvent event) {
                        input = (TaskPropertyRef) event.getData("oldInput");
                        output = (TaskPropertyRef) event.getData("oldOutput");
                        createStyles();
                    }
                    public void doAction(UndoableEvent event) {
                        input = (TaskPropertyRef) event.getData("newInput");
                        output = (TaskPropertyRef) event.getData("newOutput");
                        createStyles();
                    }
                }));
                panel.hide();
            }
        });
        
        VerticalPanel vPanel = new VerticalPanel();
        vPanel.add(title);
        vPanel.add(grid);
        vPanel.add(applyButton);
        
        panel.setWidget(vPanel);
        return panel;
    }
    
    @Override
    public boolean isValidForItem(FBFormItem item) {
        return this.selectedTask != null;
    }
}
