/**
 * Copyright 2010 JBoss Inc
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
package org.jbpm.task.service.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.drools.RuleBase;
import org.drools.runtime.process.WorkItemManager;

import org.jbpm.eventmessaging.EventKey;
import org.jbpm.eventmessaging.EventResponseHandler;
import org.jbpm.eventmessaging.Payload;
import org.jbpm.process.workitem.wsht.BlockingAddTaskResponseHandler;

import org.jbpm.process.workitem.wsht.BlockingGetTaskResponseHandler;
import org.jbpm.task.Attachment;
import org.jbpm.task.Comment;
import org.jbpm.task.Content;
import org.jbpm.task.Group;
import org.jbpm.task.OrganizationalEntity;
import org.jbpm.task.Status;
import org.jbpm.task.Task;
import org.jbpm.task.User;
import org.jbpm.task.event.TaskEvent;
import org.jbpm.task.query.DeadlineSummary;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.task.service.CannotAddTaskException;
import org.jbpm.task.service.ContentData;
import org.jbpm.task.service.FaultData;
import org.jbpm.task.service.Operation;
import org.jbpm.task.service.TaskClientConnector;
import org.jbpm.task.service.TaskClientHandler.GetContentResponseHandler;
import org.jbpm.task.service.TaskClientHandler.GetTaskResponseHandler;
import org.jbpm.task.service.TaskException;
import org.jbpm.task.service.TaskServiceClientSync;
import org.jbpm.task.service.responsehandlers.AbstractBaseResponseHandler;
import org.jbpm.task.service.responsehandlers.BlockingGetContentResponseHandler;
import org.jbpm.task.service.responsehandlers.BlockingQueryGenericResponseHandler;
import org.jbpm.task.service.responsehandlers.BlockingTaskOperationResponseHandler;
import org.jbpm.task.service.responsehandlers.BlockingTaskSummaryResponseHandler;

/*
 * This is a Synchronous implementation that hides the asycn nature of the default 
 * implementation just to provide a simple interface
 */

public class TaskServiceClientSyncImpl implements TaskServiceClientSync {

    private final TaskServiceClientAsyncImpl asyncTaskClient;

    public TaskServiceClientSyncImpl(TaskClientConnector connector) {
        asyncTaskClient = new TaskServiceClientAsyncImpl(connector);

    }

    public boolean connect() {
        return asyncTaskClient.connect();
    }

    public boolean connect(String address, int port) {
        return asyncTaskClient.connect(address, port);
    }

    public void disconnect() throws Exception {
        asyncTaskClient.disconnect();
    }

    public void addAttachment(long taskId, Attachment attachment, Content content) {
        asyncTaskClient.addAttachment(taskId, attachment, content, null);
    }

    public void addComment(long taskId, Comment comment) {
        asyncTaskClient.addComment(taskId, comment, null);
    }

    public void addGroup(Group group) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addTask(Task task, ContentData contentData) throws CannotAddTaskException {
        BlockingAddTaskResponseHandler handler = new BlockingAddTaskResponseHandler();
        asyncTaskClient.addTask(task, contentData, handler);
        do {
            if (handler.getError() != null) {
                throw handler.getError();
            }
        } while (!handler.isDone());
        task.setId(handler.getTaskId());
    }

    public void addUser(User user) {
        //@TODO: add this method to the async impl
    }

    public void deleteAttachment(long taskId, long attachmentId, long contentId) {
        asyncTaskClient.deleteAttachment(taskId, attachmentId, contentId, null);
    }

    public void deleteComment(long taskId, long commentId) {
        asyncTaskClient.deleteComment(taskId, commentId, null);
    }

    public void deleteFault(long taskId, String userId) {
        asyncTaskClient.deleteFault(taskId, userId, null);
    }

    public void deleteOutput(long taskId, String userId) {
        asyncTaskClient.deleteOutput(taskId, userId, null);
    }

    public void dispose() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Content getContent(long contentId) {
        BlockingGetContentResponseHandler handler = new BlockingGetContentResponseHandler();
        asyncTaskClient.getContent(contentId, handler);
        return handler.getContent();
    }

    public List<TaskSummary> getSubTasksAssignedAsPotentialOwner(long parentId, String userId, String language) {
        BlockingTaskSummaryResponseHandler handler = new BlockingTaskSummaryResponseHandler();
        asyncTaskClient.getSubTasksAssignedAsPotentialOwner(parentId, userId, language, handler);
        return handler.getResults();
    }

    public List<TaskSummary> getSubTasksByParent(long parentId, String language) {
        BlockingTaskSummaryResponseHandler handler = new BlockingTaskSummaryResponseHandler();
        asyncTaskClient.getSubTasksByParent(parentId, handler);
        return handler.getResults();
    }

    public Task getTask(long taskId) {
        BlockingGetTaskResponseHandler handler = new BlockingGetTaskResponseHandler();
        asyncTaskClient.getTask(taskId, handler);
        do {
            if (handler.getError() != null) {
                throw handler.getError();
            }
        } while (!handler.isDone());
        return handler.getTask();
    }

    public Task getTaskByWorkItemId(long workItemId) {
        BlockingGetTaskResponseHandler handler = new BlockingGetTaskResponseHandler();
        asyncTaskClient.getTaskByWorkItemId(workItemId, handler);
        do {
            if (handler.getError() != null) {
                throw handler.getError();
            }
        } while (!handler.isDone());
        return handler.getTask();
    }

    public List<TaskSummary> getTasksAssignedAsBusinessAdministrator(String userId, String language) {
        BlockingTaskSummaryResponseHandler handler = new BlockingTaskSummaryResponseHandler();
        asyncTaskClient.getTasksAssignedAsBusinessAdministrator(userId, language, handler);
        do {
            if (handler.getError() != null) {
                throw handler.getError();
            }
        } while (!handler.isDone());
        return handler.getResults();
    }

    public List<TaskSummary> getTasksAssignedAsExcludedOwner(String userId, String language) {
        BlockingTaskSummaryResponseHandler handler = new BlockingTaskSummaryResponseHandler();
        asyncTaskClient.getTasksAssignedAsExcludedOwner(userId, language, handler);
        do {
            if (handler.getError() != null) {
                throw handler.getError();
            }
        } while (!handler.isDone());
        return handler.getResults();
    }

    public List<TaskSummary> getTasksAssignedAsPotentialOwner(String userId, String language) {
        BlockingTaskSummaryResponseHandler handler = new BlockingTaskSummaryResponseHandler();
        asyncTaskClient.getTasksAssignedAsPotentialOwner(userId, language, handler);
        do {
            if (handler.getError() != null) {
                throw handler.getError();
            }
        } while (!handler.isDone());
        return handler.getResults();
    }

    public List<TaskSummary> getTasksAssignedAsPotentialOwner(String userId, List<String> groupIds, String language) {
        BlockingTaskSummaryResponseHandler handler = new BlockingTaskSummaryResponseHandler();
        asyncTaskClient.getTasksAssignedAsPotentialOwner(userId, groupIds, language, handler);
        do {
            if (handler.getError() != null) {
                throw handler.getError();
            }
        } while (!handler.isDone());
        return handler.getResults();
    }

    public List<TaskSummary> getTasksAssignedAsPotentialOwnerByGroup(String groupId, String language) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<TaskSummary> getTasksAssignedAsRecipient(String userId, String language) {
        BlockingTaskSummaryResponseHandler handler = new BlockingTaskSummaryResponseHandler();
        asyncTaskClient.getTasksAssignedAsRecipient(userId, language, handler);
        do {
            if (handler.getError() != null) {
                throw handler.getError();
            }
        } while (!handler.isDone());
        return handler.getResults();
    }

    public List<TaskSummary> getTasksAssignedAsTaskInitiator(String userId, String language) {
        BlockingTaskSummaryResponseHandler handler = new BlockingTaskSummaryResponseHandler();
        asyncTaskClient.getTasksAssignedAsTaskInitiator(userId, language, handler);
        do {
            if (handler.getError() != null) {
                throw handler.getError();
            }
        } while (!handler.isDone());
        return handler.getResults();
    }

    public List<TaskSummary> getTasksAssignedAsTaskStakeholder(String userId, String language) {
        BlockingTaskSummaryResponseHandler handler = new BlockingTaskSummaryResponseHandler();
        asyncTaskClient.getTasksAssignedAsTaskStakeholder(userId, language, handler);
        do {
            if (handler.getError() != null) {
                throw handler.getError();
            }
        } while (!handler.isDone());
        return handler.getResults();
    }

    public List<TaskSummary> getTasksOwned(String userId, String language) {
        BlockingTaskSummaryResponseHandler handler = new BlockingTaskSummaryResponseHandler();
        asyncTaskClient.getTasksOwned(userId, language, handler);
        do {
            if (handler.getError() != null) {
                throw handler.getError();
            }
        } while (!handler.isDone());
        return handler.getResults();
    }

    public List<DeadlineSummary> getUnescalatedDeadlines() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void nominateTask(long taskId, String userId, List<OrganizationalEntity> potentialOwners) {
        asyncTaskClient.nominate(taskId, userId, potentialOwners, null);
    }

    public List<?> query(String qlString, Integer size, Integer offset) {
        BlockingQueryGenericResponseHandler handler = new BlockingQueryGenericResponseHandler();
        asyncTaskClient.query(qlString, size, offset, handler);
        do {
            if (handler.getError() != null) {
                throw handler.getError();
            }
        } while (!handler.isDone());
        return handler.getResults();
    }

    public void setDocumentContent(long taskId, Content content) {
        asyncTaskClient.setDocumentContent(taskId, content, null);
    }

    public void setFault(long taskId, String userId, FaultData faultContentData) {
        asyncTaskClient.setFault(taskId, userId, faultContentData, null);
    }

    public void setGlobals(String type, Map<String, Object> globals) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setOutput(long taskId, String userId, ContentData outputContentData) {
        asyncTaskClient.setOutput(taskId, userId, outputContentData, null);
    }

    public void setPriority(long taskId, String userId, int priority) {
        asyncTaskClient.setPriority(taskId, userId, priority, null);
    }

    public void setRuleBase(String type, RuleBase ruleBase) {
    }

    public void taskOperation(Operation operation, long taskId, String userId, String targetEntityId, ContentData data, List<String> groupIds) throws TaskException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void fail(long taskId, String userId, FaultData faultData) {
        asyncTaskClient.fail(taskId, userId, faultData, null);
    }

    public void forward(long taskId, String userId, String targetEntityId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void skip(long taskId, String userId) {
        asyncTaskClient.skip(taskId, userId, null);
    }

    public void start(long taskId, String userId) {
        BlockingTaskOperationResponseHandler handler = new BlockingTaskOperationResponseHandler();
        asyncTaskClient.start(taskId, userId, handler);
        do {
            if (handler.getError() != null) {
                throw handler.getError();
            }
        } while (!handler.isDone());


    }

    public void stop(long taskId, String userId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void suspend(long taskId, String userId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void resume(long taskId, String userId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void claim(long taskId, String userId) {
        BlockingTaskOperationResponseHandler handler = new BlockingTaskOperationResponseHandler();
        asyncTaskClient.claim(taskId, userId, handler);
        do {
            if (handler.getError() != null) {
                throw handler.getError();
            }
        } while (!handler.isDone());
    }

    public void claim(long taskId, String userId, List<String> groupIds) {
        BlockingTaskOperationResponseHandler handler = new BlockingTaskOperationResponseHandler();
        asyncTaskClient.claim(taskId, userId, groupIds, handler);
        do {
            if (handler.getError() != null) {
                throw handler.getError();
            }
        } while (!handler.isDone());
        if (handler.getError() != null) {
            throw handler.getError();
        }
    }

    public void complete(long taskId, String userId, ContentData outputData) {
        asyncTaskClient.complete(taskId, userId, outputData, null);
    }

    public void release(long taskId, String userId) {
        asyncTaskClient.release(taskId, userId, null);
    }

    public void remove(long taskId, String userId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void register(long taskId, String userId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void delegate(long taskId, String userId, String targetUserId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void nominate(long taskId, String userId, List<OrganizationalEntity> potentialOwners) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void activate(long taskId, String userId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void registerForEvent(EventKey key, boolean remove, WorkItemManager manager) {
        TaskCompletedHandler handler = new TaskCompletedHandler(manager, asyncTaskClient);
        asyncTaskClient.registerForEvent(key, remove, handler);

    }

    public boolean isConnected() {
        return asyncTaskClient.isConnected();
    }

    private static class TaskCompletedHandler extends AbstractBaseResponseHandler implements EventResponseHandler {

        private WorkItemManager manager;
        private TaskServiceClientAsyncImpl client;

        public TaskCompletedHandler(WorkItemManager manager, TaskServiceClientAsyncImpl client) {
            this.manager = manager;
            this.client = client;
        }

        public void execute(Payload payload) {
            TaskEvent event = (TaskEvent) payload.get();
            long taskId = event.getTaskId();
            System.out.println("Task completed " + taskId);
            GetTaskResponseHandler getTaskResponseHandler =
                    new GetCompletedTaskResponseHandler(manager, client);
            client.getTask(taskId, getTaskResponseHandler);
        }

        public boolean isRemove() {
            return false;
        }
    }

    private static class GetCompletedTaskResponseHandler extends AbstractBaseResponseHandler implements GetTaskResponseHandler {

        private WorkItemManager manager;
        private TaskServiceClientAsyncImpl client;

        public GetCompletedTaskResponseHandler(WorkItemManager manager, TaskServiceClientAsyncImpl client) {
            this.manager = manager;
            this.client = client;
        }

        public void execute(Task task) {
            long workItemId = task.getTaskData().getWorkItemId();
            if (task.getTaskData().getStatus() == Status.Completed) {
                String userId = task.getTaskData().getActualOwner().getId();
                Map<String, Object> results = new HashMap<String, Object>();
                results.put("ActorId", userId);
                long contentId = task.getTaskData().getOutputContentId();
                if (contentId != -1) {
                    GetContentResponseHandler getContentResponseHandler =
                            new GetResultContentResponseHandler(manager, task, results);
                    client.getContent(contentId, getContentResponseHandler);
                } else {
                    manager.completeWorkItem(workItemId, results);
                }
            } else {
                manager.abortWorkItem(workItemId);
            }
        }
    }

    private static class GetResultContentResponseHandler extends AbstractBaseResponseHandler implements GetContentResponseHandler {

        private WorkItemManager manager;
        private Task task;
        private Map<String, Object> results;

        public GetResultContentResponseHandler(WorkItemManager manager, Task task, Map<String, Object> results) {
            this.manager = manager;
            this.task = task;
            this.results = results;
        }

        public void execute(Content content) {
            ByteArrayInputStream bis = new ByteArrayInputStream(content.getContent());
            ObjectInputStream in;
            try {
                in = new ObjectInputStream(bis);
                Object result = in.readObject();
                in.close();
                results.put("Result", result);
                if (result instanceof Map) {
                    Map<?, ?> map = (Map) result;
                    for (Map.Entry<?, ?> entry : map.entrySet()) {
                        if (entry.getKey() instanceof String) {
                            results.put((String) entry.getKey(), entry.getValue());
                        }
                    }
                }
                manager.completeWorkItem(task.getTaskData().getWorkItemId(), results);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}