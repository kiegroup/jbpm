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
import org.jbpm.services.task.audit.impl.model.GroupAuditTask;
import org.jbpm.services.task.audit.impl.model.UserAuditTask;
import org.kie.api.task.TaskService;
import org.kie.internal.task.api.model.TaskEvent;

/**
 *
 * @author salaboy
 */
public interface TaskAuditService {
    void setTaskService(TaskService taskService);
    List<TaskEvent> getAllTaskEvents(long taskId);
    List<UserAuditTask> getAllUserAuditTasks(String userId);
    List<UserAuditTask> getAllUserAuditTasksByStatus(String userId, String status);
    List<UserAuditTask> getAllUserAuditTasksByDueDate(String userId, Date dueDate);
    List<UserAuditTask> getAllUserAuditTasksByStatusByDueDate(String userId, String status, Date dueDate);
    List<UserAuditTask> getAllUserAuditTasksByStatusByDueDateOptional(String userId, String status, Date dueDate);
    
    List<GroupAuditTask> getAllGroupAuditTasks(String groupIds);
    List<GroupAuditTask> getAllGroupAuditTasksByStatus(String groupIds, String status);
    List<GroupAuditTask> getAllGroupAuditTasksByDueDate(String groupIds, Date dueDate);
    List<GroupAuditTask> getAllGroupAuditTasksByStatusByDueDate(String groupIds, String status, Date dueDate);
    List<GroupAuditTask> getAllGroupAuditTasksByStatusByDueDateOptional(String groupIds, String status, Date dueDate);
    
}
