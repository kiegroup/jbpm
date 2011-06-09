package org.jbpm.formbuilder.shared.task;

import java.util.ArrayList;
import java.util.List;

public class TaskRef {

    private String taskId;
    private List<TaskPropertyRef> inputs = new ArrayList<TaskPropertyRef>();
    private List<TaskPropertyRef> outputs = new ArrayList<TaskPropertyRef>();
    
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
            if (key.equals(ref.getName())) {
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
            if (key.equals(ref.getName())) {
                return ref;
            }
        }
        return null;
    }
    
    public Object removeOutput(String key) {
        return this.outputs.remove(getOutput(key));
    }
}
