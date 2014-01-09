/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jbpm.services.task.lifecycle.listeners;

import org.jbpm.services.task.audit.impl.model.TaskEventImpl;
import java.util.Date;
import org.jbpm.services.task.audit.impl.model.UserAuditTask;
import org.jbpm.services.task.audit.impl.model.UserAuditTaskImpl;

import org.kie.api.task.model.Task;
import org.kie.internal.task.api.TaskContext;
import org.kie.internal.task.api.TaskPersistenceContext;

/**
 *
 */
public class JPATaskLifeCycleEventListener implements TaskLifeCycleEventListener{

   
    public JPATaskLifeCycleEventListener() {
    }
    
    @Override
    public void afterTaskStartedEvent(TaskEvent event) {
        String userId = "";
        Task ti = event.getTask();
        TaskPersistenceContext persistenceContext = ((TaskContext)event.getTaskContext()).getPersistenceContext();
        if(ti.getTaskData().getActualOwner() != null){
            userId = ti.getTaskData().getActualOwner().getId();
        }
        persistenceContext.persist(new TaskEventImpl(ti.getId(), org.kie.internal.task.api.model.TaskEvent.TaskEventType.STARTED, userId, new Date()));
        UserAuditTask task = persistenceContext.find(UserAuditTaskImpl.class, ti.getId());
        task.setStatus(ti.getTaskData().getStatus().name());
        persistenceContext.persist(task);
        
    }

    @Override
    public void afterTaskActivatedEvent(TaskEvent event) {
        String userId = "";
        Task ti = event.getTask();
        TaskPersistenceContext persistenceContext = ((TaskContext)event.getTaskContext()).getPersistenceContext();
        if(ti.getTaskData().getActualOwner() != null){
            userId = ti.getTaskData().getActualOwner().getId();
        }
        persistenceContext.persist(new TaskEventImpl(ti.getId(), org.kie.internal.task.api.model.TaskEvent.TaskEventType.ACTIVATED, userId, new Date()));
        UserAuditTask task = persistenceContext.find(UserAuditTaskImpl.class, ti.getId());
        task.setStatus(ti.getTaskData().getStatus().name());
        persistenceContext.persist(task);
    }

    @Override
    public void afterTaskClaimedEvent(TaskEvent event) {
        String userId = "";
        Task ti = event.getTask();
        TaskPersistenceContext persistenceContext = ((TaskContext)event.getTaskContext()).getPersistenceContext();
        if(ti.getTaskData().getActualOwner() != null){
            userId = ti.getTaskData().getActualOwner().getId();
        }
        persistenceContext.persist(new TaskEventImpl(ti.getId(), org.kie.internal.task.api.model.TaskEvent.TaskEventType.CLAIMED, userId, new Date()));
        UserAuditTask task = persistenceContext.find(UserAuditTaskImpl.class, ti.getId());
        task.setStatus(ti.getTaskData().getStatus().name());
        persistenceContext.persist(task);
    }

    @Override
    public void afterTaskSkippedEvent(TaskEvent event) {
        String userId = "";
        Task ti = event.getTask();
        TaskPersistenceContext persistenceContext = ((TaskContext)event.getTaskContext()).getPersistenceContext();
        if(ti.getTaskData().getActualOwner() != null){
            userId = ti.getTaskData().getActualOwner().getId();
        }
        persistenceContext.persist(new TaskEventImpl(ti.getId(), org.kie.internal.task.api.model.TaskEvent.TaskEventType.SKIPPED, userId, new Date()));
        UserAuditTask task = persistenceContext.find(UserAuditTaskImpl.class, ti.getId());
        task.setStatus(ti.getTaskData().getStatus().name());
        persistenceContext.persist(task);
    }

    @Override
    public void afterTaskStoppedEvent(TaskEvent event ) {
        String userId = "";
        Task ti = event.getTask();
        TaskPersistenceContext persistenceContext = ((TaskContext)event.getTaskContext()).getPersistenceContext();
        if(ti.getTaskData().getActualOwner() != null){
            userId = ti.getTaskData().getActualOwner().getId();
        }
        persistenceContext.persist(new TaskEventImpl(ti.getId(), org.kie.internal.task.api.model.TaskEvent.TaskEventType.STOPPED, userId, new Date()));
        UserAuditTask task = persistenceContext.find(UserAuditTaskImpl.class, ti.getId());
        task.setStatus(ti.getTaskData().getStatus().name());
        persistenceContext.persist(task);
    }

    @Override
    public void afterTaskCompletedEvent(TaskEvent event) {
        String userId = "";
        Task ti = event.getTask();
        TaskPersistenceContext persistenceContext = ((TaskContext)event.getTaskContext()).getPersistenceContext();
        if(ti.getTaskData().getActualOwner() != null){
            userId = ti.getTaskData().getActualOwner().getId();
        }
        persistenceContext.persist(new TaskEventImpl(ti.getId(), org.kie.internal.task.api.model.TaskEvent.TaskEventType.COMPLETED, userId, new Date()));
        UserAuditTask task = persistenceContext.find(UserAuditTaskImpl.class, ti.getId());
        task.setStatus(ti.getTaskData().getStatus().name());
        persistenceContext.persist(task);
    }

    @Override
    public void afterTaskFailedEvent(TaskEvent event) {
        String userId = "";
        Task ti = event.getTask();
        TaskPersistenceContext persistenceContext = ((TaskContext)event.getTaskContext()).getPersistenceContext();
        if(ti.getTaskData().getActualOwner() != null){
            userId = ti.getTaskData().getActualOwner().getId();
        }
        persistenceContext.persist(new TaskEventImpl(ti.getId(), org.kie.internal.task.api.model.TaskEvent.TaskEventType.FAILED, userId, new Date()));
        UserAuditTask task = persistenceContext.find(UserAuditTaskImpl.class, ti.getId());
        task.setStatus(ti.getTaskData().getStatus().name());
        persistenceContext.persist(task);
    }

    @Override
    public void afterTaskAddedEvent(TaskEvent event) {
        String userId = "";
        Task ti = event.getTask();
        TaskPersistenceContext persistenceContext = ((TaskContext)event.getTaskContext()).getPersistenceContext();
        if(ti.getTaskData().getActualOwner() != null){
            userId = ti.getTaskData().getActualOwner().getId();
            persistenceContext.persist(new UserAuditTaskImpl(userId, ti.getId(), ti.getTaskData().getStatus().name(), 
                                                        ti.getTaskData().getActivationTime(), ti.getNames().get(0).getText(), 
                                                        (!ti.getDescriptions().isEmpty())?ti.getDescriptions().get(0).getText():"", ti.getPriority(), 
                                                        (ti.getTaskData().getCreatedBy() == null)?"":ti.getTaskData().getCreatedBy().getId(),
                                                        ti.getTaskData().getCreatedOn(), ti.getTaskData().getExpirationTime(),
                                                        ti.getTaskData().getProcessInstanceId(), ti.getTaskData().getProcessId(), ti.getTaskData().getProcessSessionId(),
                                                        ti.getTaskData().getParentId()));
        }
        persistenceContext.persist(new TaskEventImpl(ti.getId(), org.kie.internal.task.api.model.TaskEvent.TaskEventType.ADDED, userId , new Date()));
    }

    @Override
    public void afterTaskExitedEvent(TaskEvent event) {
        String userId = "";
        Task ti = event.getTask();
        TaskPersistenceContext persistenceContext = ((TaskContext)event.getTaskContext()).getPersistenceContext();
        if(ti.getTaskData().getActualOwner() != null){
            userId = ti.getTaskData().getActualOwner().getId();
        }
        persistenceContext.persist(new TaskEventImpl(ti.getId(), org.kie.internal.task.api.model.TaskEvent.TaskEventType.EXITED, userId, new Date()));
        UserAuditTask task = persistenceContext.find(UserAuditTaskImpl.class, ti.getId());
        task.setStatus(ti.getTaskData().getStatus().name());
        persistenceContext.persist(task);
    }

    @Override
    public void afterTaskReleasedEvent(TaskEvent event) {
        String userId = "";
        Task ti = event.getTask();
        TaskPersistenceContext persistenceContext = ((TaskContext)event.getTaskContext()).getPersistenceContext();
        if(ti.getTaskData().getActualOwner() != null){
            userId = ti.getTaskData().getActualOwner().getId();
        }
        persistenceContext.persist(new TaskEventImpl(ti.getId(), org.kie.internal.task.api.model.TaskEvent.TaskEventType.RELEASED, userId, new Date()));
        UserAuditTask task = persistenceContext.find(UserAuditTaskImpl.class, ti.getId());
        persistenceContext.remove(task);
        
    }

    @Override
    public void afterTaskResumedEvent(TaskEvent event) {
        String userId = "";
        Task ti = event.getTask();
        TaskPersistenceContext persistenceContext = ((TaskContext)event.getTaskContext()).getPersistenceContext();
        if(ti.getTaskData().getActualOwner() != null){
            userId = ti.getTaskData().getActualOwner().getId();
        }
        persistenceContext.persist(new TaskEventImpl(ti.getId(), org.kie.internal.task.api.model.TaskEvent.TaskEventType.RESUMED, userId, new Date()));
        UserAuditTask task = persistenceContext.find(UserAuditTaskImpl.class, ti.getId());
        task.setStatus(ti.getTaskData().getStatus().name());
        persistenceContext.persist(task);
    }

    @Override
    public void afterTaskSuspendedEvent(TaskEvent event) {
        String userId = "";
        Task ti = event.getTask();
        TaskPersistenceContext persistenceContext = ((TaskContext)event.getTaskContext()).getPersistenceContext();
        if(ti.getTaskData().getActualOwner() != null){
            userId = ti.getTaskData().getActualOwner().getId();
        }
        persistenceContext.persist(new TaskEventImpl(ti.getId(), org.kie.internal.task.api.model.TaskEvent.TaskEventType.SUSPENDED, userId, new Date()));
        UserAuditTask task = persistenceContext.find(UserAuditTaskImpl.class, ti.getId());
        task.setStatus(ti.getTaskData().getStatus().name());
        persistenceContext.persist(task);
    }

    @Override
    public void afterTaskForwardedEvent(TaskEvent event) {
        String userId = "";
        Task ti = event.getTask();
        TaskPersistenceContext persistenceContext = ((TaskContext)event.getTaskContext()).getPersistenceContext();
        if(ti.getTaskData().getActualOwner() != null){
            userId = ti.getTaskData().getActualOwner().getId();
        }
        persistenceContext.persist(new TaskEventImpl(ti.getId(), org.kie.internal.task.api.model.TaskEvent.TaskEventType.FORWARDED, userId, new Date()));
        UserAuditTask task = persistenceContext.find(UserAuditTaskImpl.class, ti.getId());
        task.setStatus(ti.getTaskData().getStatus().name());
        persistenceContext.persist(task);
    }

    @Override
    public void afterTaskDelegatedEvent(TaskEvent event) {
        String userId = "";
        Task ti = event.getTask();
        TaskPersistenceContext persistenceContext = ((TaskContext)event.getTaskContext()).getPersistenceContext();
        if(ti.getTaskData().getActualOwner() != null){
            userId = ti.getTaskData().getActualOwner().getId();
        }
        persistenceContext.persist(new TaskEventImpl(ti.getId(), org.kie.internal.task.api.model.TaskEvent.TaskEventType.DELEGATED, userId, new Date()));
        UserAuditTask task = persistenceContext.find(UserAuditTaskImpl.class, ti.getId());
        task.setStatus(ti.getTaskData().getStatus().name());
        persistenceContext.persist(task);
    }

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
    
}
