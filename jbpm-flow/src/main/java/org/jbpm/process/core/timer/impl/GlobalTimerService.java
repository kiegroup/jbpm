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
package org.jbpm.process.core.timer.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;

import org.drools.core.command.SingleSessionCommandService;
import org.drools.core.command.impl.CommandBasedStatefulKnowledgeSession;
import org.drools.core.common.InternalKnowledgeRuntime;
import org.drools.core.runtime.InternalLocalRunner;
import org.drools.core.time.InternalSchedulerService;
import org.drools.core.time.Job;
import org.drools.core.time.JobContext;
import org.drools.core.time.JobHandle;
import org.drools.core.time.SelfRemovalJobContext;
import org.drools.core.time.TimerService;
import org.drools.core.time.Trigger;
import org.drools.core.time.impl.CommandServiceTimerJobFactoryManager;
import org.drools.core.time.impl.DefaultJobHandle;
import org.drools.core.time.impl.TimerJobFactoryManager;
import org.drools.core.time.impl.TimerJobInstance;
import org.jbpm.process.core.timer.GlobalSchedulerService;
import org.jbpm.process.core.timer.NamedJobContext;
import org.jbpm.process.instance.timer.TimerManager.ProcessJobContext;
import org.jbpm.process.instance.timer.TimerManager.StartProcessJobContext;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.Executable;
import org.kie.api.runtime.ExecutableRunner;
import org.kie.api.runtime.RequestContext;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.internal.runtime.manager.InternalRuntimeManager;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GlobalTimerService implements TimerService, InternalSchedulerService {
	

	private static final Logger logger = LoggerFactory.getLogger(GlobalTimerService.class);
    
    protected TimerJobFactoryManager jobFactoryManager;
    protected final GlobalSchedulerService schedulerService;
    protected final RuntimeManager manager;
    protected final ConcurrentHashMap<Long, List<GlobalJobHandle>> timerJobsPerSession = new ConcurrentHashMap<Long, List<GlobalJobHandle>>();
    protected final ConcurrentSkipListSet<GlobalJobHandle> startTimerJobs = new ConcurrentSkipListSet<>((o1, o2) -> ((Long) o1.getId()).compareTo(o2.getId()));

    private List<TimerServiceListener> listeners; 
    private String timerServiceId;
    
    public GlobalTimerService(RuntimeManager manager, GlobalSchedulerService schedulerService) {
        this.manager = manager;
        this.schedulerService = schedulerService;
        this.schedulerService.initScheduler(this);
        jobFactoryManager = initJobFactoryManager();
        this.listeners = new ArrayList<>();
    }

    private TimerJobFactoryManager initJobFactoryManager() {
        try {
            return (TimerJobFactoryManager ) Class.forName("org.jbpm.persistence.timer.GlobalJPATimerJobFactoryManager").newInstance();
        } catch (Exception e) {
            logger.error("Cannot instantiate TimerJobFactoryManager");
        }
        return null;
    }

    public boolean isTransactional() {
        return this.schedulerService.isTransactional();
    }

    @Override
    public JobHandle scheduleJob(Job job, JobContext ctx, Trigger trigger) {
        if (ctx instanceof StartProcessJobContext) {
            // no session for start process job context
            GlobalJobHandle jobHandle = registerJobHandle(job, ctx, trigger);
            if (jobHandle != null) {
                startTimerJobs.add(jobHandle);
            }
            return jobHandle;
        } else if (ctx instanceof ProcessJobContext) {
            ProcessJobContext processCtx = (ProcessJobContext) ctx; 
 
            List<GlobalJobHandle> jobHandles = timerJobsPerSession.get(processCtx.getSessionId());
            if (jobHandles == null) {
                jobHandles = new CopyOnWriteArrayList<GlobalJobHandle>();
                timerJobsPerSession.put(processCtx.getSessionId(), jobHandles);
            } else {
                // check if the given job is already scheduled
                for (GlobalJobHandle handle : jobHandles) {
                    Long timerId = handle.getTimerId();
                    if (timerId != null && timerId == processCtx.getTimer().getId()) {
                        // this timer job is already registered
                        return handle;
                    }
                }
            }
            GlobalJobHandle jobHandle = registerJobHandle(job, ctx, trigger);
            if (jobHandle != null) {
            	jobHandles.add(jobHandle);
            }
                       
            return jobHandle;
        }

        return registerJobHandle(job, ctx, trigger);
    }

    public void removeJobByTimerId(long processInstanceId, long timerId) {
        TimerJobInstance instance = this.schedulerService.getTimerJobInstance(processInstanceId, timerId);
        if (instance != null) {
            removeJob(instance.getJobHandle());
        }
    }

    @Override
    public boolean removeJob(JobHandle jobHandle) {
        if (jobHandle == null) {
            return false;
        }
        this.schedulerService.invalidate(jobHandle);

        if (startTimerJobs.contains(jobHandle)) {
            logger.debug("Start Job timer handle found {} removed", jobHandle.getId());
            return unregisterJobHandle(jobHandle);
        }

        long sessionId = ((GlobalJobHandle) jobHandle).getSessionId();
        List<GlobalJobHandle> handles = timerJobsPerSession.get(sessionId);
        if (handles == null) {
            logger.debug("No known job handles for session {}", sessionId);
            return unregisterJobHandle(jobHandle);
        }

        if (handles.contains(jobHandle)) {
        	logger.debug("Found match so removing job handle {} from sessions {} handles", jobHandle, sessionId);
            handles.remove(jobHandle);
            if (handles.isEmpty()) {
                timerJobsPerSession.remove(sessionId);
            }
            return unregisterJobHandle(jobHandle);
        } else {
        	logger.debug("No match for job handle {} within handles of session {}", jobHandle, sessionId);
            return false;
        }
    }

    private GlobalJobHandle registerJobHandle (Job job, JobContext ctx, Trigger trigger) {
        GlobalJobHandle jobHandle = (GlobalJobHandle) this.schedulerService.scheduleJob(job, ctx, trigger);
        listeners.forEach(listener -> listener.fireTimerScheduled(jobHandle));
        return jobHandle;
    }

    private boolean unregisterJobHandle(JobHandle jobHandle) {
        boolean jobRemoved = this.schedulerService.removeJob(jobHandle);
        listeners.forEach(listener -> listener.fireTimerCancelled(jobHandle));
        return jobRemoved;
    }

    @Override
    public long getCurrentTime() {
        return System.currentTimeMillis();
    }

    @Override
    public void reset() {
        schedulerService.initScheduler(this);
        timerJobsPerSession.clear();
        startTimerJobs.clear();
        jobFactoryManager = initJobFactoryManager();
    }

    @Override
    public void shutdown() {
        //do nothing, this timer service is always active

    }
    
    public void destroy() {
        Collection<List<GlobalJobHandle>> activeTimers = timerJobsPerSession.values();
        for (List<GlobalJobHandle> handles : activeTimers) {
            for (GlobalJobHandle handle : handles) {
                unregisterJobHandle(handle);
            }
        }

        startTimerJobs.stream().forEach(handle -> unregisterJobHandle(handle));
    }

    @Override
    public long getTimeToNextJob() {
        return 0;
    }

    @Override
    public Collection<TimerJobInstance> getTimerJobInstances(long id) {
        Collection<TimerJobInstance> timers = new ArrayList<TimerJobInstance>();
        List<GlobalJobHandle> jobs = timerJobsPerSession.get(id); {
            if (jobs != null) {
                for (GlobalJobHandle job : jobs) {
                	if (job != null && schedulerService.isValid(job)) {
                		timers.add(job.getTimerJobInstance());
                	}
                }
            }
        }   
        logger.debug("Returning timers {} per session registered for KieSessionId {}", timers, id);
        return timers;
    }
    
    public void clearTimerJobInstances(long id) {
        synchronized (timerJobsPerSession) {            
            List<GlobalJobHandle> jobs = timerJobsPerSession.remove(id); 

            logger.debug(" KieSessionId {} jobs removed: {}. Current size of timerJobsPerSession is {}", id, (jobs == null) ? "none" : jobs,  timerJobsPerSession.size());

            if (jobs != null) {
                for (GlobalJobHandle handle : jobs) {
                    jobFactoryManager.removeTimerJobInstance(handle.getTimerJobInstance());
                }
            }
        }
    }

    @Override
    public void internalSchedule(TimerJobInstance timerJobInstance) {
        if (this.schedulerService instanceof InternalSchedulerService) {
            ((InternalSchedulerService) this.schedulerService).internalSchedule(timerJobInstance);
        } else {
            throw new UnsupportedOperationException("Unsupported scheduler operation internalSchedule on class " + this.schedulerService.getClass()); 
        }
    }

    @Override
    public void setTimerJobFactoryManager(TimerJobFactoryManager timerJobFactoryManager) {
    	if (jobFactoryManager instanceof CommandServiceTimerJobFactoryManager &&
            timerJobFactoryManager instanceof CommandServiceTimerJobFactoryManager &&
            getRunner() == null) {
            ( (CommandServiceTimerJobFactoryManager) jobFactoryManager ).setRunner( ( (CommandServiceTimerJobFactoryManager) timerJobFactoryManager ).getRunner() );
    	}
    }

    @Override
    public TimerJobFactoryManager getTimerJobFactoryManager() {
        return this.jobFactoryManager;
    }
    
    public ExecutableRunner getRunner( JobContext jobContext ) {
        JobContext ctxorig = jobContext;
        if (ctxorig instanceof SelfRemovalJobContext) {
            ctxorig = ((SelfRemovalJobContext) ctxorig).getJobContext();
        }
        ProcessJobContext ctx = null;
        if (ctxorig instanceof ProcessJobContext) {
            ctx = (ProcessJobContext) ctxorig;
        } else if(ctxorig instanceof NamedJobContext){
        	return getRunner( ((NamedJobContext)ctxorig).getProcessInstanceId(), ctx );
        } else {
            return getRunner();
        }
        
        return getRunner( ctx.getProcessInstanceId(), ctx );
    }
    
    public String getTimerServiceId() {
        return timerServiceId;
    }

    public void setTimerServiceId(String timerServiceId) {
        this.timerServiceId = timerServiceId;
    }
    
    public JobHandle buildJobHandleForContext(NamedJobContext ctx) {
        return this.schedulerService.buildJobHandleForContext(ctx);
    }
    
    public InternalRuntimeManager getRuntimeManager() {
    	return (InternalRuntimeManager) manager;
    }
    
    protected ExecutableRunner getRunner( Long processInstanceId, ProcessJobContext ctx ) {
    	RuntimeEngine runtime = manager.getRuntimeEngine(ProcessInstanceIdContext.get(processInstanceId));
        if (runtime == null) {
            throw new RuntimeException("No runtime engine found, could not be initialized yet");
        }
        try {
            if (runtime.getKieSession() instanceof CommandBasedStatefulKnowledgeSession) {
                CommandBasedStatefulKnowledgeSession cmd = (CommandBasedStatefulKnowledgeSession) runtime.getKieSession();
                if (ctx != null) {
                    ctx.setKnowledgeRuntime((InternalKnowledgeRuntime) ( (SingleSessionCommandService) cmd.getRunner() ).getKieSession() );
                }
                return new DisposableCommandService(cmd.getRunner(), manager, runtime, schedulerService.retryEnabled());
            } else if (runtime.getKieSession() instanceof InternalKnowledgeRuntime && ctx != null) {
                ctx.setKnowledgeRuntime((InternalKnowledgeRuntime) runtime.getKieSession());
            }
            
            return new DisposableCommandService(getRunner(), manager, runtime, schedulerService.retryEnabled());
        } catch (Throwable e) {
            // since the DisposableCommandService was not created dispose runtime engine directly
            manager.disposeRuntimeEngine(runtime);
            throw new RuntimeException(e);
        }
    }

    private ExecutableRunner<RequestContext> getRunner() {
        return jobFactoryManager instanceof CommandServiceTimerJobFactoryManager ?
               ( (CommandServiceTimerJobFactoryManager) jobFactoryManager ).getRunner() :
               null;
    }
        
    public ConcurrentHashMap<Long, List<GlobalJobHandle>> getTimerJobsPerSession() {
        return timerJobsPerSession;
    }


    public static class GlobalJobHandle extends DefaultJobHandle
        implements
        JobHandle{
    
        private static final long     serialVersionUID = 510l;
    
        public GlobalJobHandle(long id) {
            super(id);
        }
        
        public Long getTimerId() {
            JobContext ctx = this.getTimerJobInstance().getJobContext();
            if (ctx instanceof SelfRemovalJobContext) {
                ctx = ((SelfRemovalJobContext) ctx).getJobContext();
            }
            if (ctx instanceof ProcessJobContext) {
                return ((ProcessJobContext)ctx).getTimer().getId();
            } else {
                return null;
            }
        }
    
        public long getSessionId() {
        	if (this.getTimerJobInstance() != null) {
	            JobContext ctx = this.getTimerJobInstance().getJobContext();
	            if (ctx instanceof SelfRemovalJobContext) {
	                ctx = ((SelfRemovalJobContext) ctx).getJobContext();
	            }
	            if (ctx instanceof ProcessJobContext) {
	                return ((ProcessJobContext)ctx).getSessionId();
	            }
        	}
            return -1;
        }

        public String getUuid() {
            return null;
        }

    }
    
    public static class DisposableCommandService implements InternalLocalRunner {

        private ExecutableRunner<RequestContext> delegate;
        private RuntimeManager manager;
        private RuntimeEngine runtime;
        private boolean retry = false;
        
        
        public DisposableCommandService(ExecutableRunner<RequestContext> delegate, RuntimeManager manager, RuntimeEngine runtime, boolean retry) {
            this.delegate = delegate;
            this.manager = manager;
            this.runtime = runtime;
            this.retry = retry;
        }

        @Override
        public RequestContext execute( Executable executable, RequestContext ctx ) {
        	try {
        		if (delegate == null) {
                    ExecutableRunner<RequestContext> runner = ExecutableRunner.create();
                    RequestContext context = runner.createContext().with( runtime.getKieSession() );
                    return runner.execute( executable, context );
        		} else {
                    return delegate.execute(executable);
                }
        	} catch (RuntimeException e) {
        		if (retry) {
        			return delegate.execute(executable, ctx);
        		} else {
        			throw e;
        		}
        	}
        }

        @Override
        public RequestContext createContext() {
            return delegate.createContext();
        }
        
        public void dispose() {
            manager.disposeRuntimeEngine(runtime);
        }
        
        public Environment getEnvironment() {
        	
        	return runtime.getKieSession().getEnvironment();
        }
        
    }

    public List<TimerServiceListener> getListeners() {
        return listeners;
    }

    public void addTimerServiceListener(TimerServiceListener timerServiceListener) {
        listeners.add(timerServiceListener);
    }

    public void removeTimerServiceListener(TimerServiceListener timerServiceListener) {
        listeners.remove(timerServiceListener);
    }
}
