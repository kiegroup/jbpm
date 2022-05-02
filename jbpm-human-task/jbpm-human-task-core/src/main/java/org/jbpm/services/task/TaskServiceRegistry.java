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

package org.jbpm.services.task;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.kie.internal.task.api.InternalTaskService;

public final class TaskServiceRegistry {

    private static TaskServiceRegistry INSTANCE;

    private Map<String, InternalTaskService> taskServices;

    private InternalTaskService defaultTaskService;

    public TaskServiceRegistry() {
        taskServices = new ConcurrentHashMap<>();
    }
    public static synchronized TaskServiceRegistry instance() {
        if (INSTANCE == null) {
            INSTANCE = new TaskServiceRegistry();
        }
        return INSTANCE;
    }

    public void registerTaskService(String deploymentId, InternalTaskService taskService) {
        if(taskService == null) {
            return;
        }
        if (deploymentId == null) {
            defaultTaskService = taskService;
        } else {
            taskServices.put(deploymentId, taskService);
        }
    }

    public InternalTaskService get(String deploymentId) {
        return deploymentId != null ? taskServices.get(deploymentId) : defaultTaskService;
    }

    public void remove(String deploymentId) {
        if (deploymentId == null) {
            defaultTaskService = null;
        } else {
            taskServices.remove(deploymentId);
        }
    }
}
