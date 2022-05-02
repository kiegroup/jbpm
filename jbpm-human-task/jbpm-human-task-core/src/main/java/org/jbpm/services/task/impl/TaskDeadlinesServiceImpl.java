/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.services.task.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.time.Job;
import org.drools.core.time.JobContext;
import org.drools.core.time.JobHandle;
import org.drools.core.time.TimerService;
import org.drools.core.time.Trigger;
import org.drools.core.time.impl.IntervalTrigger;
import org.jbpm.process.core.timer.NamedJobContext;
import org.jbpm.process.core.timer.TimerServiceRegistry;
import org.jbpm.process.core.timer.impl.GlobalTimerService;
import org.jbpm.services.task.TaskServiceRegistry;
import org.jbpm.services.task.commands.ExecuteDeadlinesCommand;
import org.jbpm.services.task.deadlines.NotificationListener;
import org.jbpm.services.task.utils.ClassUtil;
import org.kie.api.task.model.Task;
import org.kie.internal.task.api.TaskDeadlinesService;
import org.kie.internal.task.api.TaskPersistenceContext;
import org.kie.internal.task.api.model.Deadline;
import org.kie.internal.task.api.model.DeadlineSummary;
import org.kie.internal.task.api.model.Deadlines;
import org.kie.internal.task.api.model.InternalTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskDeadlinesServiceImpl implements TaskDeadlinesService {
    
    private static final Logger logger = LoggerFactory.getLogger(TaskDeadlinesServiceImpl.class);
    protected static NotificationListener notificationListener;

    private static final Map<String, JobHandle> jobHandles = new ConcurrentHashMap<String, JobHandle>();

    private TaskPersistenceContext persistenceContext;

    public TaskDeadlinesServiceImpl() {
    }
    
    public TaskDeadlinesServiceImpl(TaskPersistenceContext persistenceContext) {
    	this.persistenceContext = persistenceContext;
    }

    public void setPersistenceContext(TaskPersistenceContext persistenceContext) {
        this.persistenceContext = persistenceContext;
    }


    public void schedule(long taskId, long deadlineId, long delay, DeadlineType type) {
        Task task = persistenceContext.findTask(taskId);
        String deploymentId = task.getTaskData().getDeploymentId();

        TimerService timerService = TimerServiceRegistry.getInstance().get(deploymentId + TimerServiceRegistry.TIMER_SERVICE_SUFFIX);
        if (timerService != null) {
            logger.debug("Scheduling in timer service of type {} for deployment {}, task {}, deadline {}, delay {} and type {}", timerService.getClass(), deploymentId, taskId, deadlineId, delay, type);
            TaskDeadlineJob deadlineJob = new TaskDeadlineJob(taskId, deadlineId, type, deploymentId, task.getTaskData().getProcessInstanceId());
            Trigger trigger = new IntervalTrigger( timerService.getCurrentTime(),
                    null,
                    null,
                    -1,
                    delay,
                    0,
                    null,
                    null ) ;
            JobHandle handle = timerService.scheduleJob(deadlineJob, new TaskDeadlineJobContext(deadlineJob.getId(), task.getTaskData().getProcessInstanceId(), deploymentId), trigger);
            jobHandles.put(deadlineJob.getId(), handle);

        } else {
            logger.error("No scheduler found for deadline {}", deploymentId);
        }

    }

    public void unschedule(long taskId, DeadlineType type) {
        Task task = persistenceContext.findTask(taskId);
        String deploymentId = task.getTaskData().getDeploymentId();
        
        Deadlines deadlines = ((InternalTask)task).getDeadlines();

        TimerService timerService = TimerServiceRegistry.getInstance().get(deploymentId + TimerServiceRegistry.TIMER_SERVICE_SUFFIX);
        if (timerService != null) {
 
            if (type == DeadlineType.START) {
                List<Deadline> startDeadlines = deadlines.getStartDeadlines();
                List<DeadlineSummary> resultList = (List<DeadlineSummary>)persistenceContext.queryWithParametersInTransaction("UnescalatedStartDeadlinesByTaskId",
                		persistenceContext.addParametersToMap("taskId", taskId),
						ClassUtil.<List<DeadlineSummary>>castClass(List.class));
                for (DeadlineSummary summary : resultList) {
                    TaskDeadlineJob deadlineJob = new TaskDeadlineJob(summary.getTaskId(), summary.getDeadlineId(), DeadlineType.START, deploymentId, task.getTaskData().getProcessInstanceId());
                    logger.debug("unscheduling timer job for deadline {} {} and task {}  using timer service {}", deadlineJob.getId(), summary.getDeadlineId(), taskId, timerService);
                    JobHandle jobHandle = jobHandles.remove(deadlineJob.getId()); 
                    if (jobHandle == null && timerService instanceof GlobalTimerService) {
                        jobHandle = ((GlobalTimerService) timerService).buildJobHandleForContext(new TaskDeadlineJobContext(deadlineJob.getId(), task.getTaskData().getProcessInstanceId(), deploymentId));
                    }

                    if (jobHandle != null) {
                        timerService.removeJob(jobHandle);
                    }
                    // mark the deadlines so they won't be rescheduled again
                    for (Deadline deadline : startDeadlines) {
                        if (deadline.getId() == summary.getDeadlineId()) {
                            deadline.setEscalated(true);
                        }
                    }
                }
            } else if (type == DeadlineType.END) {
                List<Deadline> endDeadlines = deadlines.getStartDeadlines();
                List<DeadlineSummary> resultList = (List<DeadlineSummary>)persistenceContext.queryWithParametersInTransaction("UnescalatedEndDeadlinesByTaskId",
                		persistenceContext.addParametersToMap("taskId", taskId),
						ClassUtil.<List<DeadlineSummary>>castClass(List.class));
                for (DeadlineSummary summary : resultList) {
                    
                    TaskDeadlineJob deadlineJob = new TaskDeadlineJob(summary.getTaskId(), summary.getDeadlineId(), DeadlineType.END, deploymentId, task.getTaskData().getProcessInstanceId());
                    logger.debug("unscheduling timer job for deadline {} and task {}  using timer service {}", deadlineJob.getId(), taskId, timerService);
                    JobHandle jobHandle = jobHandles.remove(deadlineJob.getId()); 
                    if (jobHandle == null && timerService instanceof GlobalTimerService) {
                        jobHandle = ((GlobalTimerService) timerService).buildJobHandleForContext(new TaskDeadlineJobContext(deadlineJob.getId(), task.getTaskData().getProcessInstanceId(), deploymentId));
                    }
                    if (jobHandle != null) {
                        timerService.removeJob(jobHandle);
                    }
                    // mark the deadlines so they won't be rescheduled again
                    for (Deadline deadline : endDeadlines) {
                        if (deadline.getId() == summary.getDeadlineId()) {
                            deadline.setEscalated(true);
                        }
                    }
                }
            }
            
        } else {
            logger.error("No scheduler found for deadline {}", deploymentId);
        }
    }
    
    public void unschedule(long taskId, Deadline deadline, DeadlineType type) {
        Task task = persistenceContext.findTask(taskId);
        String deploymentId = task.getTaskData().getDeploymentId();
        
        TimerService timerService = TimerServiceRegistry.getInstance().get(deploymentId + TimerServiceRegistry.TIMER_SERVICE_SUFFIX);
        if (timerService instanceof GlobalTimerService) {
             
            TaskDeadlineJob deadlineJob = new TaskDeadlineJob(taskId, deadline.getId(), type, deploymentId, task.getTaskData().getProcessInstanceId());
            logger.debug("unscheduling timer job for deadline {} {} and task {}  using timer service {}", deadlineJob.getId(), deadline.getId(), taskId, timerService);
            JobHandle jobHandle = jobHandles.remove(deadlineJob.getId()); 
            if (jobHandle == null) {        
                jobHandle = ((GlobalTimerService) timerService).buildJobHandleForContext(new TaskDeadlineJobContext(deadlineJob.getId(), task.getTaskData().getProcessInstanceId(), deploymentId));
            }
            timerService.removeJob(jobHandle);
            // mark the deadlines so they won't be rescheduled again                  
            deadline.setEscalated(true);
             
        }
    }

    @SuppressWarnings("unused")
    private class TaskDeadlineJob implements Job, Serializable {

        private static final long serialVersionUID = -2453658968872574615L;
        private long taskId;
        private long deadlineId;
        private DeadlineType type;
        private String deploymentId;
        private Long processInstanceId;
        
        public TaskDeadlineJob(long taskId, long deadlineId, DeadlineType type, String deploymentId, Long processInstanceId) {
            this.taskId = taskId;
            this.deadlineId = deadlineId;
            this.type = type;
            this.deploymentId = deploymentId;
            this.processInstanceId = processInstanceId;
        }
        
        public long getTaskId() {
            return taskId;
        }

        public long getDeadlineId() {
            return deadlineId;
        }
        
        public DeadlineType getType() {
            return this.type;
        }
        
        public String getDeploymentId() {
            return deploymentId;
        }
        
        public long getProcessInstanceId() {
            return processInstanceId;
        }

        @Override
        public void execute(JobContext ctx) {
            schedule(deploymentId, processInstanceId, taskId, deadlineId, type);
        }
        
        public String getId() {
            return taskId +"_"+deadlineId+"_"+type;
        }
    }
    

    private void schedule(String deploymentId, Long processInstanceId, long taskId, long deadlineId, DeadlineType type) {
        ExecuteDeadlinesCommand command = new ExecuteDeadlinesCommand(taskId, deadlineId, type);
        TaskServiceRegistry.instance().get(deploymentId).execute(command);
    }

    private static class TaskDeadlineJobContext implements NamedJobContext {

        private static final long serialVersionUID = -6838102884655249845L;
        private JobHandle jobHandle;
        private String jobName;
        private Long processInstanceId;
        private String deploymentId;
        
        public TaskDeadlineJobContext(String jobName, Long processInstanceId, String deploymentId) {
            this.jobName = jobName;
            this.processInstanceId = processInstanceId;
            this.deploymentId = deploymentId;
        }
        
        @Override
        public void setJobHandle(JobHandle jobHandle) {
            this.jobHandle = jobHandle;
        }

        @Override
        public JobHandle getJobHandle() {
            return jobHandle;
        }

        @Override
        public String getJobName() {
            return jobName;
        }

		@Override
		public Long getProcessInstanceId() {
			return processInstanceId;
		}

		@Override
        public String getDeploymentId() {
            return deploymentId;
        }

        @Override
        public InternalWorkingMemory getWorkingMemory() {
            return null;
        }
    }

    public void dispose() {
        try {
            jobHandles.clear();
        } catch (Exception e) {
            logger.error("Error encountered when disposing TaskDeadlineService", e);
        }
    }

}
