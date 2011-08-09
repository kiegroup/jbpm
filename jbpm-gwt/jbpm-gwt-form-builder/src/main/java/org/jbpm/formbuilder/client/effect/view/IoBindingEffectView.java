/**
 * Copyright 2011 JBoss Inc 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.formbuilder.client.effect.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.client.bus.UndoableEvent;
import org.jbpm.formbuilder.client.bus.UndoableHandler;
import org.jbpm.formbuilder.client.effect.IoBindingEffect;
import org.jbpm.formbuilder.client.messages.Constants;
import org.jbpm.formbuilder.client.resources.FormBuilderGlobals;
import org.jbpm.formbuilder.shared.rep.Data;
import org.jbpm.formbuilder.shared.task.TaskPropertyRef;

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
import com.google.gwt.user.client.ui.VerticalPanel;

public class IoBindingEffectView extends PopupPanel {

    private final Constants i18n = FormBuilderGlobals.getInstance().getI18n();
    private final EventBus bus = FormBuilderGlobals.getInstance().getEventBus();
    private final ListBox inputList;
    private final ListBox outputList;
    private final IoBindingEffect effect;
    
    public IoBindingEffectView(IoBindingEffect ioBindingEffect) {
        this.effect = ioBindingEffect;
        HTML title = new HTML("<strong>" + i18n.SelectIoConfig() + "</strong>");
        title.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        
        this.inputList = createVisualList(effect.getItem().getInput(), effect.getIoRef().getInputs());
        this.outputList = createVisualList(effect.getItem().getOutput(), effect.getIoRef().getOutputs());
        
        Button applyButton = new Button(i18n.ConfirmButton());
        applyButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                onApplyBinding();
            }
        });
        Button cancelButton = new Button(i18n.CancelButton());
        cancelButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                hide();
            }
        });
        
        Grid grid = new Grid(3,2);
        grid.setWidget(0, 0, new Label(i18n.LabelInput()));
        grid.setWidget(0, 1, inputList);
        grid.setWidget(1, 0, new Label(i18n.LabelOutput()));
        grid.setWidget(1, 1, outputList);
        grid.setWidget(2, 0, applyButton);
        grid.setWidget(2, 1, cancelButton);
        
        VerticalPanel vPanel = new VerticalPanel();
        vPanel.add(title);
        vPanel.add(grid);
        setWidget(vPanel);
    }

    private void onApplyBinding() {
        Map<String, Object> dataSnapshot = new HashMap<String, Object>();
        String selectedInput = inputList.getValue(inputList.getSelectedIndex());
        if (selectedInput != null && !"".equals(selectedInput)) {
            dataSnapshot.put("newInput", effect.getIoRef().getInput(selectedInput));
        } else {
            dataSnapshot.put("newInput", null);
        }
        dataSnapshot.put("oldInput", effect.getInput());
        String selectedOutput = outputList.getValue(outputList.getSelectedIndex());
        if (selectedOutput != null && !"".equals(selectedOutput)) {
            dataSnapshot.put("newOutput", effect.getIoRef().getOutput(selectedOutput));
        } else {
            dataSnapshot.put("newOutput", null);
        }
        dataSnapshot.put("oldOutput", effect.getOutput());
        bus.fireEvent(new UndoableEvent(dataSnapshot, new UndoableHandler() {
            public void onEvent(UndoableEvent event) { }
            public void undoAction(UndoableEvent event) {
                effect.setInput((TaskPropertyRef) event.getData("oldInput"));
                effect.setOutput((TaskPropertyRef) event.getData("oldOutput"));
                effect.fire();
            }
            public void doAction(UndoableEvent event) {
                effect.setInput((TaskPropertyRef) event.getData("newInput"));
                effect.setOutput((TaskPropertyRef) event.getData("newOutput"));
                effect.fire();
            }
        }));
        hide();
    }
    
    private ListBox createVisualList(Data ioData, List<TaskPropertyRef> ioList) {
        ListBox inputList = new ListBox();
        String selectedInputName = ioData == null ? null : ioData.getName();
        inputList.addItem("", "");
        for (TaskPropertyRef io : ioList) {
            inputList.addItem(io.getName() + " (" + io.getSourceExpresion() + ")", io.getName());
            if (selectedInputName != null && io.getName().equals(selectedInputName)) {
                inputList.setSelectedIndex(inputList.getItemCount() - 1);
            }
        }
        return inputList;
    }
}
