/*
 * Copyright 2014 JBoss by Red Hat.
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

package org.jbpm.services.task.audit.service;

import java.util.Date;
import java.util.List;
import org.jbpm.services.task.audit.commands.GetAllUserAuditTasksByDueDateCommand;
import org.jbpm.services.task.audit.commands.GetAllUserAuditTasksByStatusByDueDateCommand;
import org.jbpm.services.task.audit.commands.GetAllUserAuditTasksByStatusCommand;
import org.jbpm.services.task.audit.commands.GetAllUserAuditTasksCommand;
import org.jbpm.services.task.audit.commands.GetAuditEventsCommand;
import org.jbpm.services.task.audit.impl.model.UserAuditTask;
import org.kie.api.task.TaskService;
import org.kie.internal.task.api.InternalTaskService;
import org.kie.internal.task.api.model.TaskEvent;

/**
 *
 * @author salaboy
 */
public class TaskAuditServiceImpl implements TaskAuditService {
    
    private InternalTaskService taskService;
    
    @Override
    public List<TaskEvent> getAllTaskEvents(long taskId) {
        return taskService.execute(new GetAuditEventsCommand(taskId));
    }
    
    @Override
    public List<UserAuditTask> getAllUserAuditTasks(String userId) {
        return taskService.execute(new GetAllUserAuditTasksCommand(userId));
    }
    
    @Override
    public List<UserAuditTask> getAllUserAuditTasksByStatus(String userId, String status) {
        return taskService.execute(new GetAllUserAuditTasksByStatusCommand(userId, status));
    }
    
    @Override
    public List<UserAuditTask> getAllUserAuditTasksByDueDate(String userId, Date dueDate) {
        return taskService.execute(new GetAllUserAuditTasksByDueDateCommand(userId, dueDate));
    }
    
    @Override
    public List<UserAuditTask> getAllUserAuditTasksByStatusByDueDate(String userId, String status, Date dueDate) {
        return taskService.execute(new GetAllUserAuditTasksByStatusByDueDateCommand(userId, status, dueDate));
    }
    
    @Override
    public List<UserAuditTask> getAllUserAuditTasksByStatusByDueDateOptional(String userId, String status, Date dueDate) {
        return taskService.execute(new GetAllUserAuditTasksByStatusByDueDateCommand(userId, status, dueDate));
    }

    @Override
    public void setTaskService(TaskService taskService) {
        this.taskService = (InternalTaskService) taskService;
    }
    
}
