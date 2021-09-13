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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jbpm.services.task.audit.impl.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.Version;

import org.kie.internal.task.api.model.InternalTaskData;
import org.kie.internal.task.api.model.TaskEvent;

/**
 *
 */
@Entity
@Table(name = "TaskEvent", indexes = {@Index(name = "IDX_TaskEvent_taskId", columnList = "taskId"), @Index(name = "IDX_TaskEvent_processInstanceId", columnList = "processInstanceId")})
@SequenceGenerator(name = "taskEventIdSeq", sequenceName = "TASK_EVENT_ID_SEQ")
public class TaskEventImpl implements TaskEvent, Serializable {

  private static final long serialVersionUID = 6304722095353315479L;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "taskEventIdSeq")
  @Column(name = "id")
  private Long id;

  @Version
  @Column(name = "OPTLOCK")
  private Integer version;

  private Long taskId;

  private Long workItemId;

  @Enumerated(EnumType.STRING)
  private TaskEventType type;

  private Long processInstanceId;

  private String userId;

  private String message;

  private String correlationKey;

  private Integer processType;
  
  private String currentOwner;

  @Temporal(javax.persistence.TemporalType.TIMESTAMP)
  private Date logTime;

  public TaskEventImpl() {
  }

  public TaskEventImpl(long taskId, TaskEventType type, String userId) {
    this.taskId = taskId;
    this.type = type;
    this.userId = userId;
    this.logTime = new Date();
  }

  public TaskEventImpl(Long taskId, TaskEventType type, String userId, Date logTime) {
    this.taskId = taskId;
    this.type = type;
    this.userId = userId;
    this.logTime = logTime;
  }

  public TaskEventImpl(Long taskId, TaskEventType type, Long processInstanceId, Long workItemId, String userId, Date logTime) {
    this.taskId = taskId;
    this.type = type;
    this.processInstanceId = processInstanceId;
    this.workItemId = workItemId;
    this.userId = userId;
    this.logTime = logTime;
  }

  public TaskEventImpl(Long taskId, TaskEventType type, Long processInstanceId, Long workItemId, String userId) {
    this(taskId, type, processInstanceId, workItemId, userId, new Date());

  }

  public TaskEventImpl(Long taskId, TaskEventType type, Long processInstanceId, Long workItemId, String userId, String message) {
    this(taskId, type, processInstanceId, workItemId, userId, new Date());
    this.message = message;
  }
  
  public TaskEventImpl(org.kie.api.task.TaskEvent event, TaskEventType type) {
      this(event, type, null);
  }

  public TaskEventImpl(org.kie.api.task.TaskEvent event, TaskEventType type, String message) {
      InternalTaskData taskData = (InternalTaskData)event.getTask().getTaskData(); 
      this.taskId = event.getTask().getId();
      this.type = type;
      this.processInstanceId = taskData.getProcessInstanceId();
      this.workItemId = taskData.getWorkItemId();
      this.currentOwner = taskData.getActualOwner()!= null? taskData.getActualOwner().getId() : "";
      this.userId = event.getTaskContext().getUserId();
      this.message = message;
      this.logTime = event.getEventDate();
  }

  @Override
  public long getId() {
    return id;
  }

  @Override
  public long getTaskId() {
    return taskId;
  }

  @Override
  public TaskEventType getType() {
    return type;
  }

  @Override
  public String getUserId() {
    return userId;
  }

  @Override
  public Date getLogTime() {
    return logTime;
  }

  @Override
  public Long getProcessInstanceId() {
    return processInstanceId;
  }

  public Long getWorkItemId() {
    return workItemId;
  }

  public String getMessage() {
    return message;
  }

  @Override
  public String getCorrelationKey() {
    return correlationKey;
}

  @Override
  public Integer getProcessType() {
    return processType;
  }

  @Override
  public void setCorrelationKey(String correlationKey) {
    this.correlationKey = correlationKey;
  }

  @Override
  public void setProcessType(Integer processType) {
    this.processType = processType;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  @Override
  public String getCurrentOwner() {
      return currentOwner;
  }

  public void setCurrentOwner(String currentOwner) {
      this.currentOwner = currentOwner;
  }

  @Override
  public int hashCode() {
      return Objects.hash(currentOwner, correlationKey, id, logTime, message, processInstanceId, processType, taskId, type, userId, version, workItemId);
  }

  @Override
  public boolean equals(Object obj) {
      if (this == obj) {
          return true;
      }
      if (!(obj instanceof TaskEventImpl)) {
          return false;
      }
      TaskEventImpl other = (TaskEventImpl) obj;
      return Objects.equals(currentOwner, other.currentOwner) && Objects.equals(correlationKey, other.correlationKey) && Objects.equals(id, other.id) && Objects.equals(logTime, other.logTime) && Objects.equals(message,
                                                                                                                                                                                                                other.message) &&
             Objects.equals(processInstanceId, other.processInstanceId) && Objects.equals(processType, other.processType) && Objects.equals(taskId, other.taskId) && type == other.type && Objects.equals(userId,
                                                                                                                                                                                                          other.userId) &&
             Objects.equals(version, other.version) && Objects.equals(workItemId, other.workItemId);
  }

  @Override
  public String toString() {
      return "TaskEventImpl [id=" + id + ", version=" + version + ", taskId=" + taskId + ", workItemId=" + workItemId + ", type=" + type + ", processInstanceId=" + processInstanceId + ", userId=" + userId +
             ", message=" +
             message + ", correlationKey=" + correlationKey + ", processType=" + processType + ", actualOwner=" + currentOwner + ", logTime=" + logTime + "]";
  }
}
