package org.jbpm.formbuilder.client.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.messages.Constants;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
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
    
    private final Constants i18n = FormBuilderGlobals.getInstance().getI18n();
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
        panel.add(new Label(i18n.FormProcessId() + " " + ioRef.getProcessId()));
        panel.add(new Label(i18n.FormTaskId() + " " + ioRef.getTaskId()));
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
        inputsGrid.resize(inputs.size(), 4);
        for (int index = 0; index < inputs.size(); index++) {
            TaskPropertyRef input = inputs.get(index);
            inputsGrid.setWidget(index, 0, new HTML("<strong>" + i18n.InputNameLabel() + ":</strong>"));
            inputsGrid.setWidget(index, 1, new Label(input.getName()));
            inputsGrid.setWidget(index, 2, new HTML("<strong>" + i18n.InputExpressionLabel() + ":</strong>"));
            inputsGrid.setWidget(index, 3, new Label(input.getSourceExpresion()));
        }
        panel.add(inputsGrid);
    }
    
    protected void showOutputs() {
        List<TaskPropertyRef> outputs = this.ioRef.getOutputs();
        outputsGrid.resize(outputs.size(), 4);
        for (int index = 0; index < outputs.size(); index++) {
            TaskPropertyRef output = outputs.get(index);
            outputsGrid.setWidget(index, 0, new HTML("<strong>" + i18n.OutputNameLabel() + ":</strong>"));
            outputsGrid.setWidget(index, 1, new Label(output.getName()));
            outputsGrid.setWidget(index, 2, new HTML("<strong>" + i18n.OutputExpressionLabel() + ":</strong>"));
            outputsGrid.setWidget(index, 3, new Label(output.getSourceExpresion()));
        }
        panel.add(outputsGrid);
    }
    
    protected void showMetaData() {
        Map<String, String> metaData = this.ioRef.getMetaData();
        metaDataGrid.resize(metaData.size(), 4);
        List<Map.Entry<String, String>> entries = new ArrayList<Map.Entry<String, String>>(metaData.entrySet());
        for (int index = 0; index < entries.size(); index++) {
            Map.Entry<String, String> entry = entries.get(index);
            metaDataGrid.setWidget(index, 0, new HTML("<strong>" + i18n.MetaDataNameLabel() + ":</strong>"));
            metaDataGrid.setWidget(index, 1, new Label(entry.getKey()));
            metaDataGrid.setWidget(index, 2, new HTML("<strong>" + i18n.MetaDataValueLabel() + ":</strong>"));
            metaDataGrid.setWidget(index, 3, new Label(entry.getValue()));
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
