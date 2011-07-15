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
package org.jbpm.formbuilder.server.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.jbpm.formbuilder.shared.task.TaskPropertyRef;
import org.jbpm.formbuilder.shared.task.TaskRef;

public class TaskRefDTO {

    private String _processId;
    private String _taskName;
    private String _taskId;
    private List<PropertyDTO> _input = new ArrayList<PropertyDTO>();
    private List<PropertyDTO> _output = new ArrayList<PropertyDTO>();
    private List<MetaDataDTO> _metaData = new ArrayList<MetaDataDTO>();
    
    public TaskRefDTO() {
        // jaxb needs a default constructor
    }
    
    public TaskRefDTO(TaskRef task) {
        this._processId = task.getProcessId();
        this._taskName = task.getTaskName();
        this._taskId = task.getTaskId();
        for (TaskPropertyRef ref : task.getInputs()) {
            _input.add(new PropertyDTO(ref));
        }
        for (TaskPropertyRef ref : task.getOutputs()) {
            _output.add(new PropertyDTO(ref));
        }
        for (Map.Entry<String, String> entry : task.getMetaData().entrySet()) {
            _metaData.add(new MetaDataDTO(entry));
        }
    }

    @XmlAttribute 
    public String getProcessId() {
        return _processId;
    }

    public void setProcessId(String processId) {
        this._processId = processId;
    }
    
    @XmlAttribute 
    public String getTaskName() {
        return _taskName;
    }

    public void setTaskName(String taskName) {
        this._taskName = taskName;
    }

    @XmlAttribute 
    public String getTaskId() {
        return _taskId;
    }

    public void setTaskId(String taskId) {
        this._taskId = taskId;
    }

    @XmlElement 
    public List<PropertyDTO> getInput() {
        return _input;
    }

    public void setInput(List<PropertyDTO> input) {
        this._input = input;
    }

    @XmlElement 
    public List<PropertyDTO> getOutput() {
        return _output;
    }

    public void setOutput(List<PropertyDTO> output) {
        this._output = output;
    }

    @XmlElement 
    public List<MetaDataDTO> getMetaData() {
        return _metaData;
    }

    public void setMetaData(List<MetaDataDTO> metaData) {
        this._metaData = metaData;
    }
}
