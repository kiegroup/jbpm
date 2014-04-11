/*
 * Copyright 2013 JBoss by Red Hat.
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
package org.jbpm.services.task.audit;

import org.jbpm.services.task.audit.index.IndexService;
import org.jbpm.services.task.audit.service.TaskAuditService;
import org.jbpm.services.task.audit.service.TaskAuditServiceImpl;
import org.kie.api.task.TaskService;

/**

 */
public class TaskAuditConfigurator {

    private final TaskAuditService taskAuditService = new TaskAuditServiceImpl();
    private boolean multiThreadIndex = false;

    public TaskAuditConfigurator setTaskService(TaskService service) {
        taskAuditService.setTaskService(service);
        return this;
    }

    public TaskAuditConfigurator setIndexService(IndexService service) {
        taskAuditService.setIndexService(service);
        return this;
    }

    public TaskAuditConfigurator setMultiThreadIndex(boolean multiThreadIndex) {
        this.multiThreadIndex = multiThreadIndex;
        return this;
    }

    public TaskAuditService getTaskAuditService(){
        ((TaskAuditServiceImpl) taskAuditService).setMultiThreadIndex(multiThreadIndex);
        ((TaskAuditServiceImpl) taskAuditService).startupIndexes();
        return taskAuditService;
    }


}