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
package org.jbpm.formbuilder.server.task;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jbpm.formbuilder.shared.task.TaskPropertyRef;
import org.jbpm.formbuilder.shared.task.TaskRef;

public class TaskRepoHelper {

    Map<String, TaskRef> tasksMap = new HashMap<String, TaskRef>();
    
    List<TaskRef> tasks = new LinkedList<TaskRef>();
    
    public void clear() {
        tasks.clear();
    }
    
    public void addTask(TaskRef task) {
        TaskRef oldTask = tasksMap.get(task.getTaskName());
        if (oldTask != null) {
            for (TaskPropertyRef input : task.getInputs()) {
                oldTask.addInput(input.getName(), input.getSourceExpresion());
            }
            for (TaskPropertyRef output : task.getOutputs()) {
                oldTask.addOutput(output.getName(), output.getSourceExpresion());
            }
            Map<String, String> metaData = oldTask.getMetaData();
            metaData.putAll(task.getMetaData());
            oldTask.setMetaData(metaData);
        } else {
            tasks.add(task);
        }
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
