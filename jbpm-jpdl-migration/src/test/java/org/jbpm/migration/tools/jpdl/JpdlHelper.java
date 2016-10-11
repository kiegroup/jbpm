/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.migration.tools.jpdl;

import java.util.Collection;

import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.taskmgmt.exe.TaskInstance;

/**
 * Common helper methods for jpdl execution and diagnostics.
 */
public class JpdlHelper {

    /**
     * Finds task instance of given name.
     *
     * @param name
     *            Task name.
     * @param pi
     *            Process instance containing the task.
     * @return task instance
     * @throws IllegalArgumentException
     *             if the task instance hasn't been found.
     */
    public static TaskInstance getTaskInstance(String name, ProcessInstance pi) {
        Collection<TaskInstance> taskInstances = pi.getTaskMgmtInstance().getTaskInstances();
        for (TaskInstance ti : taskInstances) {
            if (ti.getName().equals(name)) {
                return ti;
            }
        }
        throw new IllegalArgumentException(String.format("Task instance with name \"%s\" was not found", name));
    }
}
