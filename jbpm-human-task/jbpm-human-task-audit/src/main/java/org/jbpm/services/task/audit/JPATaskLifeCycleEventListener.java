/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jbpm.services.task.audit;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.EntityManagerFactory;

import org.jbpm.persistence.api.integration.EventManagerProvider;
import org.jbpm.persistence.api.integration.PersistenceEventManager;
import org.jbpm.persistence.api.integration.model.TaskOperationView;
import org.jbpm.process.audit.ArchiveLoggerProvider;
import org.jbpm.process.audit.AuditLoggerArchiveTreat;
import org.jbpm.services.task.audit.impl.model.AuditTaskImpl;
import org.jbpm.services.task.audit.impl.model.TaskEventImpl;
import org.jbpm.services.task.audit.variable.TaskIndexerManager;
import org.jbpm.services.task.lifecycle.listeners.TaskLifeCycleEventListener;
import org.jbpm.services.task.persistence.JPATaskPersistenceContext;
import org.jbpm.services.task.persistence.PersistableEventListener;
import org.jbpm.services.task.utils.ClassUtil;
import org.kie.api.task.TaskEvent;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Task;
import org.kie.internal.task.api.TaskContext;
import org.kie.internal.task.api.TaskPersistenceContext;
import org.kie.internal.task.api.TaskVariable;
import org.kie.internal.task.api.TaskVariable.VariableType;
import org.kie.internal.task.api.model.TaskEvent.TaskEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class JPATaskLifeCycleEventListener extends PersistableEventListener implements TaskLifeCycleEventListener, AuditLoggerArchiveTreat {

    public static final String METADATA_TASK_EVENT = "TASK_EVENT";
    public static final String METADATA_AUDIT_TASK = "TASK_AUDIT_EVENT";
    public static final String METADATA_VAR_EVENT = "TASK_VAR_EVENT";

    private static final Logger logger = LoggerFactory.getLogger(JPATaskLifeCycleEventListener.class);
    private List<ArchiveLoggerProvider> archiveLoggerProviders;

    private static final int TASK_DESCRIPTION_LENGTH = Integer.parseInt(System.getProperty("org.jbpm.ht.task.description.length", "255"));

    
    public JPATaskLifeCycleEventListener(boolean flag) {
        super(null);
        archiveLoggerProviders = initArchiveLoggerProvider();
    }
    
    public JPATaskLifeCycleEventListener(EntityManagerFactory emf) {
        super(emf);
        archiveLoggerProviders = initArchiveLoggerProvider();
    }

    @Override
    public void afterTaskStartedEvent(TaskEvent event) {
        Task ti = event.getTask();
        TaskPersistenceContext persistenceContext = getPersistenceContext(((TaskContext)event.getTaskContext()).getPersistenceContext());
        try {
            TaskEventImpl taskEventImpl = new TaskEventImpl(event, TaskEventType.STARTED);
            event.getMetadata().put(METADATA_TASK_EVENT, taskEventImpl);
            persistenceContext.persist(taskEventImpl);
            createTaskOperationView(event, TaskEventType.STARTED);

            AuditTaskImpl auditTaskImpl = getAuditTask(event, persistenceContext, ti);
            if (auditTaskImpl == null) {
                logger.warn("Unable find audit task entry for task id {} '{}', skipping audit task update", ti.getId(), ti.getName());
                return;
            }
            auditTaskImpl.setStatus(ti.getTaskData().getStatus().name());
            auditTaskImpl.setActualOwner(getActualOwner(ti));
            auditTaskImpl.setLastModificationDate(event.getEventDate());
            event.getMetadata().put(METADATA_AUDIT_TASK, auditTaskImpl);
            persistenceContext.merge(auditTaskImpl);
        } finally {
            cleanup(persistenceContext);
        }
    }

    @Override
    public void afterTaskActivatedEvent(TaskEvent event) {
        Task ti = event.getTask();
        TaskPersistenceContext persistenceContext = getPersistenceContext(((TaskContext)event.getTaskContext()).getPersistenceContext());
        try {            
            TaskEventImpl taskEventImpl = new TaskEventImpl(event, TaskEventType.ACTIVATED);
            event.getMetadata().put(METADATA_TASK_EVENT, taskEventImpl);
            persistenceContext.persist(taskEventImpl);
            createTaskOperationView(event, TaskEventType.ACTIVATED);

            AuditTaskImpl auditTaskImpl = getAuditTask(event, persistenceContext, ti);
            if (auditTaskImpl == null) {
                logger.warn("Unable find audit task entry for task id {} '{}', skipping audit task update", ti.getId(), ti.getName());
                return;
            }
            auditTaskImpl.setStatus(ti.getTaskData().getStatus().name());
            auditTaskImpl.setActualOwner(getActualOwner(ti));
            auditTaskImpl.setDescription(ti.getDescription());    
            auditTaskImpl.setLastModificationDate(event.getEventDate());
            event.getMetadata().put(METADATA_AUDIT_TASK, auditTaskImpl);
            persistenceContext.merge(auditTaskImpl);
        } finally {
            cleanup(persistenceContext);
        }
    }

    @Override
    public void afterTaskClaimedEvent(TaskEvent event) {
        Task ti = event.getTask();
        TaskPersistenceContext persistenceContext = getPersistenceContext(((TaskContext)event.getTaskContext()).getPersistenceContext());
        try {            
            TaskEventImpl taskEventImpl = new TaskEventImpl(event, TaskEventType.CLAIMED);
            event.getMetadata().put(METADATA_TASK_EVENT, taskEventImpl);
            persistenceContext.persist(taskEventImpl);
            createTaskOperationView(event, TaskEventType.CLAIMED);

            
            AuditTaskImpl auditTaskImpl = getAuditTask(event, persistenceContext, ti);
            if (auditTaskImpl == null) {
                logger.warn("Unable find audit task entry for task id {} '{}', skipping audit task update", ti.getId(), ti.getName());
                return;
            }
            auditTaskImpl.setStatus(ti.getTaskData().getStatus().name());
            auditTaskImpl.setActualOwner(getActualOwner(ti));
            auditTaskImpl.setDescription(ti.getDescription());
            auditTaskImpl.setLastModificationDate(event.getEventDate());
            event.getMetadata().put(METADATA_AUDIT_TASK, auditTaskImpl);
            persistenceContext.merge(auditTaskImpl);
        } finally {
            cleanup(persistenceContext);
        }
    }

    @Override
    public void afterTaskSkippedEvent(TaskEvent event) {
        Task ti = event.getTask();
        TaskPersistenceContext persistenceContext = getPersistenceContext(((TaskContext)event.getTaskContext()).getPersistenceContext());
        try {            
            TaskEventImpl taskEventImpl = new TaskEventImpl(event, TaskEventType.SKIPPED);
            event.getMetadata().put(METADATA_TASK_EVENT, taskEventImpl);
            persistenceContext.persist(taskEventImpl);
            createTaskOperationView (event, TaskEventType.SKIPPED);
           
            AuditTaskImpl auditTaskImpl = getAuditTask(event, persistenceContext, ti);
            if (auditTaskImpl == null) {
                logger.warn("Unable find audit task entry for task id {} '{}', skipping audit task update", ti.getId(), ti.getName());
                return;
            }
            auditTaskImpl.setStatus(ti.getTaskData().getStatus().name());
            auditTaskImpl.setActualOwner(getActualOwner(ti));
            auditTaskImpl.setDescription(ti.getDescription());
            auditTaskImpl.setLastModificationDate(event.getEventDate());
            event.getMetadata().put(METADATA_AUDIT_TASK, auditTaskImpl);
            persistenceContext.merge(auditTaskImpl);
            
        } finally {
            cleanup(persistenceContext);
        }
        
    }

    @Override
    public void afterTaskStoppedEvent(TaskEvent event) {
        Task ti = event.getTask();
        TaskPersistenceContext persistenceContext = getPersistenceContext(((TaskContext)event.getTaskContext()).getPersistenceContext());
        try {            
            TaskEventImpl taskEventImpl = new TaskEventImpl(event, TaskEventType.STOPPED);
            event.getMetadata().put(METADATA_TASK_EVENT, taskEventImpl);
            persistenceContext.persist(taskEventImpl);
            createTaskOperationView (event, TaskEventType.STOPPED);

            AuditTaskImpl auditTaskImpl = getAuditTask(event, persistenceContext, ti);
            if (auditTaskImpl == null) {
                logger.warn("Unable find audit task entry for task id {} '{}', skipping audit task update", ti.getId(), ti.getName());
                return;
            }
            auditTaskImpl.setDescription(ti.getDescription());
            auditTaskImpl.setStatus(ti.getTaskData().getStatus().name());
            auditTaskImpl.setActualOwner(getActualOwner(ti));
            auditTaskImpl.setLastModificationDate(event.getEventDate());
            event.getMetadata().put(METADATA_AUDIT_TASK, auditTaskImpl);
            persistenceContext.merge(auditTaskImpl);
        } finally {
            cleanup(persistenceContext);
        }

    }

    @Override
    public void afterTaskCompletedEvent(TaskEvent event) {
        Task ti = event.getTask();
        TaskPersistenceContext persistenceContext = getPersistenceContext(((TaskContext)event.getTaskContext()).getPersistenceContext());
        try {
            TaskEventImpl taskEventImpl = new TaskEventImpl(event, TaskEventType.COMPLETED);

            event.getMetadata().put(METADATA_TASK_EVENT, taskEventImpl);
            archiveLoggerProviders.forEach(e -> e.archive(((JPATaskPersistenceContext) persistenceContext).getEntityManager(), taskEventImpl));
            persistenceContext.persist(taskEventImpl);
            createTaskOperationView (event, TaskEventType.COMPLETED);

            AuditTaskImpl auditTaskImpl = getAuditTask(event, persistenceContext, ti);
            if (auditTaskImpl == null) {
                logger.warn("Unable find audit task entry for task id {} '{}', skipping audit task update", ti.getId(), ti.getName());
                return;
            }

            auditTaskImpl.setStatus(ti.getTaskData().getStatus().name());
            auditTaskImpl.setActualOwner(getActualOwner(ti));
            auditTaskImpl.setLastModificationDate(event.getEventDate());
            event.getMetadata().put(METADATA_AUDIT_TASK, auditTaskImpl);
            archiveLoggerProviders.forEach(e -> e.archive(((JPATaskPersistenceContext) persistenceContext).getEntityManager(), auditTaskImpl));
            persistenceContext.merge(auditTaskImpl);
        } finally {
            cleanup(persistenceContext);
        }
    }

    @Override
    public void afterTaskFailedEvent(TaskEvent event) {
        Task ti = event.getTask();
        TaskPersistenceContext persistenceContext = getPersistenceContext(((TaskContext)event.getTaskContext()).getPersistenceContext());
        try {
            TaskEventImpl taskEventImpl = new TaskEventImpl(event, TaskEventType.FAILED);
            event.getMetadata().put(METADATA_TASK_EVENT, taskEventImpl);
            persistenceContext.persist(taskEventImpl);
            createTaskOperationView (event, TaskEventType.FAILED);

            AuditTaskImpl auditTaskImpl = getAuditTask(event, persistenceContext, ti);
            if (auditTaskImpl == null) {
                logger.warn("Unable find audit task entry for task id {} '{}', skipping audit task update", ti.getId(), ti.getName());
                return;
            }
            ;
            auditTaskImpl.setStatus(ti.getTaskData().getStatus().name());
            auditTaskImpl.setActualOwner(getActualOwner(ti));
            auditTaskImpl.setLastModificationDate(event.getEventDate());
            event.getMetadata().put(METADATA_AUDIT_TASK, auditTaskImpl);
            persistenceContext.merge(auditTaskImpl);
        } finally {
            cleanup(persistenceContext);
        }
    }

    @Override
    public void afterTaskAddedEvent(TaskEvent event) {
        Task ti = event.getTask();
        TaskPersistenceContext persistenceContext = getPersistenceContext(((TaskContext)event.getTaskContext()).getPersistenceContext());
        try {
            AuditTaskImpl auditTaskImpl = new AuditTaskImpl(
                ti.getId(),
                ti.getName(),
                ti.getTaskData().getStatus().name(),
                ti.getTaskData().getActivationTime(),
                (ti.getTaskData().getActualOwner() != null) ? ti.getTaskData().getActualOwner().getId() : "",
                ti.getDescription(),
                ti.getPriority(),
                (ti.getTaskData().getCreatedBy() != null) ? ti.getTaskData().getCreatedBy().getId() : "",
                ti.getTaskData().getCreatedOn(),
                ti.getTaskData().getExpirationTime(),
                ti.getTaskData().getProcessInstanceId(),
                ti.getTaskData().getProcessId(),
                ti.getTaskData().getProcessSessionId(),
                ti.getTaskData().getDeploymentId(),
                ti.getTaskData().getParentId(),
                ti.getTaskData().getWorkItemId(),
                event.getEventDate()
            );
            event.getMetadata().put(METADATA_AUDIT_TASK, auditTaskImpl);
            persistenceContext.persist(auditTaskImpl);

            TaskEventImpl taskEventImpl = new TaskEventImpl(event, TaskEventType.ADDED);
            event.getMetadata().put(METADATA_TASK_EVENT, taskEventImpl);
            persistenceContext.persist(taskEventImpl);
            createTaskOperationView(event, TaskEventType.ADDED);

        } finally {
            cleanup(persistenceContext);
        }
    }

    @Override
    public void afterTaskExitedEvent(TaskEvent event) {
        Task ti = event.getTask();
        TaskPersistenceContext persistenceContext = getPersistenceContext(((TaskContext)event.getTaskContext()).getPersistenceContext());
        try {            
            TaskEventImpl taskEventImpl = new TaskEventImpl(event, TaskEventType.EXITED);
            event.getMetadata().put(METADATA_TASK_EVENT, taskEventImpl);
            persistenceContext.persist(taskEventImpl);
            createTaskOperationView (event, TaskEventType.EXITED);

            AuditTaskImpl auditTaskImpl = getAuditTask(event, persistenceContext, ti);
            if (auditTaskImpl == null) {
                logger.warn("Unable find audit task entry for task id {} '{}', skipping audit task update", ti.getId(), ti.getName());
                return;
            }
            
            auditTaskImpl.setDescription(ti.getDescription());
            auditTaskImpl.setName(ti.getName());  
            auditTaskImpl.setActivationTime(ti.getTaskData().getActivationTime());
            auditTaskImpl.setPriority(ti.getPriority());
            auditTaskImpl.setDueDate(ti.getTaskData().getExpirationTime());
            auditTaskImpl.setStatus(ti.getTaskData().getStatus().name());
            auditTaskImpl.setActualOwner(getActualOwner(ti));
            auditTaskImpl.setLastModificationDate(event.getEventDate());
            event.getMetadata().put(METADATA_AUDIT_TASK, auditTaskImpl);
            persistenceContext.merge(auditTaskImpl);
            
        } finally {
            cleanup(persistenceContext);
        }
        
    }

    @Override
    public void afterTaskReleasedEvent(TaskEvent event) {
        Task ti = event.getTask();
        TaskPersistenceContext persistenceContext = getPersistenceContext(((TaskContext)event.getTaskContext()).getPersistenceContext());
        try {
             
            AuditTaskImpl auditTaskImpl = getAuditTask(event, persistenceContext, ti);
            if (auditTaskImpl == null) {
                logger.warn("Unable find audit task entry for task id {} '{}', skipping audit task update", ti.getId(), ti.getName());
                return;
            }
   
            auditTaskImpl.setDescription(ti.getDescription());
            auditTaskImpl.setName(ti.getName());  
            auditTaskImpl.setActivationTime(ti.getTaskData().getActivationTime());
            auditTaskImpl.setPriority(ti.getPriority());
            auditTaskImpl.setDueDate(ti.getTaskData().getExpirationTime());
            auditTaskImpl.setStatus(ti.getTaskData().getStatus().name());
            auditTaskImpl.setActualOwner(getActualOwner(ti));
            auditTaskImpl.setLastModificationDate(event.getEventDate());
            event.getMetadata().put(METADATA_AUDIT_TASK, auditTaskImpl);
            persistenceContext.merge(auditTaskImpl);
        } finally {
            cleanup(persistenceContext);
        }

    }

    @Override
    public void afterTaskResumedEvent(TaskEvent event) {
        Task ti = event.getTask();
        TaskPersistenceContext persistenceContext = getPersistenceContext(((TaskContext)event.getTaskContext()).getPersistenceContext());
        try {            
            TaskEventImpl taskEventImpl = new TaskEventImpl(event, TaskEventType.RESUMED);
            event.getMetadata().put(METADATA_TASK_EVENT, taskEventImpl);
            persistenceContext.persist(taskEventImpl);
            createTaskOperationView (event, TaskEventType.RESUMED);
            AuditTaskImpl auditTaskImpl = getAuditTask(event, persistenceContext, ti);
            if (auditTaskImpl == null) {
                logger.warn("Unable find audit task entry for task id {} '{}', skipping audit task update", ti.getId(), ti.getName());
                return;
            }
            auditTaskImpl.setDescription(ti.getDescription());
            auditTaskImpl.setName(ti.getName());  
            auditTaskImpl.setActivationTime(ti.getTaskData().getActivationTime());
            auditTaskImpl.setPriority(ti.getPriority());
            auditTaskImpl.setDueDate(ti.getTaskData().getExpirationTime());
            auditTaskImpl.setStatus(ti.getTaskData().getStatus().name());
            auditTaskImpl.setActualOwner(getActualOwner(ti));
            auditTaskImpl.setLastModificationDate(event.getEventDate());
            event.getMetadata().put(METADATA_AUDIT_TASK, auditTaskImpl);
            persistenceContext.merge(auditTaskImpl);
            
        } finally {
            cleanup(persistenceContext);
        }
    }

    @Override
    public void afterTaskSuspendedEvent(TaskEvent event) {
        Task ti = event.getTask();
        TaskPersistenceContext persistenceContext = getPersistenceContext(((TaskContext)event.getTaskContext()).getPersistenceContext());
        try {
            TaskEventImpl taskEventImpl = new TaskEventImpl(event, TaskEventType.SUSPENDED);
            event.getMetadata().put(METADATA_TASK_EVENT, taskEventImpl);
            persistenceContext.persist(taskEventImpl);
            createTaskOperationView (event, TaskEventType.SUSPENDED);
            AuditTaskImpl auditTaskImpl = getAuditTask(event, persistenceContext, ti);
            if (auditTaskImpl == null) {
                logger.warn("Unable find audit task entry for task id {} '{}', skipping audit task update", ti.getId(), ti.getName());
                return;
            }
              auditTaskImpl.setDescription(ti.getDescription());
            auditTaskImpl.setName(ti.getName());  
            auditTaskImpl.setActivationTime(ti.getTaskData().getActivationTime());
            auditTaskImpl.setPriority(ti.getPriority());
            auditTaskImpl.setDueDate(ti.getTaskData().getExpirationTime());
            auditTaskImpl.setStatus(ti.getTaskData().getStatus().name());
            auditTaskImpl.setActualOwner(getActualOwner(ti));
            auditTaskImpl.setLastModificationDate(event.getEventDate());
            event.getMetadata().put(METADATA_AUDIT_TASK, auditTaskImpl);
            persistenceContext.merge(auditTaskImpl);
            
        } finally {
            cleanup(persistenceContext);
        }
    }

    @Override
    public void afterTaskForwardedEvent(TaskEvent event) {
        Task ti = event.getTask();
        TaskPersistenceContext persistenceContext = getPersistenceContext(((TaskContext)event.getTaskContext()).getPersistenceContext());
        try {
            StringBuilder message = new StringBuilder();
            String entitiesAsString = (ti.getPeopleAssignments().getPotentialOwners()).stream().map(oe -> oe.getId()).collect(Collectors.joining(","));
            message.append("Forward to [" + entitiesAsString + "]");
            
            TaskEventImpl taskEventImpl = new TaskEventImpl(event, TaskEventType.FORWARDED, message.toString());

            event.getMetadata().put(METADATA_TASK_EVENT, taskEventImpl);
            persistenceContext.persist(taskEventImpl);
            createTaskOperationView (event, TaskEventType.FORWARDED);
            AuditTaskImpl auditTaskImpl = getAuditTask(event, persistenceContext, ti);
            if (auditTaskImpl == null) {
                logger.warn("Unable find audit task entry for task id {} '{}', skipping audit task update", ti.getId(), ti.getName());
                return;
            }
            auditTaskImpl.setDescription(ti.getDescription());
            auditTaskImpl.setName(ti.getName());  
            auditTaskImpl.setActivationTime(ti.getTaskData().getActivationTime());
            auditTaskImpl.setPriority(ti.getPriority());
            auditTaskImpl.setDueDate(ti.getTaskData().getExpirationTime());
            auditTaskImpl.setStatus(ti.getTaskData().getStatus().name());
            auditTaskImpl.setActualOwner(getActualOwner(ti));
            auditTaskImpl.setLastModificationDate(event.getEventDate());
            event.getMetadata().put(METADATA_AUDIT_TASK, auditTaskImpl);
            persistenceContext.merge(auditTaskImpl);
            
        } finally {
            cleanup(persistenceContext);
        }
    }

    @Override
    public void afterTaskDelegatedEvent(TaskEvent event) {
        Task ti = event.getTask();
        TaskPersistenceContext persistenceContext = getPersistenceContext(((TaskContext)event.getTaskContext()).getPersistenceContext());
        try {           
            TaskEventImpl taskEventImpl = new TaskEventImpl(event, TaskEventType.DELEGATED);
            event.getMetadata().put(METADATA_TASK_EVENT, taskEventImpl);
            persistenceContext.persist(taskEventImpl);
            createTaskOperationView (event, TaskEventType.DELEGATED);
            AuditTaskImpl auditTaskImpl = getAuditTask(event, persistenceContext, ti);
            if (auditTaskImpl == null) {
                logger.warn("Unable find audit task entry for task id {} '{}', skipping audit task update", ti.getId(), ti.getName());
                return;
            }
            auditTaskImpl.setDescription(ti.getDescription());
            auditTaskImpl.setName(ti.getName());  
            auditTaskImpl.setActivationTime(ti.getTaskData().getActivationTime());
            auditTaskImpl.setPriority(ti.getPriority());
            auditTaskImpl.setDueDate(ti.getTaskData().getExpirationTime());
            auditTaskImpl.setStatus(ti.getTaskData().getStatus().name());
            auditTaskImpl.setActualOwner(getActualOwner(ti));
            auditTaskImpl.setLastModificationDate(event.getEventDate());
            event.getMetadata().put(METADATA_AUDIT_TASK, auditTaskImpl);
            persistenceContext.merge(auditTaskImpl);
            
        } finally {
            cleanup(persistenceContext);
        }
    }
    
    @Override
    public void afterTaskNominatedEvent(TaskEvent event) {
        Task ti = event.getTask();
        TaskPersistenceContext persistenceContext = getPersistenceContext(((TaskContext)event.getTaskContext()).getPersistenceContext());
        try {
            TaskEventImpl taskEventImpl = new TaskEventImpl(event, TaskEventType.NOMINATED);
            event.getMetadata().put(METADATA_TASK_EVENT, taskEventImpl);
            persistenceContext.persist(taskEventImpl);
            createTaskOperationView (event, TaskEventType.NOMINATED);
            AuditTaskImpl auditTaskImpl = getAuditTask(event, persistenceContext, ti);
            if (auditTaskImpl == null) {
                logger.warn("Unable find audit task entry for task id {} '{}', skipping audit task update", ti.getId(), ti.getName());
                return;
            }
            auditTaskImpl.setDescription(ti.getDescription());
            auditTaskImpl.setName(ti.getName());  
            auditTaskImpl.setActivationTime(ti.getTaskData().getActivationTime());
            auditTaskImpl.setPriority(ti.getPriority());
            auditTaskImpl.setDueDate(ti.getTaskData().getExpirationTime());
            auditTaskImpl.setStatus(ti.getTaskData().getStatus().name());
            auditTaskImpl.setActualOwner(getActualOwner(ti));
            auditTaskImpl.setLastModificationDate(event.getEventDate());
            event.getMetadata().put(METADATA_AUDIT_TASK, auditTaskImpl);
            persistenceContext.merge(auditTaskImpl);
        } finally {
            cleanup(persistenceContext);
        }
    }
    
    /*
     * helper methods - start
     */
    
    protected AuditTaskImpl getAuditTask(TaskEvent event, TaskPersistenceContext persistenceContext, Task ti) {
        AuditTaskImpl auditTaskImpl = persistenceContext.queryWithParametersInTransaction("getAuditTaskById", true, 
                persistenceContext.addParametersToMap("taskId", ti.getId()),
                ClassUtil.<AuditTaskImpl>castClass(AuditTaskImpl.class));
        
        return auditTaskImpl;
    }

    /*
     * helper methods - end
     */
    
    @Override
    public void beforeTaskActivatedEvent(TaskEvent event) {

    }

    @Override
    public void beforeTaskClaimedEvent(TaskEvent event) {

    }

    @Override
    public void beforeTaskSkippedEvent(TaskEvent event) {

    }

    @Override
    public void beforeTaskStartedEvent(TaskEvent event) {

    }

    @Override
    public void beforeTaskStoppedEvent(TaskEvent event) {

    }

    @Override
    public void beforeTaskCompletedEvent(TaskEvent event) {

    }

    @Override
    public void beforeTaskFailedEvent(TaskEvent event) {

    }

    @Override
    public void beforeTaskAddedEvent(TaskEvent event) {
        
    }

    @Override
    public void beforeTaskExitedEvent(TaskEvent event) {

    }

    @Override
    public void beforeTaskReleasedEvent(TaskEvent event) {
        TaskPersistenceContext persistenceContext = getPersistenceContext(((TaskContext)event.getTaskContext()).getPersistenceContext());
        try {
            TaskEventImpl taskEventImpl = new TaskEventImpl(event, TaskEventType.RELEASED);
            event.getMetadata().put(METADATA_TASK_EVENT, taskEventImpl);
            persistenceContext.persist(taskEventImpl);
            createTaskOperationView (event, TaskEventType.RELEASED);

        } finally {
            cleanup(persistenceContext);
        }
    }

    @Override
    public void beforeTaskResumedEvent(TaskEvent event) {

    }

    @Override
    public void beforeTaskSuspendedEvent(TaskEvent event) {

    }

    @Override
    public void beforeTaskForwardedEvent(TaskEvent event) {

    }

    @Override
    public void beforeTaskDelegatedEvent(TaskEvent event) {

    }
    
    @Override
    public void beforeTaskNominatedEvent(TaskEvent event) {

    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj ) 
            return true;
        if ( obj == null ) 
            return false;
        if ( (obj instanceof JPATaskLifeCycleEventListener) ) 
            return true;
        
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.getClass().getName().hashCode();
        
        return result;
    }

    @Override
    public void beforeTaskUpdatedEvent(TaskEvent event) {
        
        
    }

    public String getUpdateFieldLog(String fieldName, String previousValue, String value) {
        return "Updated " + fieldName 
                + " {From: '"+ (previousValue != null ? previousValue : "") 
                + "' to: '"+ (value != null ? value : "") + "'}";
    }

    @Override
    public void afterTaskUpdatedEvent(TaskEvent event) {
        Task ti = event.getTask();
        TaskPersistenceContext persistenceContext = getPersistenceContext(((TaskContext)event.getTaskContext()).getPersistenceContext());
        try {
            
            AuditTaskImpl auditTaskImpl = getAuditTask(event, persistenceContext, ti);
            if (auditTaskImpl == null) {
                logger.warn("Unable find audit task entry for task id {} '{}', skipping audit task update", ti.getId(), ti.getName());
                return;
            }

            if ((ti.getDescription() != null && !ti.getDescription().equals(auditTaskImpl.getDescription()))
                    || (ti.getDescription() == null && auditTaskImpl.getDescription() != null)) {
                String message = getUpdateFieldLog("Description", auditTaskImpl.getDescription(), ti.getDescription());

                if (message != null && message.length() > TASK_DESCRIPTION_LENGTH) {
                    message = message.substring(0, (TASK_DESCRIPTION_LENGTH - 2)) + "'}";
                    logger.warn("TaskEvent message content was trimmed as it was too long(more than {} characters)", TASK_DESCRIPTION_LENGTH);
                }
                
                TaskEventImpl taskEventImpl = new TaskEventImpl(event,
                                                                TaskEventType.UPDATED,
                                                                message);
                event.getMetadata().put(METADATA_TASK_EVENT, taskEventImpl);
                persistenceContext.persist(taskEventImpl);
                createTaskOperationView (event, TaskEventType.UPDATED);

            }
            if( (ti.getName() != null && !ti.getName().equals(auditTaskImpl.getName()))
                    || (ti.getName() == null && auditTaskImpl.getName() != null)){
                String message = getUpdateFieldLog("Name", auditTaskImpl.getName(), ti.getName());
                TaskEventImpl taskEventImpl = new TaskEventImpl(event,
                                                                TaskEventType.UPDATED,
                                                                message);
                event.getMetadata().put(METADATA_TASK_EVENT, taskEventImpl);
                persistenceContext.persist(taskEventImpl);
                createTaskOperationView (event, TaskEventType.UPDATED);
            }
            if( auditTaskImpl.getPriority() != ti.getPriority()){
                String message = getUpdateFieldLog("Priority", String.valueOf(auditTaskImpl.getPriority()), String.valueOf(ti.getPriority()));
                TaskEventImpl taskEventImpl = new TaskEventImpl(event,
                                                                TaskEventType.UPDATED,
                                                                message);
                event.getMetadata().put(METADATA_TASK_EVENT, taskEventImpl);
                persistenceContext.persist(taskEventImpl);
                createTaskOperationView (event, TaskEventType.UPDATED);
            }

            if((auditTaskImpl.getDueDate() != null && ti.getTaskData().getExpirationTime() != null 
                    && auditTaskImpl.getDueDate().getTime() != ti.getTaskData().getExpirationTime().getTime()) 
                    || (auditTaskImpl.getDueDate() == null && ti.getTaskData().getExpirationTime() != null)
                    || (auditTaskImpl.getDueDate() != null && ti.getTaskData().getExpirationTime() == null)){
                String fromDate = (auditTaskImpl.getDueDate() != null ? new Date(auditTaskImpl.getDueDate().getTime()).toString(): null);
                String toDate = (ti.getTaskData().getExpirationTime()!= null ? ti.getTaskData().getExpirationTime().toString() : "" );
                String message = getUpdateFieldLog( "DueDate",
                                                    fromDate,
                                                    toDate );
                TaskEventImpl taskEventImpl = new TaskEventImpl(event,
                                                                TaskEventType.UPDATED,
                                                                message);
                event.getMetadata().put(METADATA_TASK_EVENT, taskEventImpl);
                persistenceContext.persist(taskEventImpl);
                createTaskOperationView (event, TaskEventType.UPDATED);
            }
    
            auditTaskImpl.setDescription(ti.getDescription());
            auditTaskImpl.setName(ti.getName());
            auditTaskImpl.setPriority(ti.getPriority());
            auditTaskImpl.setDueDate(ti.getTaskData().getExpirationTime());
            auditTaskImpl.setLastModificationDate(event.getEventDate());
            event.getMetadata().put(METADATA_AUDIT_TASK, auditTaskImpl);
            persistenceContext.merge(auditTaskImpl);
            
            
        } catch(Exception e){
            e.printStackTrace();

        }

        finally {
            cleanup(persistenceContext);
        }
    }

    @Override
    public void beforeTaskReassignedEvent(TaskEvent event) {

    }

    @Override
    public void afterTaskReassignedEvent(TaskEvent event) {
        Task ti = event.getTask();
        TaskPersistenceContext persistenceContext = getPersistenceContext(((TaskContext)event.getTaskContext()).getPersistenceContext());
        try {
            TaskEventImpl taskEventImpl = new TaskEventImpl(event, TaskEventType.DELEGATED);
            event.getMetadata().put(METADATA_TASK_EVENT, taskEventImpl);
            persistenceContext.persist(taskEventImpl);
            createTaskOperationView (event, TaskEventType.DELEGATED);

            AuditTaskImpl auditTaskImpl = getAuditTask(event, persistenceContext, ti);
            if (auditTaskImpl == null) {
                logger.warn("Unable find audit task entry for task id {} '{}', skipping audit task update", ti.getId(), ti.getName());
                return;
            }
            auditTaskImpl.setDescription(ti.getDescription());
            auditTaskImpl.setName(ti.getName());  
            auditTaskImpl.setActivationTime(ti.getTaskData().getActivationTime());
            auditTaskImpl.setPriority(ti.getPriority());
            auditTaskImpl.setDueDate(ti.getTaskData().getExpirationTime());
            auditTaskImpl.setStatus(ti.getTaskData().getStatus().name());
            auditTaskImpl.setActualOwner(getActualOwner(ti));
            auditTaskImpl.setLastModificationDate(event.getEventDate());
            event.getMetadata().put(METADATA_AUDIT_TASK, auditTaskImpl);
            persistenceContext.merge(auditTaskImpl);
            
        } finally {
            cleanup(persistenceContext);
        }
    }

    @Override
    public void beforeTaskNotificationEvent(TaskEvent event) {

    }

    @Override
    public void afterTaskNotificationEvent(TaskEvent event) {

    }
    
    @Override
    public void afterTaskOutputVariableChangedEvent(TaskEvent event, Map<String, Object> variables) {
        Task task = event.getTask();        
        TaskPersistenceContext persistenceContext = getPersistenceContext(((TaskContext)event.getTaskContext()).getPersistenceContext());
        // first cleanup previous values if any
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("type", VariableType.OUTPUT);
        params.put("taskId", task.getId());
        int removed = persistenceContext.executeUpdate("DeleteTaskVariableForTask", params);
        logger.debug("Deleted {} output variables logs for task id {}", removed, task.getId());
        
        if (variables == null || variables.isEmpty()) {
            return;
        }
        
        indexAndPersistVariables(event, task, variables, persistenceContext, VariableType.OUTPUT);
        String message = "Task output data updated";
        TaskEventImpl taskEventImpl = new TaskEventImpl(event,
                                                        TaskEventType.UPDATED,
                                                        message);
        event.getMetadata().put(METADATA_TASK_EVENT, taskEventImpl);
        persistenceContext.persist(taskEventImpl);
        createTaskOperationView (event, TaskEventType.UPDATED);
        AuditTaskImpl auditTaskImpl = getAuditTask(event, persistenceContext, task);
        if (auditTaskImpl == null) {
            logger.warn("Unable find audit task entry for task id {} '{}', skipping audit task update", task.getId(), task.getName());
            return;
        }
        auditTaskImpl.setLastModificationDate(event.getEventDate());
        event.getMetadata().put(METADATA_AUDIT_TASK, auditTaskImpl);
        persistenceContext.merge(auditTaskImpl);
        
    }

    @Override
    public void afterTaskInputVariableChangedEvent(TaskEvent event, Map<String, Object> variables) {
        if (variables == null || variables.isEmpty()) {
            return;
        }
        Task task = event.getTask();        
        TaskPersistenceContext persistenceContext = getPersistenceContext(((TaskContext)event.getTaskContext()).getPersistenceContext());

        indexAndPersistVariables(event, task, variables, persistenceContext, VariableType.INPUT);
        
    }
    
    protected void indexAndPersistVariables(TaskEvent event, Task task, Map<String, Object> variables, TaskPersistenceContext persistenceContext, VariableType type) {
        TaskIndexerManager manager = TaskIndexerManager.get();
        
        for (Map.Entry<String, Object> variable : variables.entrySet()) {
            if (TaskLifeCycleEventConstants.SKIPPED_TASK_VARIABLES.contains(variable.getKey()) || variable.getValue() == null) {
                continue;
            }
            List<TaskVariable> taskVars = manager.index(task, variable.getKey(), variable.getValue());
            event.getMetadata().put(METADATA_VAR_EVENT, taskVars);
            if (taskVars != null) {
                for (TaskVariable tVariable : taskVars) {
                    tVariable.setType(type);
                    persistenceContext.persist(tVariable);
                }
            }
        }
    }
    
    @Override
    public void afterTaskAssignmentsAddedEvent(TaskEvent event, AssignmentType type, List<OrganizationalEntity> entities) {                
        assignmentsUpadted(event, type, entities, "] have been added");    
    }

    @Override
    public void afterTaskAssignmentsRemovedEvent(TaskEvent event, AssignmentType type, List<OrganizationalEntity> entities) {
        assignmentsUpadted(event, type, entities, "] have been removed");
    }
    
    protected void assignmentsUpadted(TaskEvent event, AssignmentType type, List<OrganizationalEntity> entities, String messageSufix) {
        if (entities == null || entities.isEmpty()) {
            return;
        }
        TaskPersistenceContext persistenceContext = getPersistenceContext(((TaskContext)event.getTaskContext()).getPersistenceContext());
        StringBuilder message = new StringBuilder();
        
        switch (type) {
            case POT_OWNER:
                message.append("Potential owners [");
                break;
            case EXCL_OWNER:
                message.append("Excluded owners [");
                break;
            case ADMIN:
                message.append("Business administrators [");
                break;
            default:
                break;
        }
        message.append(entities.stream().map(oe -> oe.getId()).collect(Collectors.joining(",")));
        message.append(messageSufix);
        
        TaskEventImpl taskEventImpl = new TaskEventImpl(event,
                                                        TaskEventType.UPDATED, 
                                                        message.toString());
        event.getMetadata().put(METADATA_TASK_EVENT, taskEventImpl);
        persistenceContext.persist(taskEventImpl);
        createTaskOperationView (event, TaskEventType.UPDATED, type);
    }

    
    protected String getActualOwner(Task ti) {
        String userId = "";
        if (ti.getTaskData().getActualOwner() != null) {
            userId = ti.getTaskData().getActualOwner().getId();
        }
        
        return userId;
    }
    
    private void createTaskOperationView (TaskEvent event, TaskEventType type) {
        createTaskOperationView (event, type, null);
    }
    
    private void createTaskOperationView (TaskEvent event, TaskEventType type, AssignmentType assignType) {
        PersistenceEventManager eventManager = EventManagerProvider.getInstance().get();
        if (eventManager.isActive()) {
            eventManager.update(new TaskOperationView(event, type, assignType));
        }
    }

}
