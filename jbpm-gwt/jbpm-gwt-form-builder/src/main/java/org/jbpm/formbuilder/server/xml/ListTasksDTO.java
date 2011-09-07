/*
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
