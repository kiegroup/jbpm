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
import java.util.List;

public class MockTaskDefinitionService implements TaskDefinitionService {

    private List<TaskRef> tasks = new ArrayList<TaskRef>();
    
    public MockTaskDefinitionService() {
        TaskRef task1 = new TaskRef();
        task1.setTaskId("task1");
        task1.addInput("input1", "${hey}");
        task1.addInput("input2", "${why}");
        task1.addOutput("output1", "");
        task1.addOutput("output2", "");
        tasks.add(task1);
        TaskRef task2 = new TaskRef();
        task2.addInput("input3", "${hey}");
        task2.addInput("input4", "${why}");
        task2.addOutput("output3", "");
        task2.addOutput("output4", "");
        tasks.add(task2);
    }
    
    public List<TaskRef> query(String pkgName, String filter) {
        return new ArrayList<TaskRef>(tasks);
    }

    public void update(TaskRef task) {
        boolean updated = false;
        for (int i = 0; i < tasks.size(); i++) {
            TaskRef currTask = tasks.get(i);
            if (task.getTaskId().equals(currTask.getTaskId())) {
                tasks.set(i, task);
                updated = true;
                break;
            }
        }
        if (!updated) {
            throw new RuntimeException("Not updated " + task);
        }
    }
}
