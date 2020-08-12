/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.services.task.audit.impl.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;

import org.kie.internal.task.api.TaskVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Table(name = "TaskVariableImpl", indexes = {
        @Index(name = "IDX_TaskVariableImpl_taskId", columnList = "taskId"),
        @Index(name = "IDX_TaskVariableImpl_pInstId", columnList = "processInstanceId"),
        @Index(name = "IDX_TaskVariableImpl_processId", columnList = "processId")
})
@SequenceGenerator(name = "taskVarIdSeq", sequenceName = "TASK_VAR_ID_SEQ", allocationSize = 1)
public class TaskVariableImpl implements TaskVariable, Serializable {

    private static final long serialVersionUID = 5388016330549830048L;
    private static final Logger logger = LoggerFactory.getLogger(TaskVariableImpl.class);
    
    @Transient
    private final int VARIABLE_LOG_LENGTH = Integer.parseInt(System.getProperty("org.jbpm.task.var.log.length", "4000"));

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "taskVarIdSeq")
    private Long id;

    private Long taskId;

    private Long processInstanceId;

    private String processId;

    private String name;

    @Column(length=4000)
    private String value;

    @Enumerated(EnumType.ORDINAL)
    private VariableType type;

    @Temporal(TemporalType.TIMESTAMP)
    private Date modificationDate;

    public Long getId() {
        return id;
    }

    @Override
    public Long getTaskId() {
        return taskId;
    }

    @Override
    public Long getProcessInstanceId() {
        return processInstanceId;
    }

    @Override
    public String getProcessId() {
        return processId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public Date getModificationDate() {
        return modificationDate;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public void setProcessInstanceId(Long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        if (value != null && value.length() > VARIABLE_LOG_LENGTH) {
            value = value.substring(0, VARIABLE_LOG_LENGTH);
            logger.warn("Task variable '{}' content was trimmed as it was too long (more than {} characters)", name, VARIABLE_LOG_LENGTH);
        }
        this.value = value;
    }

    public void setModificationDate(Date modificationDate) {
        this.modificationDate = modificationDate;
    }

    public VariableType getType() {
        return type;
    }

    public void setType(VariableType type) {
        this.type = type;
    }

    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((modificationDate == null) ? 0 : modificationDate.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((processId == null) ? 0 : processId.hashCode());
        result = prime * result + ((processInstanceId == null) ? 0 : processInstanceId.hashCode());
        result = prime * result + ((taskId == null) ? 0 : taskId.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TaskVariableImpl other = (TaskVariableImpl) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (modificationDate == null) {
            if (other.modificationDate != null)
                return false;
        } else if (!modificationDate.equals(other.modificationDate))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (processId == null) {
            if (other.processId != null)
                return false;
        } else if (!processId.equals(other.processId))
            return false;
        if (processInstanceId == null) {
            if (other.processInstanceId != null)
                return false;
        } else if (!processInstanceId.equals(other.processInstanceId))
            return false;
        if (taskId == null) {
            if (other.taskId != null)
                return false;
        } else if (!taskId.equals(other.taskId))
            return false;
        if (type != other.type)
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "TaskVariableImpl [id=" + id + ", taskId=" + taskId + ", processInstanceId=" + processInstanceId +
               ", processId=" + processId + ", name=" + name + ", value=" + value + ", type=" + type +
               ", modificationDate=" + modificationDate + "]";
    }


}
