package org.jbpm.formbuilder.server.task;

import java.util.LinkedList;
import java.util.List;

import org.jbpm.formbuilder.shared.task.TaskRef;

public class TaskRepoHelper {

    List<TaskRef> tasks = new LinkedList<TaskRef>();
    
    public void clear() {
        tasks.clear();
    }
    
    public void addTask(TaskRef task) {
        tasks.add(task);
    }
    
    public List<TaskRef> getTasks() {
        return tasks;
    }

    public void addOutput(String processInputName, String id) {
        for (TaskRef task : tasks) {
            if (task.getTaskName().equals(processInputName)) {
                task.addOutput(id, "${" + id + "}");
                return;
            }
        }
        TaskRef ref = new TaskRef();
        ref.setTaskId(processInputName);
        ref.addOutput(id, "${" + id + "}");
        tasks.add(ref);
    }
}
