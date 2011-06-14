package org.jbpm.formbuilder.shared.task;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.formbuilder.shared.rep.FormRepresentation;

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

    public FormRepresentation getAssociatedForm(TaskRef task) {
        // TODO Auto-generated method stub
        return null;
    }

}
