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
package org.jbpm.formbuilder.shared.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskRef {

    private String processId;
    private String taskId;
    private List<TaskPropertyRef> inputs = new ArrayList<TaskPropertyRef>();
    private List<TaskPropertyRef> outputs = new ArrayList<TaskPropertyRef>();
    private Map<String, String> metaData = new HashMap<String, String>();
    
    public List<TaskPropertyRef> getInputs() {
        return inputs;
    }
    
    public void setInputs(List<TaskPropertyRef> inputs) {
        this.inputs = inputs;
    }
    
    public List<TaskPropertyRef> getOutputs() {
        return outputs;
    }
    
    public void setOutputs(List<TaskPropertyRef> outputs) {
        this.outputs = outputs;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
    
    public boolean addInput(String key, String value) {
        TaskPropertyRef tpRef = new TaskPropertyRef();
        tpRef.setName(key);
        tpRef.setSourceExpresion(value);
        return this.inputs.add(tpRef);
    }
    
    public TaskPropertyRef getInput(String key) {
        for (TaskPropertyRef ref : inputs) {
            if (key != null && key.equals(ref.getName())) {
                return ref;
            }
        }
        return null;
    }
    
    public Object removeInput(String key) {
        return this.inputs.remove(getInput(key));
    }
    
    public boolean addOutput(String key, String value) {
        TaskPropertyRef tpRef = new TaskPropertyRef();
        tpRef.setName(key);
        tpRef.setSourceExpresion(value);
        return this.outputs.add(tpRef);
    }
    
    public TaskPropertyRef getOutput(String key) {
        for (TaskPropertyRef ref : outputs) {
            if (key != null && key.equals(ref.getName())) {
                return ref;
            }
        }
        return null;
    }
    
    public Object removeOutput(String key) {
        return this.outputs.remove(getOutput(key));
    }

    public String getTaskName() {
        return this.taskId;
    }
    
    public void setProcessId(String processId) {
        this.processId = processId;
    }
    
    public String getProcessId() {
        return processId;
    }
    
    public void setMetaData(Map<String, String> metaData) {
        this.metaData = metaData;
    }
    
    public Map<String, String> getMetaData() {
        return metaData;
    }
}
