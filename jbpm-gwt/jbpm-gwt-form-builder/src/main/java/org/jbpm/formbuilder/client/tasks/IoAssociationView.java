package org.jbpm.formbuilder.client.tasks;

import java.util.List;

import org.jbpm.formbuilder.shared.task.TaskRef;

public interface IoAssociationView {

    interface Presenter {

        TaskRow newTaskRow(TaskRef task, boolean even);

        void addQuickFormHandling(TaskRow row);
        
    };
    
    SearchFilterView getSearch();

    void setTasks(List<TaskRef> tasks);

    void setSelectedTask(TaskRef selectedTask);

    void disableSearch();

}
