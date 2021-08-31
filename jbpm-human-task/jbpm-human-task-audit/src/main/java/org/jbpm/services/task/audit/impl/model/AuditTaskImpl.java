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

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;

import org.kie.internal.task.api.AuditTask;

@Entity
@Table(name = "AuditTaskImpl", indexes = {
        @Index(name = "IDX_AuditTaskImpl_taskId", columnList = "taskId"),
        @Index(name = "IDX_AuditTaskImpl_pInstId", columnList = "processInstanceId"),
        @Index(name = "IDX_AuditTaskImpl_workItemId", columnList = "workItemId"),
        @Index(name = "IDX_AuditTaskImpl_name", columnList = "name"),
        @Index(name = "IDX_AuditTaskImpl_processId", columnList = "processId"),
        @Index(name = "IDX_AuditTaskImpl_status", columnList = "status")
})
@SequenceGenerator(name = "auditIdSeq", sequenceName = "AUDIT_ID_SEQ", allocationSize = 1)
public class AuditTaskImpl implements Serializable,
                                      AuditTask {

    private static final long serialVersionUID = 5388016330549830043L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "auditIdSeq")
    private Long id;

    private Long taskId;

    private String status;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date activationTime;
    private String name;
    private String description;
    private int priority;
    private String createdBy;
    private String actualOwner;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date createdOn;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date dueDate;
    private long processInstanceId;
    private String processId;
    private long processSessionId;
    private long parentId;
    private String deploymentId;
    private Long workItemId;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date lastModificationDate;

    public AuditTaskImpl() {
    }

    public AuditTaskImpl(long taskId,
                         String name,
                         String status,
                         Date activationTime,
                         String actualOwner,
                         String description,
                         int priority,
                         String createdBy,
                         Date createdOn,
                         Date dueDate,
                         long processInstanceId,
                         String processId,
                         long processSessionId,
                         String deploymentId,
                         long parentId,
                         long workItemId) {
        this.taskId = taskId;
        this.status = status;
        this.activationTime = activationTime;
        this.name = name;
        this.description = description;
        this.priority = priority;
        this.createdBy = createdBy;
        this.createdOn = createdOn;
        this.actualOwner = actualOwner;
        this.dueDate = dueDate;
        this.processInstanceId = processInstanceId;
        this.processId = processId;
        this.processSessionId = processSessionId;
        this.deploymentId = deploymentId;
        this.parentId = parentId;
        this.workItemId = workItemId;
        this.lastModificationDate = new Date();
    }

    public AuditTaskImpl(long taskId,
                         String name,
                         String status,
                         Date activationTime,
                         String actualOwner,
                         String description,
                         int priority,
                         String createdBy,
                         Date createdOn,
                         Date dueDate,
                         long processInstanceId,
                         String processId,
                         long processSessionId,
                         String deploymentId,
                         long parentId,
                         long workItemId,
                         Date lastModificationDate) {
        this.taskId = taskId;
        this.status = status;
        this.activationTime = activationTime;
        this.name = name;
        this.description = description;
        this.priority = priority;
        this.createdBy = createdBy;
        this.createdOn = createdOn;
        this.actualOwner = actualOwner;
        this.dueDate = dueDate;
        this.processInstanceId = processInstanceId;
        this.processId = processId;
        this.processSessionId = processSessionId;
        this.deploymentId = deploymentId;
        this.parentId = parentId;
        this.workItemId = workItemId;
        this.lastModificationDate = lastModificationDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public long getTaskId() {
        return taskId;
    }

    @Override
    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public Date getActivationTime() {
        return activationTime;
    }

    @Override
    public void setActivationTime(Date activationTime) {
        this.activationTime = activationTime;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public String getCreatedBy() {
        return createdBy;
    }

    @Override
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public Date getCreatedOn() {
        return createdOn;
    }

    @Override
    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    @Override
    public Date getDueDate() {
        return dueDate;
    }

    @Override
    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    @Override
    public long getProcessInstanceId() {
        return processInstanceId;
    }

    @Override
    public void setProcessInstanceId(long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    @Override
    public String getProcessId() {
        return processId;
    }

    @Override
    public void setProcessId(String processId) {
        this.processId = processId;
    }

    @Override
    public long getProcessSessionId() {
        return processSessionId;
    }

    @Override
    public void setProcessSessionId(long processSessionId) {
        this.processSessionId = processSessionId;
    }

    @Override
    public long getParentId() {
        return parentId;
    }

    @Override
    public void setParentId(long parentId) {
        this.parentId = parentId;
    }

    public String getActualOwner() {
        return actualOwner;
    }

    public void setActualOwner(String actualOwner) {
        this.actualOwner = actualOwner;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    @Override
    public long getWorkItemId() {
        return workItemId;
    }

    @Override
    public void setWorkItemId(long workItemId) {
        this.workItemId = workItemId;
    }

    public Date getLastModificationDate() {
        return lastModificationDate;
    }

    public void setLastModificationDate(Date lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((activationTime == null) ? 0 : activationTime.hashCode());
        result = prime * result + ((actualOwner == null) ? 0 : actualOwner.hashCode());
        result = prime * result + ((createdBy == null) ? 0 : createdBy.hashCode());
        result = prime * result + ((createdOn == null) ? 0 : createdOn.hashCode());
        result = prime * result + ((deploymentId == null) ? 0 : deploymentId.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((dueDate == null) ? 0 : dueDate.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((lastModificationDate == null) ? 0 : lastModificationDate.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + (int) (parentId ^ (parentId >>> 32));
        result = prime * result + priority;
        result = prime * result + ((processId == null) ? 0 : processId.hashCode());
        result = prime * result + (int) (processInstanceId ^ (processInstanceId >>> 32));
        result = prime * result + (int) (processSessionId ^ (processSessionId >>> 32));
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + ((taskId == null) ? 0 : taskId.hashCode());
        result = prime * result + ((workItemId == null) ? 0 : workItemId.hashCode());
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
        AuditTaskImpl other = (AuditTaskImpl) obj;
        if (activationTime == null) {
            if (other.activationTime != null)
                return false;
        } else if (!activationTime.equals(other.activationTime))
            return false;
        if (actualOwner == null) {
            if (other.actualOwner != null)
                return false;
        } else if (!actualOwner.equals(other.actualOwner))
            return false;
        if (createdBy == null) {
            if (other.createdBy != null)
                return false;
        } else if (!createdBy.equals(other.createdBy))
            return false;
        if (createdOn == null) {
            if (other.createdOn != null)
                return false;
        } else if (!createdOn.equals(other.createdOn))
            return false;
        if (deploymentId == null) {
            if (other.deploymentId != null)
                return false;
        } else if (!deploymentId.equals(other.deploymentId))
            return false;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (dueDate == null) {
            if (other.dueDate != null)
                return false;
        } else if (!dueDate.equals(other.dueDate))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (lastModificationDate == null) {
            if (other.lastModificationDate != null)
                return false;
        } else if (!lastModificationDate.equals(other.lastModificationDate))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (parentId != other.parentId)
            return false;
        if (priority != other.priority)
            return false;
        if (processId == null) {
            if (other.processId != null)
                return false;
        } else if (!processId.equals(other.processId))
            return false;
        if (processInstanceId != other.processInstanceId)
            return false;
        if (processSessionId != other.processSessionId)
            return false;
        if (status == null) {
            if (other.status != null)
                return false;
        } else if (!status.equals(other.status))
            return false;
        if (taskId == null) {
            if (other.taskId != null)
                return false;
        } else if (!taskId.equals(other.taskId))
            return false;
        if (workItemId == null) {
            if (other.workItemId != null)
                return false;
        } else if (!workItemId.equals(other.workItemId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "AuditTaskImpl [id=" + id + ", taskId=" + taskId + ", status=" + status + ", activationTime=" +
               activationTime + ", name=" + name + ", description=" + description + ", priority=" + priority +
               ", createdBy=" + createdBy + ", actualOwner=" + actualOwner + ", createdOn=" + createdOn + ", dueDate=" +
               dueDate + ", processInstanceId=" + processInstanceId + ", processId=" + processId +
               ", processSessionId=" + processSessionId + ", parentId=" + parentId + ", deploymentId=" + deploymentId +
               ", workItemId=" + workItemId + ", lastModificationDate=" + lastModificationDate + "]";
    }

}
