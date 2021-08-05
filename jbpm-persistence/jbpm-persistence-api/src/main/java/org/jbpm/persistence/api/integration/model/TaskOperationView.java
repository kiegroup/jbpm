/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.persistence.api.integration.model;

import java.util.Date;

import org.jbpm.persistence.api.integration.InstanceView;
import org.kie.api.task.TaskEvent;
import org.kie.api.task.TaskLifeCycleEventListener.AssignmentType;
import org.kie.internal.task.api.model.TaskEvent.TaskEventType;


public class TaskOperationView implements InstanceView<TaskEvent> {

    private static final long serialVersionUID = 1L;
    private transient TaskEvent source;

    private String compositeId;
    private TaskEventType type;
    private AssignmentType assignType;
    private Date date;
    private String userId;
    
    public TaskOperationView(TaskEvent taskEvent, TaskEventType type, AssignmentType assignType) {
        this.source = taskEvent;
        this.type = type;
        this.assignType = assignType;
    }

    public TaskEventType getType() {
        return type;
    }
    
    public AssignmentType getAssignType() {
        return assignType;
    }

    public String getUserId() {
        return userId;
    }
    
    public Date getDate () {
        return date;
    }

    @Override
    public void copyFromSource() {
        this.compositeId = System.getProperty("org.kie.server.id", "") + '_' + source.getTask().getId();
        this.userId = source.getTaskContext().getUserId();
        this.date = source.getEventDate();
    }

    @Override
    public TaskEvent getSource() {
        return source;
    }

    @Override
    public String getCompositeId() {
       return compositeId;
    }

    @Override
    public String toString() {
        return "TaskOperationView [compositeId=" + compositeId + ", type=" + type + ", assignType=" + assignType +
               ", date=" + date + ", userId=" + userId + "]";
    }

}
