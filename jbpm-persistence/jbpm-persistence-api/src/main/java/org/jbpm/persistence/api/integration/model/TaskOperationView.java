/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

import java.util.Arrays;

import org.jbpm.persistence.api.integration.InstanceView;
import org.kie.internal.task.api.TaskOperationInfo;
import org.kie.internal.task.api.TaskOperationType;


public class TaskOperationView implements InstanceView<TaskOperationInfo> {

    private static final long serialVersionUID = 1L;
    private transient TaskOperationInfo source;

    private String compositeId;
    private TaskOperationType type;
    private String userId;
    private String[] targetEntities;

    public TaskOperationView(TaskOperationInfo taskOperation) {
        this.source = taskOperation;
    }

    public String[] getTargetEntities() {
        return targetEntities;
    }

    public TaskOperationType getType() {
        return type;
    }

    public String getUserId() {
        return userId;
    }

    @Override
    public void copyFromSource() {
        this.compositeId = System.getProperty("org.kie.server.id", "") + '_' + source.getTask().getId();
        this.userId = source.getUserId();
        this.type = source.getType();
        this.targetEntities = source.getTargetEntities();
    }

    @Override
    public TaskOperationInfo getSource() {
        return source;
    }

    @Override
    public String getCompositeId() {
       return compositeId;
    }

    @Override
    public String toString() {
        return "TaskOperationView [compositeId=" + compositeId + ", type=" + type + ", userId=" + userId +
               ", targetEntities=" + Arrays.toString(targetEntities) + "]";
    }
}
