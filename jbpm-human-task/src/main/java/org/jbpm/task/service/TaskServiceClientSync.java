/*
 * Copyright 2011 JBoss Inc..
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
package org.jbpm.task.service;

import java.util.List;
import java.util.Map;
import org.drools.RuleBase;
import org.drools.runtime.process.WorkItemManager;
import org.jbpm.eventmessaging.EventKey;
import org.jbpm.task.Attachment;
import org.jbpm.task.Comment;
import org.jbpm.task.Content;
import org.jbpm.task.Group;
import org.jbpm.task.OrganizationalEntity;
import org.jbpm.task.Task;
import org.jbpm.task.User;
import org.jbpm.task.query.DeadlineSummary;
import org.jbpm.task.query.TaskSummary;

/**
 *
 * @author salaboy
 * Synchronous behavior to interact with the task service
 */
public interface TaskServiceClient {

    void addAttachment(final long taskId, final Attachment attachment, final Content content);

    void addComment(final long taskId, final Comment comment);

    void addTask(final Task task, final ContentData contentData) throws CannotAddTaskException;

    void addUser(final User user);
    
    void addGroup(final Group group);

    void deleteAttachment(final long taskId, final long attachmentId, final long contentId);

    void deleteComment(final long taskId, final long commentId);

    Content getContent(final long contentId);
    
    List<?> query(final String qlString, final Integer size, final Integer offset);
    
    void registerForEvent(EventKey key, boolean remove, WorkItemManager manager);

    // Setters
   
    void setDocumentContent(final long taskId, final Content content);

    void setGlobals(final String type, final Map<String, Object> globals);

    void setRuleBase(final String type, final RuleBase ruleBase);

    // Get Tasks
    
    List<TaskSummary> getSubTasksAssignedAsPotentialOwner(final long parentId, final String userId, final String language);

    List<TaskSummary> getSubTasksByParent(final long parentId, final String language);

    Task getTask(final long taskId);

    Task getTaskByWorkItemId(final long workItemId);

    List<TaskSummary> getTasksAssignedAsBusinessAdministrator(final String userId, final String language);

    List<TaskSummary> getTasksAssignedAsExcludedOwner(final String userId, final String language);

    List<TaskSummary> getTasksAssignedAsPotentialOwner(final String userId, final String language);

    List<TaskSummary> getTasksAssignedAsPotentialOwner(final String userId, final List<String> groupIds, final String language);

    List<TaskSummary> getTasksAssignedAsPotentialOwnerByGroup(final String groupId, final String language);

    List<TaskSummary> getTasksAssignedAsRecipient(final String userId, final String language);

    List<TaskSummary> getTasksAssignedAsTaskInitiator(final String userId, final String language);

    List<TaskSummary> getTasksAssignedAsTaskStakeholder(final String userId, final String language);

    List<TaskSummary> getTasksOwned(final String userId, final String language);

    List<DeadlineSummary> getUnescalatedDeadlines();

    // Task Operations
     
    void fail(long taskId, String userId, FaultData faultData);

    void forward(long taskId, String userId, String targetEntityId);

    void skip(long taskId, String userId);
 
    void start(long taskId, String userId);

    void stop(long taskId, String userId);

    void suspend(long taskId, String userId);
    
    void resume(long taskId, String userId);
    
    void claim(long taskId, String userId);

    void claim(long taskId, String userId, List<String> groupIds);

    void complete(long taskId, String userId, ContentData outputData);
    
    void setPriority(final long taskId, final String userId, final int priority);
    
    void release(long taskId, String userId);

    void remove(long taskId, String userId);
    
    void setFault(final long taskId, final String userId, final FaultData faultContentData);
    
    void setOutput(final long taskId, final String userId, final ContentData outputContentData);
    
    void register(long taskId, String userId);
    
    void delegate(long taskId, String userId, String targetUserId);
    
    void nominate(long taskId, String userId, List<OrganizationalEntity> potentialOwners);

    void deleteFault(final long taskId, final String userId);

    void deleteOutput(final long taskId, final String userId);
    
    void nominateTask(final long taskId, String userId, final List<OrganizationalEntity> potentialOwners);
    
    void activate(long taskId, String userId);
    
    void taskOperation(final Operation operation, final long taskId, final String userId, final String targetEntityId, final ContentData data, List<String> groupIds) throws TaskException;
    
    //Connection Management
    
    boolean connect();

    boolean connect(String address, int port);

    void disconnect() throws Exception;
    
    void dispose();
    
}
