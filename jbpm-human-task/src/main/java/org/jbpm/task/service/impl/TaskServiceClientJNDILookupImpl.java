/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jbpm.task.service.impl;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
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
import org.jbpm.task.service.CannotAddTaskException;
import org.jbpm.task.service.ContentData;
import org.jbpm.task.service.FaultData;
import org.jbpm.task.service.Operation;
import org.jbpm.task.service.TaskException;
import org.jbpm.task.service.TaskServiceClientSync;

/**
 *
 * @author salaboy
 */
public class TaskServiceClientJNDILookupImpl implements TaskServiceClientSync {

    private TaskServiceClientSyncLocalImpl session;

    private static TaskServiceClientJNDILookupImpl instance;
    
    public static TaskServiceClientJNDILookupImpl getInstance(){
        if(instance == null){
            instance = new TaskServiceClientJNDILookupImpl();
        }
        return instance;
    }
    
    public TaskServiceClientJNDILookupImpl() {
        try {
            
           
            Context initCtx = new InitialContext();
            
            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            
            session = (TaskServiceClientSyncLocalImpl) envCtx.lookup("bean/TaskServiceClientFactory");
            
        } catch (NamingException ex) {
            
            Logger.getLogger(TaskServiceClientJNDILookupImpl.class.getName()).log(Level.SEVERE, null, ex);
            
        }



    }

    public void addAttachment(long taskId, Attachment attachment, Content content) {
        session.addAttachment(taskId, attachment, content);
    }

    public void addComment(long taskId, Comment comment) {
        session.addComment(taskId, comment);
    }

    public void addGroup(Group group) {
        session.addGroup(group);
    }

    public void addTask(Task task, ContentData contentData) throws CannotAddTaskException {
        session.addTask(task, contentData);
    }

    public void addUser(User user) {
        session.addUser(user);
    }

    public void deleteAttachment(long taskId, long attachmentId, long contentId) {
        session.deleteAttachment(taskId, attachmentId, contentId);
    }

    public void deleteComment(long taskId, long commentId) {
        session.deleteComment(taskId, commentId);
    }

    public void deleteFault(long taskId, String userId) {
        session.deleteFault(taskId, userId);
    }

    public void deleteOutput(long taskId, String userId) {
        session.deleteOutput(taskId, userId);
    }

    public void dispose() {
        session.dispose();
    }

    public Content getContent(long contentId) {
        return session.getContent(contentId);
    }

    public List<TaskSummary> getSubTasksAssignedAsPotentialOwner(long parentId, String userId, String language) {
        return session.getSubTasksAssignedAsPotentialOwner(parentId, userId, language);
    }

    public List<TaskSummary> getSubTasksByParent(long parentId, String language) {
        return session.getSubTasksAssignedAsPotentialOwner(parentId, language, language);
    }

    public Task getTask(long taskId) {
        return session.getTask(taskId);
    }

    public Task getTaskByWorkItemId(long workItemId) {
        return session.getTaskByWorkItemId(workItemId);
    }

    public List<TaskSummary> getTasksAssignedAsBusinessAdministrator(String userId, String language) {
        return session.getTasksAssignedAsBusinessAdministrator(userId, language);
    }

    public List<TaskSummary> getTasksAssignedAsExcludedOwner(String userId, String language) {
        return session.getTasksAssignedAsExcludedOwner(userId, language);
    }

    public List<TaskSummary> getTasksAssignedAsPotentialOwner(String userId, String language) {
        return session.getTasksAssignedAsPotentialOwner(userId, language);
    }

    public List<TaskSummary> getTasksAssignedAsPotentialOwner(String userId, List<String> groupIds, String language) {
        return session.getTasksAssignedAsPotentialOwner(userId, groupIds, language);
    }

    public List<TaskSummary> getTasksAssignedAsPotentialOwnerByGroup(String groupId, String language) {
        return session.getTasksAssignedAsPotentialOwnerByGroup(groupId, language);
    }

    public List<TaskSummary> getTasksAssignedAsRecipient(String userId, String language) {
        return session.getTasksAssignedAsRecipient(userId, language);
    }

    public List<TaskSummary> getTasksAssignedAsTaskInitiator(String userId, String language) {
        return session.getTasksAssignedAsTaskInitiator(userId, language);
    }

    public List<TaskSummary> getTasksAssignedAsTaskStakeholder(String userId, String language) {
        return session.getTasksAssignedAsTaskStakeholder(userId, language);
    }

    public List<TaskSummary> getTasksOwned(String userId, String language) {
        return session.getTasksOwned(userId, language);
    }

    public List<DeadlineSummary> getUnescalatedDeadlines() {
        return session.getUnescalatedDeadlines();
    }

    public void nominateTask(long taskId, String userId, List<OrganizationalEntity> potentialOwners) {
        session.nominateTask(taskId, userId, potentialOwners);
    }

    public List<?> query(String qlString, Integer size, Integer offset) {
        return session.query(qlString, size, offset);
    }

    public void setDocumentContent(long taskId, Content content) {
        session.setDocumentContent(taskId, content);
    }

    public void setFault(long taskId, String userId, FaultData faultContentData) {
        session.setFault(taskId, userId, faultContentData);
    }

    public void setGlobals(String type, Map<String, Object> globals) {
        session.setGlobals(type, globals);
    }

    public void setOutput(long taskId, String userId, ContentData outputContentData) {
        session.setOutput(taskId, userId, outputContentData);
    }

    public void setPriority(long taskId, String userId, int priority) {
        session.setPriority(taskId, userId, priority);
    }

    public void taskOperation(Operation operation, long taskId, String userId, String targetEntityId, ContentData data, List<String> groupIds) throws TaskException {
        session.taskOperation(operation, taskId, userId, targetEntityId, data, groupIds);
    }

    public void setRuleBase(String type, RuleBase ruleBase) {
        session.setRuleBase(type, ruleBase);
    }

    public void fail(long taskId, String userId, FaultData faultData) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void forward(long taskId, String userId, String targetEntityId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void skip(long taskId, String userId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void start(long taskId, String userId) {
        throw new UnsupportedOperationException("Not supported yet.");
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void claim(long taskId, String userId, List<String> groupIds) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void complete(long taskId, String userId, ContentData outputData) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void release(long taskId, String userId) {
        throw new UnsupportedOperationException("Not supported yet.");
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

    public boolean connect() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean connect(String address, int port) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void disconnect() throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void registerForEvent(EventKey key, boolean remove, WorkItemManager manager) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isConnected() {
        return true;
    }
}
