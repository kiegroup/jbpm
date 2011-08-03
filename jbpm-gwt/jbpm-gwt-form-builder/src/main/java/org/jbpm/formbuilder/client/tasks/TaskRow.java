package org.jbpm.formbuilder.client.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.common.handler.EventHelper;
import org.jbpm.formbuilder.common.handler.RightClickHandler;
import org.jbpm.formbuilder.shared.task.TaskPropertyRef;
import org.jbpm.formbuilder.shared.task.TaskRef;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class TaskRow extends FocusPanel {
    
    private final List<HandlerRegistration> rclickRegs = new ArrayList<HandlerRegistration>();

    private final TaskRef ioRef;
    private final VerticalPanel panel = new VerticalPanel();
    
    private final HandlerRegistration focus;
    private final HandlerRegistration blur;
    
    private final Grid inputsGrid = new Grid();
    private final Grid outputsGrid = new Grid();
    private final Grid metaDataGrid = new Grid();
    
    public TaskRow(TaskRef ioRef, boolean even) {
        this.ioRef = ioRef;
        addStyleName(even ? "even" : "odd");
        panel.add(new Label("Process ID: " + ioRef.getProcessId()));
        panel.add(new Label("Task ID: " + ioRef.getTaskId()));
        this.focus = addFocusHandler(new FocusHandler() {
            public void onFocus(FocusEvent event) {
                showInputs();
                showOutputs();
                showMetaData();
            }
        });
        this.blur = addBlurHandler(new BlurHandler() {
            public void onBlur(BlurEvent event) {
                hideInputs();
                hideOutputs();
                hideMetaData();
            }
        });
        add(panel);
    }
    
    public HandlerRegistration getFocus() {
        return focus;
    }
    
    public HandlerRegistration getBlur() {
        return blur;
    }
    
    protected VerticalPanel getPanel() {
        return panel;
    }
    
    public TaskRef getIoRef() {
        return ioRef;
    }
    
    @Override
    public void onBrowserEvent(Event event) {
        EventHelper.onBrowserEvent(this, event);
    }
    
    public HandlerRegistration addRightClickHandler(final RightClickHandler handler) {
        HandlerRegistration reg = EventHelper.addRightClickHandler(this, handler);
        rclickRegs.add(reg);
        return reg;
    }
    
    public void clearRightClickHandlers() {
        for (HandlerRegistration reg : rclickRegs) {
            reg.removeHandler();
        }
        rclickRegs.clear();
    }
    
    protected void showInputs() {
        List<TaskPropertyRef> inputs = this.ioRef.getInputs();
        inputsGrid.resize(inputs.size(), 2);
        for (int index = 0; index < inputs.size(); index++) {
            TaskPropertyRef input = inputs.get(index);
            inputsGrid.setWidget(index, 0, new HTML("<strong>input name:</strong>" + input.getName()));
            inputsGrid.setWidget(index, 1, new HTML("<strong>input expression:</strong>" + input.getSourceExpresion()));
        }
        panel.add(inputsGrid);
    }
    
    protected void showOutputs() {
        List<TaskPropertyRef> outputs = this.ioRef.getOutputs();
        outputsGrid.resize(outputs.size(), 2);
        for (int index = 0; index < outputs.size(); index++) {
            TaskPropertyRef output = outputs.get(index);
            outputsGrid.setWidget(index, 0, new HTML("<strong>output name:</strong>" + output.getName()));
            outputsGrid.setWidget(index, 1, new HTML("<strong>output expression:</strong>" + output.getSourceExpresion()));
        }
        panel.add(outputsGrid);
    }
    
    protected void showMetaData() {
        Map<String, String> metaData = this.ioRef.getMetaData();
        metaDataGrid.resize(metaData.size(), 2);
        List<Map.Entry<String, String>> entries = new ArrayList<Map.Entry<String, String>>(metaData.entrySet());
        for (int index = 0; index < entries.size(); index++) {
            Map.Entry<String, String> entry = entries.get(index);
            metaDataGrid.setWidget(index, 0, new HTML("<strong>meta data name:</strong>" + entry.getKey()));
            metaDataGrid.setWidget(index, 1, new HTML("<strong>meta data value:</strong>" + entry.getValue()));
        }
        panel.add(metaDataGrid);
    }
    
    protected void hideInputs() {
        panel.remove(inputsGrid);
    }
    
    protected void hideOutputs() {
        panel.remove(outputsGrid);
    }
    
    protected void hideMetaData() {
        panel.remove(metaDataGrid);
    }
}
