/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jbpm.task.service.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.drools.RuleBase;
import org.drools.SystemEventListenerFactory;
import org.drools.runtime.process.WorkItemManager;
import org.jbpm.eventmessaging.EventKey;
import org.jbpm.eventmessaging.EventTriggerTransport;
import org.jbpm.eventmessaging.Payload;
import org.jbpm.task.Attachment;
import org.jbpm.task.Comment;
import org.jbpm.task.Content;
import org.jbpm.task.Group;
import org.jbpm.task.OrganizationalEntity;
import org.jbpm.task.Status;
import org.jbpm.task.Task;
import org.jbpm.task.User;
import org.jbpm.task.event.TaskCompletedEvent;
import org.jbpm.task.event.TaskFailedEvent;
import org.jbpm.task.event.TaskSkippedEvent;
import org.jbpm.task.query.DeadlineSummary;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.task.service.CannotAddTaskException;
import org.jbpm.task.service.ContentData;
import org.jbpm.task.service.FaultData;
import org.jbpm.task.service.Operation;
import org.jbpm.task.service.TaskException;
import org.jbpm.task.service.TaskService;
import org.jbpm.task.service.TaskServiceClientSync;
import org.jbpm.task.service.TaskServiceSession;

/**
 *
 * @author salaboy
 */
public class TaskServiceClientSyncLocalImpl implements TaskServiceClientSync{
    private TaskServiceSession session;
    
    private EntityManagerFactory emf;
    private static TaskServiceClientSyncLocalImpl instance;
    
    public static TaskServiceClientSyncLocalImpl getInstance(){
        if(instance == null){
            instance = new TaskServiceClientSyncLocalImpl();
        }
        return instance;
    }
    
    
    public TaskServiceClientSyncLocalImpl(){
        this(Persistence.createEntityManagerFactory("org.jbpm.task"));
    }
    
    public TaskServiceClientSyncLocalImpl(EntityManagerFactory emf) {
        
        this.emf = emf;
        TaskService taskService = new TaskService(emf, SystemEventListenerFactory.getSystemEventListener());
        session = taskService.createSession();
        
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
        return session.getTasksAssignedAsPotentialOwner(userId,  language);
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
        session.taskOperation(Operation.Fail, taskId, userId, null, faultData, null);
    }

    public void forward(long taskId, String userId, String targetEntityId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void skip(long taskId, String userId) {
        session.taskOperation(Operation.Skip, taskId, userId, null, null, null);
    }

    public void start(long taskId, String userId) {
        session.taskOperation(Operation.Start, taskId, userId, null, null, null);
    }

    public void stop(long taskId, String userId) {
        session.taskOperation(Operation.Stop, taskId, userId, null, null, null);
    }

    public void suspend(long taskId, String userId) {
        session.taskOperation(Operation.Suspend, taskId, userId, null, null, null);
    }

    public void resume(long taskId, String userId) {
        session.taskOperation(Operation.Resume, taskId, userId, null, null, null);
    }

    public void claim(long taskId, String userId) {
        session.taskOperation(Operation.Claim, taskId, userId, null, null, null);
    }

    public void claim(long taskId, String userId, List<String> groupIds) {
       session.taskOperation(Operation.Claim, taskId, userId, null, null, groupIds);
               //claim(taskId, userId, groupIds);
    }

    public void complete(long taskId, String userId, ContentData outputData) {
        session.taskOperation(Operation.Complete, taskId, userId, null, outputData, null);
                //complete(taskId, userId, outputData);
    }

    public void release(long taskId, String userId) {
        session.taskOperation(Operation.Release, taskId, userId, null, null, null);
    }

    public void remove(long taskId, String userId) {
        session.taskOperation(Operation.Remove, taskId, userId, null, null, null);
    }

    public void register(long taskId, String userId) {
        session.taskOperation(Operation.Register, taskId, userId, null, null, null);
    }

    public void delegate(long taskId, String userId, String targetUserId) {
        session.taskOperation(Operation.Delegate, taskId, userId, targetUserId, null, null);
    }

    public void nominate(long taskId, String userId, List<OrganizationalEntity> potentialOwners) {
        //session.taskOperation(Operation.N, taskId, userId, userId, null, null);
        //OPERATION NOMINATE MISSING???
    }

    public void activate(long taskId, String userId) {
        session.taskOperation(Operation.Activate, taskId, userId, null, null, null);
    }

    public boolean connect() {
        return true;
    }

    public boolean connect(String address, int port) {
        return true;
    }

    public void disconnect() throws Exception {
        //do nothing
    }

    public void registerForEvent(EventKey key, boolean remove, WorkItemManager manager) {
        SimpleEventTransport transport = new SimpleEventTransport(session, manager, remove);
        session.getService().getEventKeys().register(key, transport);
        
    }

    public boolean isConnected() {
        return true;
    }
    
    
    
    
    private static class SimpleEventTransport implements EventTriggerTransport {

        private boolean remove;
        private WorkItemManager manager;
        private TaskServiceSession session;

        public SimpleEventTransport(TaskServiceSession session, WorkItemManager manager, boolean remove) {
            this.session = session;
            this.manager = manager;
            this.remove = remove;
        }

        public void trigger(Payload payload) {
            if (payload.get() instanceof TaskFailedEvent) {
                Task task = session.getTask(((TaskFailedEvent) payload.get()).getTaskId());
                manager.abortWorkItem(task.getTaskData().getWorkItemId());
                return;
            }
            if (payload.get() instanceof TaskSkippedEvent) {
                Task task = session.getTask(((TaskSkippedEvent) payload.get()).getTaskId());
                manager.abortWorkItem(task.getTaskData().getWorkItemId());
                return;
            }
            if (payload.get() instanceof TaskCompletedEvent) {
                Task task = session.getTask(((TaskCompletedEvent) payload.get()).getTaskId());

                task.getTaskData().setStatus(Status.Completed);
                String userId = task.getTaskData().getActualOwner().getId();
                Map<String, Object> results = new HashMap<String, Object>();
                results.put("ActorId", userId);
                long contentId = task.getTaskData().getOutputContentId();
                if (contentId != -1) {
                    Content content = session.getContent(contentId);
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

                } else {
                    manager.completeWorkItem(task.getTaskData().getWorkItemId(), results);
                }

                return;
            }

        }

        public boolean isRemove() {
            return remove;
        }
    }
    
    
}
