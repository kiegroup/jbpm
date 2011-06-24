package org.jbpm.formbuilder.server.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;

import org.jbpm.formbuilder.shared.task.TaskPropertyRef;
import org.jbpm.formbuilder.shared.task.TaskRef;

@XmlType(name="task")
public class TaskRefDTO {

    @XmlAttribute private String processId;
    @XmlAttribute private String taskName;
    @XmlAttribute private String taskId;
    @XmlElement @XmlList private List<PropertyDTO> input = new ArrayList<PropertyDTO>();
    @XmlElement @XmlList private List<PropertyDTO> output = new ArrayList<PropertyDTO>();
    @XmlElement @XmlList private List<MetaDataDTO> metaData = new ArrayList<MetaDataDTO>();
    
    public TaskRefDTO(TaskRef task) {
        this.processId = task.getProcessId();
        this.taskName = task.getTaskName();
        this.taskId = task.getTaskId();
        for (TaskPropertyRef ref : task.getInputs()) {
            input.add(new PropertyDTO(ref));
        }
        for (TaskPropertyRef ref : task.getOutputs()) {
            output.add(new PropertyDTO(ref));
        }
        for (Map.Entry<String, String> entry : task.getMetaData().entrySet()) {
            metaData.add(new MetaDataDTO(entry));
        }
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public List<PropertyDTO> getInput() {
        return input;
    }

    public void setInput(List<PropertyDTO> input) {
        this.input = input;
    }

    public List<PropertyDTO> getOutput() {
        return output;
    }

    public void setOutput(List<PropertyDTO> output) {
        this.output = output;
    }

    public List<MetaDataDTO> getMetaData() {
        return metaData;
    }

    public void setMetaData(List<MetaDataDTO> metaData) {
        this.metaData = metaData;
    }
}
