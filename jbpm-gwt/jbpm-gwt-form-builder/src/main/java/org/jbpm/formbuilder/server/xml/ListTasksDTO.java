package org.jbpm.formbuilder.server.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.jbpm.formbuilder.shared.task.TaskRef;

@XmlRootElement(name="tasks") public class ListTasksDTO {

    private List<TaskRefDTO> _task = new ArrayList<TaskRefDTO>();
    
    public ListTasksDTO() {
        // jaxb needs a default constructor
    }
    
    public ListTasksDTO(List<TaskRef> tasks) {
        if (tasks != null) {
            for (TaskRef ref : tasks) {
                _task.add(new TaskRefDTO(ref));
            }
        }
    }
    
    public void setTask(List<TaskRefDTO> task) {
        this._task = task;
    }
    
    @XmlElement
    public List<TaskRefDTO> getTask() {
        return _task;
    }
}
