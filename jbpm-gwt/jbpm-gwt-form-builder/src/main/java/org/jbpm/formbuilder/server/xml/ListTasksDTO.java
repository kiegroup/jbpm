package org.jbpm.formbuilder.server.xml;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;

import org.jbpm.formbuilder.shared.task.TaskRef;

@XmlType(name="tasks") public class ListTasksDTO {

    @XmlElement @XmlList private List<TaskRefDTO> task;
    
    public ListTasksDTO(List<TaskRef> tasks) {
        for (TaskRef ref : tasks) {
            task.add(new TaskRefDTO(ref));
        }
    }
    
    public void setTask(List<TaskRefDTO> task) {
        this.task = task;
    }
    
    public List<TaskRefDTO> getTask() {
        return task;
    }
}
