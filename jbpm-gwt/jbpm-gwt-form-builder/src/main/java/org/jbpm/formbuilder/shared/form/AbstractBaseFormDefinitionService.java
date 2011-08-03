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
package org.jbpm.formbuilder.shared.form;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.shared.rep.FormRepresentation;
import org.jbpm.formbuilder.shared.rep.Formatter;
import org.jbpm.formbuilder.shared.rep.InputData;
import org.jbpm.formbuilder.shared.rep.OutputData;
import org.jbpm.formbuilder.shared.rep.items.CompleteButtonRepresentation;
import org.jbpm.formbuilder.shared.rep.items.HeaderRepresentation;
import org.jbpm.formbuilder.shared.rep.items.LabelRepresentation;
import org.jbpm.formbuilder.shared.rep.items.TableRepresentation;
import org.jbpm.formbuilder.shared.rep.items.TextFieldRepresentation;
import org.jbpm.formbuilder.shared.task.TaskPropertyRef;
import org.jbpm.formbuilder.shared.task.TaskRef;

public abstract class AbstractBaseFormDefinitionService implements FormDefinitionService {

    private static final String FORM_ID_PREFIX = "formDefinition_";
    private static final String ITEM_ID_PREFIX = "formItemDefinition_";
    
    /**
     * @param form FormRepresentation with name to be changed
     * @return true if its an update, false if it is an insert
     */
    protected boolean updateFormName(FormRepresentation form) {
        if (form.getName() == null || "null".equals(form.getName()) || "".equals(form.getName())) {
            form.setName(FORM_ID_PREFIX + System.currentTimeMillis());
            return false;
        } else if (!form.getName().startsWith(FORM_ID_PREFIX)){
            form.setName(FORM_ID_PREFIX + form.getName());
            return false;
        }
        return true;
    }
    
    protected boolean updateItemName(String formItemName, StringBuilder returnName) {
        if (formItemName == null || "null".equals(formItemName) || "".equals(formItemName)) {
            returnName.append(ITEM_ID_PREFIX).append(System.currentTimeMillis());
            return false;
        } else if (!formItemName.startsWith(ITEM_ID_PREFIX)){
            returnName.append(ITEM_ID_PREFIX).append(formItemName);
            return false;
        }
        returnName.append(formItemName);
        return true;
    }
    
    protected boolean isItemName(String assetId) {
        return assetId.startsWith(ITEM_ID_PREFIX);
    }
    
    protected boolean isFormName(String assetId) {
        return assetId.startsWith(FORM_ID_PREFIX);
    }
    
    public FormRepresentation createFormFromTask(TaskRef task) {
        if (task == null) {
            return null;
        }
        FormRepresentation form = new FormRepresentation();
        if (task.getTaskId() != null) {
            HeaderRepresentation header = new HeaderRepresentation();
            header.setValue("Task: " + task.getTaskId());
            form.addFormItem(header);
        }
        List<TaskPropertyRef> inputs = task.getInputs();
        if (inputs != null && !inputs.isEmpty()) {
            TableRepresentation tableOfInputs = new TableRepresentation(inputs.size(), 2);
            for (int index = 0; index < inputs.size(); index++) {
                TaskPropertyRef input = inputs.get(index);
                LabelRepresentation labelName = new LabelRepresentation();
                labelName.setValue(input.getName());
                tableOfInputs.setElement(index, 0, labelName);
                LabelRepresentation labelValue = new LabelRepresentation();
                InputData data = new InputData();
                data.setName(input.getName());
                data.setValue(input.getSourceExpresion());
                data.setMimeType("multipart/form-data");
                data.setFormatter(new Formatter() {
                    public Object format(Object object) {
                        return object;
                    }
                    public Map<String, Object> getDataMap() {
                        return new HashMap<String, Object>();
                    }
                });
                labelValue.setInput(data);
                tableOfInputs.setElement(index, 1, labelValue);
            }
            LabelRepresentation labelInputs = new LabelRepresentation();
            labelInputs.setValue("Inputs:");
            form.addFormItem(labelInputs);
            form.addFormItem(tableOfInputs);
        }
        List<TaskPropertyRef> outputs = task.getOutputs();
        if (outputs != null && !outputs.isEmpty()) {
            TableRepresentation tableOfOutputs = new TableRepresentation(outputs.size(), 2);
            for (int index = 0; index < outputs.size(); index++) {
                TaskPropertyRef output = outputs.get(index);
                LabelRepresentation labelName = new LabelRepresentation();
                labelName.setValue(output.getName());
                tableOfOutputs.setElement(index, 0, labelName);
                TextFieldRepresentation textField = new TextFieldRepresentation();
                OutputData data = new OutputData();
                data.setName(output.getName());
                data.setValue(output.getSourceExpresion());
                data.setMimeType("multipart/form-data");
                data.setFormatter(new Formatter() {
                    public Object format(Object object) {
                        return object;
                    }
                    public Map<String, Object> getDataMap() {
                        return new HashMap<String, Object>();
                    }
                });
                textField.setOutput(data);
                tableOfOutputs.setElement(index, 1, textField);
            }
            LabelRepresentation labelOutputs = new LabelRepresentation();
            labelOutputs.setValue("Outputs:");
            form.addFormItem(labelOutputs);
            form.addFormItem(tableOfOutputs);
        }
        CompleteButtonRepresentation completeButton = new CompleteButtonRepresentation();
        completeButton.setText("Complete");
        form.addFormItem(completeButton);
        return form;
    }
}
