/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.services.task.impl.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.jbpm.services.task.commands.TaskContext;
import org.kie.api.runtime.Environment;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.PeopleAssignments;
import org.kie.internal.task.api.TaskDeadlinesService;
import org.kie.internal.task.api.TaskDeadlinesService.DeadlineType;
import org.kie.internal.task.api.TaskPersistenceContext;
import org.kie.internal.task.api.model.Deadline;
import org.kie.internal.task.api.model.Deadlines;
import org.kie.internal.task.api.model.InternalTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DeadlineSchedulerHelper {
    
    private DeadlineSchedulerHelper() {}

    private static final Logger logger = LoggerFactory.getLogger(DeadlineSchedulerHelper.class);
    
    public static void rescheduleDeadlinesForTask(final InternalTask task, TaskContext taskContext, boolean unboundRepeatableOnly,  DeadlineType ...types) {
        rescheduleDeadlinesForTask(task, taskContext, unboundRepeatableOnly, null, types);
    }
    

    public static void rescheduleDeadlinesForTask(final InternalTask task, TaskContext taskContext, boolean unboundRepeatableOnly, Deadline triggered, DeadlineType ...types) {
        Environment environment = taskContext.getTaskContentService().getMarshallerContext(task).getEnvironment();
        TaskPersistenceContext persistenceContext = taskContext.getPersistenceContext();
        taskContext.loadTaskVariables(task);
        PeopleAssignments peopleAssignments = task.getPeopleAssignments();
        List<OrganizationalEntity> businessAdministrators = peopleAssignments.getBusinessAdministrators();
        List<DeadlineType> deadlineTypes = Arrays.asList(types);

        Deadlines deadlines = HumanTaskHandlerHelper.setDeadlines(task.getTaskData().getTaskInputVariables(), businessAdministrators, environment, unboundRepeatableOnly);

        if (deadlines.getStartDeadlines().isEmpty() && deadlines.getEndDeadlines().isEmpty()) {
            // If there are no deadlines to schedule, skip scheduling completely
            return;
        }
        
         TaskDeadlinesService deadlineService = taskContext.getTaskDeadlinesService();

         if (deadlineTypes.contains(DeadlineType.START)) {
             logger.debug("Assigning START deadlines {}", deadlines.getStartDeadlines());
             scheduleDeadlines(
                     cleanDeadlines(deadlines.getStartDeadlines(), task.getDeadlines().getStartDeadlines(), triggered,
                             persistenceContext),
                     System.currentTimeMillis(), task.getId(), DeadlineType.START, deadlineService);

         }
         if (deadlineTypes.contains(DeadlineType.END)) {
             logger.debug("Assigning END deadlines {}", deadlines.getEndDeadlines());
             scheduleDeadlines(
                     cleanDeadlines(deadlines.getEndDeadlines(), task.getDeadlines().getEndDeadlines(), triggered,
                             persistenceContext),
                     System.currentTimeMillis(), task.getId(), DeadlineType.END, deadlineService);
         }
         persistenceContext.updateTask(task);
    }
    
    
    private static List<Deadline> cleanDeadlines(List<Deadline> parsedDeadlines,
                                                 List<Deadline> deadlines,
                                                 Deadline triggered,
                                                 TaskPersistenceContext persistenceContext) {
        List<Deadline> result = new ArrayList<>();
        for (Deadline deadline : parsedDeadlines) {
            // if reschedule is result of a trigger, then reschedule only the triggered deadline
            if (triggered != null && !deadline.getEscalations().equals(triggered.getEscalations())) {
                continue;
            }
            Iterator<Deadline> iter = deadlines.iterator();
            boolean found = false;
            while (!found && iter.hasNext()) {
                Deadline candidate = iter.next();
                if (deadline.getEscalations().equals(candidate.getEscalations())) {
                    found = true;
                    iter.remove();
                    persistenceContext.remove(candidate);
                }
            }
            if (found) {
                result.add(deadline);
                deadlines.add(deadline);
                persistenceContext.persistDeadline(deadline);
            }
        }
        return result;
    }

    public static void scheduleDeadlinesForTask(final InternalTask task, TaskContext taskContext, DeadlineType ...types) {
        TaskDeadlinesService deadlineService = taskContext.getTaskDeadlinesService();
        final long now = System.currentTimeMillis();
        List<DeadlineType> deadlineTypes = Arrays.asList(types);
        Deadlines deadlines = task.getDeadlines();

        if (deadlines != null) {
            final List<? extends Deadline> startDeadlines = deadlines.getStartDeadlines();

            if (startDeadlines != null && deadlineTypes.contains(DeadlineType.START)) {
                scheduleDeadlines(startDeadlines, now, task.getId(), DeadlineType.START, deadlineService);
            }

            final List<? extends Deadline> endDeadlines = deadlines.getEndDeadlines();

            if (endDeadlines != null && deadlineTypes.contains(DeadlineType.END)) {
                scheduleDeadlines(endDeadlines, now, task.getId(), DeadlineType.END, deadlineService);
            }
        }
    }

    public static void scheduleDeadlines(final List<? extends Deadline> deadlines, final long now, 
            final long taskId, DeadlineType type, TaskDeadlinesService deadlineService) {
        for (Deadline deadline : deadlines) {
            if (Boolean.FALSE.equals(deadline.isEscalated())) {
                Date date = deadline.getDate();
                deadlineService.schedule(taskId, deadline.getId(), date.getTime() - now, type);
            }
        }
    }
}
