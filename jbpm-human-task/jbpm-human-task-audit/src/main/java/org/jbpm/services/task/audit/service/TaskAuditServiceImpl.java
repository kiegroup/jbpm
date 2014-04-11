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

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.jbpm.services.task.audit.commands.GetAllGroupAuditTasksAdminCommand;
import org.jbpm.services.task.audit.commands.GetAllGroupAuditTasksByDueDateCommand;
import org.jbpm.services.task.audit.commands.GetAllGroupAuditTasksByStatusByDueDateCommand;
import org.jbpm.services.task.audit.commands.GetAllGroupAuditTasksByStatusCommand;
import org.jbpm.services.task.audit.commands.GetAllGroupAuditTasksCommand;
import org.jbpm.services.task.audit.commands.GetAllHistoryAuditTasksByUserCommand;
import org.jbpm.services.task.audit.commands.GetAllHistoryAuditTasksCommand;
import org.jbpm.services.task.audit.commands.GetAllUserAuditTasksAdminCommand;
import org.jbpm.services.task.audit.commands.GetAllUserAuditTasksByDueDateCommand;
import org.jbpm.services.task.audit.commands.GetAllUserAuditTasksByStatusByDueDateCommand;
import org.jbpm.services.task.audit.commands.GetAllUserAuditTasksByStatusCommand;
import org.jbpm.services.task.audit.commands.GetAllUserAuditTasksCommand;
import org.jbpm.services.task.audit.commands.GetAuditEventsCommand;
import org.jbpm.services.task.audit.impl.model.api.GroupAuditTask;
import org.jbpm.services.task.audit.impl.model.api.HistoryAuditTask;
import org.jbpm.services.task.audit.impl.model.api.UserAuditTask;
import org.jbpm.services.task.audit.index.IndexService;
import org.jbpm.services.task.audit.query.Filter;
import org.jbpm.services.task.audit.query.QueryComparator;
import org.jbpm.services.task.audit.query.QueryResult;
import org.kie.api.task.TaskService;
import org.kie.internal.task.api.InternalTaskService;
import org.kie.internal.task.api.model.TaskEvent;

/**
 *
 * @author salaboy
 */
public class TaskAuditServiceImpl implements TaskAuditService {

    private InternalTaskService taskService;
    private IndexService indexService;
    private boolean multiThreadIndex;
    private TaskIndexThread taskIndexInitThread; 

    @Override
    public List<TaskEvent> getAllTaskEvents(long taskId, int offset, int count) {
        if (indexService != null) {
            return getTaskEvents(offset, count, new TaskEventComparator(
                QueryComparator.Direction.DESCENDING));
        }
        return taskService
            .execute(new GetAuditEventsCommand(taskId, offset, count));
    }

    @Override
    public QueryResult<TaskEvent> getTaskEvents(int offset, int count,
        QueryComparator<TaskEvent> comparator,
        Filter<TaskEvent, ?>... filters) {
        if (indexService == null) throw new IllegalStateException("getTaskEvents needs indexService");
        try {
            return indexService
                .find(offset, count, comparator, TaskEvent.class, filters);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<UserAuditTask> getAllUserAuditTasks(String userId, int offset,
        int count) {
        if (indexService != null) {
            return getUserAuditTasks(offset, count, new TaskIdComparator<UserAuditTask>(), new ActualOwnerFilter<UserAuditTask>(userId));
        }
        return taskService
            .execute(new GetAllUserAuditTasksCommand(userId, offset, count));
    }

    @Override
    public List<UserAuditTask> getAllUserAuditTasksByStatus(String userId,
        List<String> statuses, int offset, int count) {
        if (indexService != null) {
            return getUserAuditTasks(offset, count, new TaskIdComparator<UserAuditTask>(),
                new ActualOwnerFilter<UserAuditTask>(userId),
                    new StatusFilter<UserAuditTask>(statuses));
        }

        List<UserAuditTask> execute = null;
        try {
            execute = taskService.execute(
                new GetAllUserAuditTasksByStatusCommand(userId, statuses,
                    offset, count));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return execute;
    }

    @Override
    public List<UserAuditTask> getAllUserAuditTasksByDueDate(String userId,
        Date dueDate, int offset, int count) {
        if (indexService != null) {
                return getUserAuditTasks(offset, count, new TaskIdComparator<UserAuditTask>(),
                    new ActualOwnerFilter<UserAuditTask>(userId),
                    new DueDateFilter<UserAuditTask>(dueDate));
        }
        return taskService.execute(
            new GetAllUserAuditTasksByDueDateCommand(userId, dueDate, offset,
                count)
        );
    }

    @Override
    public List<UserAuditTask> getAllUserAuditTasksByStatusByDueDate(
        String userId, List<String> statuses, Date dueDate, int offset,
        int count) {
        if (indexService != null) {
            return getUserAuditTasks(offset, count, new TaskIdComparator<UserAuditTask>(),new ActualOwnerFilter<UserAuditTask>(userId),
                new DueDateFilter<UserAuditTask>(dueDate), new StatusFilter<UserAuditTask>(statuses));

        }
        return taskService.execute(
            new GetAllUserAuditTasksByStatusByDueDateCommand(userId, statuses,
                dueDate, offset, count));
    }

    @Override
    public List<UserAuditTask> getAllUserAuditTasksByStatusByDueDateOptional(
        //TODO: what is the difference to the method above?
        String userId, List<String> statuses, Date dueDate, int offset, int count) {
        if (indexService != null) {
            return getUserAuditTasks(offset, count, new TaskIdComparator<UserAuditTask>(),
                new ActualOwnerFilter<UserAuditTask>(userId),
                new DueDateFilter<UserAuditTask>(dueDate),
                new StatusFilter<UserAuditTask>(statuses));
        }
        return taskService.execute(
            new GetAllUserAuditTasksByStatusByDueDateCommand(userId, statuses,
                dueDate, offset, count));
    }

    @Override
    public QueryResult<UserAuditTask> getUserAuditTasks(int offset, int count,
        QueryComparator<UserAuditTask> comparator,
        Filter<UserAuditTask, ?>... filters) {
        try {
            return indexService
                .find(offset, count, comparator, UserAuditTask.class, filters);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<GroupAuditTask> getAllGroupAuditTasks(String groupIds,
        int offset, int count) {
        if (indexService != null) {
            return getGroupAuditTasks(offset, count, new TaskIdComparator<GroupAuditTask>(),
                new PotentialOwnerFilter<GroupAuditTask>(groupIds));
        }
        return taskService
            .execute(new GetAllGroupAuditTasksCommand(groupIds, offset, count));
    }

    @Override
    public List<GroupAuditTask> getAllGroupAuditTasksByStatus(String groupIds,
        List<String> statuses, int offset, int count) {
        if (indexService != null) {
            return getGroupAuditTasks(offset, count, new TaskIdComparator<GroupAuditTask>(),
                new PotentialOwnerFilter<GroupAuditTask>(groupIds),
                new StatusFilter<GroupAuditTask>(statuses));
        }
        return taskService.execute(
            new GetAllGroupAuditTasksByStatusCommand(groupIds, statuses, offset,
                count));
    }

    @Override
    public List<GroupAuditTask> getAllGroupAuditTasksByDueDate(String groupIds,
        Date dueDate, int offset, int count) {
        if (indexService != null) {
            return getGroupAuditTasks(offset, count, new TaskIdComparator<GroupAuditTask>(),
                new PotentialOwnerFilter<GroupAuditTask>(groupIds),
                new DueDateFilter<GroupAuditTask>(dueDate));
        }
        return taskService.execute(
            new GetAllGroupAuditTasksByDueDateCommand(groupIds, dueDate, offset,
                count));
    }

    @Override
    public List<GroupAuditTask> getAllGroupAuditTasksByStatusByDueDate(
        String groupIds, List<String> statuses, Date dueDate, int offset,
        int count) {
        if (indexService != null) {
            return getGroupAuditTasks(offset, count, new TaskIdComparator<GroupAuditTask>(),
                new PotentialOwnerFilter<GroupAuditTask>(groupIds),
                new DueDateFilter<GroupAuditTask>(dueDate),
                new StatusFilter<GroupAuditTask>(statuses));
        }
        return taskService.execute(
            new GetAllGroupAuditTasksByStatusByDueDateCommand(groupIds,
                statuses, dueDate, offset, count));
    }

    @Override
    public List<GroupAuditTask> getAllGroupAuditTasksByStatusByDueDateOptional(
        String groupIds, List<String> statuses, Date dueDate, int offset,int count) {
        //TODO: - same impl as above.
        if (indexService != null) {
            return getGroupAuditTasks(offset, count, new TaskIdComparator<GroupAuditTask>(),
                new PotentialOwnerFilter<GroupAuditTask>(groupIds),
                new DueDateFilter<GroupAuditTask>(dueDate),
                new StatusFilter<GroupAuditTask>(statuses));
        }
        return taskService.execute(
            new GetAllGroupAuditTasksByStatusByDueDateCommand(groupIds,
                statuses, dueDate, offset, count));
    }

    @Override
    public QueryResult<GroupAuditTask> getGroupAuditTasks(int offset, int count,
        QueryComparator<GroupAuditTask> comparator,
        Filter<GroupAuditTask, ?>... filters) {
        try {
            return indexService
                .find(offset, count, comparator, GroupAuditTask.class, filters);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<UserAuditTask> getAllUserAuditTasksAdmin(int offset,
        int count) {
        if (indexService != null) {
            return getUserAuditTasks(offset, count,
                new TaskIdComparator<UserAuditTask>());
        }
        return taskService
            .execute(new GetAllUserAuditTasksAdminCommand(offset, count));
    }

    @Override
    public List<GroupAuditTask> getAllGroupAuditTasksAdmin(int offset,
        int count) {
        if (indexService != null) {
            return getGroupAuditTasks(offset, count,
                new TaskIdComparator<GroupAuditTask>());
        }
        return taskService
            .execute(new GetAllGroupAuditTasksAdminCommand(offset, count));
    }

    @Override
    public List<HistoryAuditTask> getAllHistoryAuditTasks(int offset, int count) {
        if (indexService != null) {
            return getHistoryAuditTasks(offset, count,
                new TaskIdComparator<HistoryAuditTask>());
        }
        return taskService
            .execute(new GetAllHistoryAuditTasksCommand(offset, count));
    }

    @Override
    public List<HistoryAuditTask> getAllHistoryAuditTasksByUser(String userId,
        int offset, int count) {
        if (indexService != null) {
            return getHistoryAuditTasks(offset, count, new TaskIdComparator<HistoryAuditTask>(), new ActualOwnerFilter<HistoryAuditTask>(userId));
        }
        return taskService.execute(
            new GetAllHistoryAuditTasksByUserCommand(userId, offset, count));
    }

    @Override
    public QueryResult<HistoryAuditTask> getHistoryAuditTasks(int offset,
        int count, QueryComparator<HistoryAuditTask> comparator,
        Filter<HistoryAuditTask, ?>... filters) {
        try {
            return indexService
                .find(offset, count, comparator, HistoryAuditTask.class,
                    filters);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setTaskService(TaskService taskService) {
        this.taskService = (InternalTaskService) taskService;
    }

    public void setIndexService(IndexService indexService) {
        this.indexService = indexService;
    }

	public void setMultiThreadIndex(boolean mtIndex) {
		this.multiThreadIndex = mtIndex;
	}

	public boolean isMultiThreadIndex() {
		return multiThreadIndex;
	}
	
	public void startupIndexes() {
		this.taskIndexInitThread = new TaskIndexThread(taskService, indexService);
		taskIndexInitThread.start();
    	if (!isMultiThreadIndex()) {
    		try {
    			taskIndexInitThread.join();
    		} catch (InterruptedException ex) { /* do nothing */ }
    	}
    }
}
