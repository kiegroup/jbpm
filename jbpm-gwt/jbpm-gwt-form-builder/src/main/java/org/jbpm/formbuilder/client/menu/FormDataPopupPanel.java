package org.jbpm.formbuilder.client.menu;

import org.jbpm.formbuilder.client.bus.FormDataPopulatedEvent;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class FormDataPopupPanel extends PopupPanel {

    private final EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
    
    private final ListBox enctype = new ListBox(false);
    private final ListBox method = new ListBox(false);
    private final TextBox action = new TextBox();
    private final TextBox taskId = new TextBox();
    private final TextBox name = new TextBox();
    private final TextArea documentation = new TextArea();
    
    public FormDataPopupPanel() {
        this(false);
    }
    
    public FormDataPopupPanel(boolean showForSavingForm) {
        super(true);

        VerticalPanel vPanel = new VerticalPanel();
        vPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        Grid grid = new Grid(6, 2);
        
        enctype.addItem("multipart/form-data");
        enctype.addItem("application/x-www-form-urlencoded");
        enctype.setSelectedIndex(0);
        
        action.setValue("complete");
        
        method.addItem("POST");
        method.addItem("GET");
        method.setSelectedIndex(0);

        if (showForSavingForm) {
            method.setEnabled(false);
            enctype.setEnabled(false);
            action.setEnabled(false);
            taskId.setEnabled(false);
            documentation.setCharacterWidth(30);
            documentation.setVisibleLines(4);
            grid.setWidget(0, 0, new Label("Check-In comment:"));
            grid.setWidget(0, 1, documentation);
        } else {
            grid.setWidget(0, 0, new HTML("&nbsp;"));
            grid.setWidget(0, 1, new HTML("&nbsp;"));
        }
        
        
        grid.setWidget(1, 0, new Label("Action:"));
        grid.setWidget(1, 1, action);
        grid.setWidget(2, 0, new Label("Method:"));
        grid.setWidget(2, 1, method);
        grid.setWidget(3, 0, new Label("Enctype:"));
        grid.setWidget(3, 1, enctype);
        grid.setWidget(4, 0, new Label("Task ID:"));
        grid.setWidget(4, 1, taskId);
        grid.setWidget(5, 0, new Label("Name:"));
        grid.setWidget(5, 1, name);
        
        vPanel.add(grid);
        vPanel.add(new Button("Apply", new ClickHandler() {
            public void onClick(ClickEvent event) {
                FormDataPopulatedEvent formEvent = new FormDataPopulatedEvent(action.getValue(), 
                        method.getValue(method.getSelectedIndex()), taskId.getValue(), 
                        enctype.getValue(enctype.getSelectedIndex()), name.getValue());
                bus.fireEvent(formEvent);
                hide();
            }
        }));
        
        add(vPanel);
        
    }
    
    public void setTaskId(String taskId) {
        this.taskId.setValue(taskId);
    }

    public void setEnctype(String enctype) {
        for (int index = 0; index < this.enctype.getItemCount(); index++) {
            if (this.enctype.getValue(index).equals(enctype)) {
                this.enctype.setSelectedIndex(index);
                break;
            }
            
        }
    }

    public void setMethod(String method) {
        for (int index = 0; index < this.method.getItemCount(); index++) {
            if (this.method.getValue(index).equals(method)) {
                this.method.setSelectedIndex(index);
                break;
            }
        }
    }

    public void setAction(String action) {
        this.action.setValue(action);
    }

    public void setName(String name) {
        this.name.setValue(name);
    }
    
    public String getFormName() {
        return name.getValue();
    }
    
    public String getAction() {
        return action.getValue();
    }
    
    public String getTaskId() {
        return taskId.getValue();
    }
    
    public String getMethod() {
        return method.getValue(method.getSelectedIndex());
    }
    
    public String getEnctype() {
        return enctype.getValue(enctype.getSelectedIndex());
    }
    
    public String getDocumentation() {
        return documentation.getValue();
    }
}
