/**
 * Copyright 2011 Miklos Vajna
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
package org.jbpm.task.service.responsehandlers;

import java.util.List;
import org.jbpm.task.Task;
import org.jbpm.task.service.TaskClientHandler.GetTasksResponseHandler;

public class BlockingGetTasksResponseHandler extends AbstractBlockingResponseHandler implements GetTasksResponseHandler {
    private static final int TASK_WAIT_TIME = 10000;

    private volatile List<Task> tasks;

    public synchronized void execute(List<Task> tasks) {
        this.tasks = tasks;
        setDone(true);
    }

    public List<Task> getTasks() {
        boolean done = waitTillDone(TASK_WAIT_TIME);

        if (!done) {
            throw new RuntimeException("Timeout : unable to retrieve Tasks");
        }

        return tasks;
    }
}
