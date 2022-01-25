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

package org.jbpm.services.ejb.timer;

import java.util.concurrent.atomic.AtomicLong;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.drools.core.time.InternalSchedulerService;
import org.drools.core.time.Job;
import org.drools.core.time.JobContext;
import org.drools.core.time.JobHandle;
import org.drools.core.time.TimerService;
import org.drools.core.time.Trigger;
import org.drools.core.time.impl.TimerJobInstance;
import org.drools.persistence.api.TransactionManager;
import org.drools.persistence.api.TransactionManagerFactory;
import org.drools.persistence.api.TransactionSynchronization;
import org.drools.persistence.jta.JtaTransactionManager;
import org.jbpm.process.core.timer.GlobalSchedulerService;
import org.jbpm.process.core.timer.JobNameHelper;
import org.jbpm.process.core.timer.NamedJobContext;
import org.jbpm.process.core.timer.SchedulerServiceInterceptor;
import org.jbpm.process.core.timer.impl.DelegateSchedulerServiceInterceptor;
import org.jbpm.process.core.timer.impl.GlobalTimerService;
import org.jbpm.process.core.timer.impl.GlobalTimerService.GlobalJobHandle;
import org.jbpm.process.instance.timer.TimerManager.ProcessJobContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EjbSchedulerService implements GlobalSchedulerService {
    private static final Logger logger = LoggerFactory.getLogger(EjbSchedulerService.class);

    private static final Boolean TRANSACTIONAL = Boolean.parseBoolean(System.getProperty("org.jbpm.ejb.timer.tx", "true"));

	private AtomicLong idCounter = new AtomicLong();
	private TimerService globalTimerService;
	private EJBTimerScheduler scheduler;
	
	private SchedulerServiceInterceptor interceptor = new DelegateSchedulerServiceInterceptor(this);
	

	@Override
	public JobHandle scheduleJob(Job job, JobContext ctx, Trigger trigger) {
		long id = idCounter.getAndIncrement();
		String jobName = getJobName(ctx, id);
		EjbGlobalJobHandle jobHandle = new EjbGlobalJobHandle(id, jobName, ((GlobalTimerService) globalTimerService).getTimerServiceId());
		
		TimerJobInstance jobInstance = null;
		// check if given timer job is marked as new timer meaning it was never scheduled before, 
		// if so skip the check by timer name as it has no way to exist
		if (!isNewTimer(ctx)) {
    		jobInstance = scheduler.getTimerByName(jobName);
    		if (jobInstance != null) {
    			return jobInstance.getJobHandle();
    		}
		}
		jobInstance = globalTimerService.getTimerJobFactoryManager().createTimerJobInstance(
														job, 
														ctx, 
														trigger, 
														jobHandle, 
														(InternalSchedulerService) globalTimerService);
		
		jobHandle.setTimerJobInstance((TimerJobInstance) jobInstance);		
		interceptor.internalSchedule(jobInstance);
		return jobHandle;
	}

    @Override
    public boolean removeJob(JobHandle jobHandle) {
        JtaTransactionManager tm = (JtaTransactionManager) TransactionManagerFactory.get().newTransactionManager();
        try {
            tm.registerTransactionSynchronization(new TransactionSynchronization() {
                @Override
                public void beforeCompletion() {
                }

                @Override
                public void afterCompletion(int status) {
                    if (status == TransactionManager.STATUS_COMMITTED) {
                        logger.debug("remove job {} after commited", jobHandle);
                        scheduler.removeJob(jobHandle);
                    }
                }
                
            });
            logger.debug("register tx to remove job {}", jobHandle);
            return true;
        } catch (Exception e) {
            logger.debug("remove job {} outside tx", jobHandle);
            return scheduler.removeJob(jobHandle);
        }
    }

    @Override
    public void invalidate(JobHandle jobHandle) {
        scheduler.evictCache(jobHandle);
    }

    @Override
    public void internalSchedule(TimerJobInstance timerJobInstance) {
        scheduler.internalSchedule(timerJobInstance);
    }

	@Override
	public void initScheduler(TimerService timerService) {
		this.globalTimerService = timerService;
		try {
			this.scheduler = InitialContext.doLookup("java:module/EJBTimerScheduler");
		} catch (NamingException e) {
			throw new RuntimeException("Unable to find EJB scheduler for jBPM timer service", e);
		}
	}

	@Override
	public void shutdown() {
		// managed by container - no op

	}

	@Override
	public JobHandle buildJobHandleForContext(NamedJobContext ctx) {

		return new EjbGlobalJobHandle(-1, getJobName(ctx, -1L), ((GlobalTimerService) globalTimerService).getTimerServiceId());
	}

	@Override
	public boolean isTransactional() {
		return TRANSACTIONAL;
	}

	@Override
	public boolean retryEnabled() {
		return false;
	}

	@Override
	public void setInterceptor(SchedulerServiceInterceptor interceptor) {
	    this.interceptor = interceptor; 
	}

	@Override
	public boolean isValid(GlobalJobHandle jobHandle) {
	    
        return true;	    
	}
	
    protected String getJobName(JobContext ctx, long id) {
           return JobNameHelper.getJobName(ctx, id);
	}
	
   private boolean isNewTimer(JobContext ctx) {
       return ctx instanceof ProcessJobContext && ((ProcessJobContext) ctx).isNewTimer();
   }

}
