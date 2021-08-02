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
package org.jbpm.runtime.manager.impl;


import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.drools.core.time.TimerService;
import org.drools.persistence.api.OrderedTransactionSynchronization;
import org.drools.persistence.api.TransactionManager;
import org.drools.persistence.api.TransactionManagerFactory;
import org.drools.persistence.api.TransactionManagerHelper;
import org.drools.persistence.api.TransactionSynchronization;
import org.jbpm.process.core.timer.GlobalSchedulerService;
import org.jbpm.process.core.timer.TimerServiceRegistry;
import org.jbpm.process.core.timer.impl.GlobalTimerService;
import org.jbpm.runtime.manager.api.SchedulerProvider;
import org.jbpm.runtime.manager.impl.error.DefaultExecutionErrorStorage;
import org.jbpm.runtime.manager.impl.error.ExecutionErrorManagerImpl;
import org.jbpm.runtime.manager.impl.lock.RuntimeManagerLockStrategyFactory;
import org.jbpm.runtime.manager.impl.lock.RuntimeManagerLockWatcherSingletonService;
import org.jbpm.runtime.manager.impl.tx.NoOpTransactionManager;
import org.jbpm.runtime.manager.spi.RuntimeManagerLock;
import org.jbpm.runtime.manager.spi.RuntimeManagerLockStrategy;
import org.jbpm.services.task.impl.TaskContentRegistry;
import org.jbpm.services.task.impl.command.CommandBasedTaskService;
import org.jbpm.services.task.wih.ExternalTaskEventListener;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.RuleRuntimeEventListener;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.Context;
import org.kie.api.runtime.manager.RegisterableItemsFactory;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeEnvironment;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.task.TaskLifeCycleEventListener;
import org.kie.internal.runtime.conf.DeploymentDescriptor;
import org.kie.internal.runtime.error.ExecutionErrorManager;
import org.kie.internal.runtime.error.ExecutionErrorStorage;
import org.kie.internal.runtime.manager.CacheManager;
import org.kie.internal.runtime.manager.Disposable;
import org.kie.internal.runtime.manager.DisposeListener;
import org.kie.internal.runtime.manager.InternalRegisterableItemsFactory;
import org.kie.internal.runtime.manager.InternalRuntimeEngine;
import org.kie.internal.runtime.manager.InternalRuntimeManager;
import org.kie.internal.runtime.manager.RuntimeManagerRegistry;
import org.kie.internal.runtime.manager.SecurityManager;
import org.kie.internal.runtime.manager.SessionFactory;
import org.kie.internal.runtime.manager.SessionNotFoundException;
import org.kie.internal.runtime.manager.TaskServiceFactory;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.kie.internal.runtime.manager.deploy.DeploymentDescriptorManager;
import org.kie.internal.task.api.EventService;
import org.kie.internal.task.api.InternalTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common implementation that all <code>RuntimeManager</code> implementations should inherit from.
 * Provides the following capabilities:
 * <ul>
 *  <li>keeps track of all active managers by their identifier and prevents multiple managers from having the same id</li>
 *  <li>provides a common close operation</li>
 *  <li>injects the RuntimeManager into the ksession's environment for further reference</li>
 *  <li>registers dispose callbacks (via transaction synchronization) 
 *  to dispose of the runtime engine automatically on transaction completion</li>
 *  <li>registers all defined items (work item handlers, event listeners)</li>
 * </ul>
 * Additionally, this provides a abstract <code>init</code> method that will be called on RuntimeManager instantiation. 
 */
public abstract class AbstractRuntimeManager implements InternalRuntimeManager {
    
    private static final Logger logger = LoggerFactory.getLogger(AbstractRuntimeManager.class);

    protected RuntimeManagerRegistry registry = RuntimeManagerRegistry.get();
    protected RuntimeManagerLockStrategyFactory lockStrategyFactory = new RuntimeManagerLockStrategyFactory();

    protected RuntimeEnvironment environment;
    protected DeploymentDescriptor deploymentDescriptor;
    protected KieContainer kieContainer;
    
	protected CacheManager cacheManager = new CacheManagerImpl();
    
    protected boolean engineInitEager = Boolean.parseBoolean(System.getProperty("org.jbpm.rm.engine.eager", "false"));

	protected String identifier;
    
    protected boolean closed = false;
    
    protected SecurityManager securityManager = null;
    protected ExecutionErrorManager executionErrorManager;
    protected RuntimeManagerLockStrategy runtimeManagerLockStrategy;
    protected RuntimeManagerLockWatcherSingletonService watcher;
    
    public AbstractRuntimeManager(RuntimeEnvironment environment, String identifier) {
        this.environment = environment;
        this.identifier = identifier;
        if (registry.isRegistered(identifier)) {
            throw new IllegalStateException("RuntimeManager with id " + identifier + " is already active");
        }

        // we start the reference and watcher
        watcher = RuntimeManagerLockWatcherSingletonService.reference();

        ((SimpleRuntimeEnvironment)environment).getEnvironmentTemplate().set(EnvironmentName.DEPLOYMENT_ID, this.getIdentifier());
        internalSetDeploymentDescriptor();
        internalSetKieContainer();
        ((InternalRegisterableItemsFactory)environment.getRegisterableItemsFactory()).setRuntimeManager(this);
        String eagerInit = (String)((SimpleRuntimeEnvironment)environment).getEnvironmentTemplate().get("RuntimeEngineEagerInit");
        if (eagerInit != null) {
        	engineInitEager = Boolean.parseBoolean(eagerInit);
        }
        ExecutionErrorStorage storage = (ExecutionErrorStorage) ((SimpleRuntimeEnvironment)environment).getEnvironmentTemplate().get("ExecutionErrorStorage");
        if (storage == null) {
            storage = new DefaultExecutionErrorStorage(environment.getEnvironment());
        }
        this.executionErrorManager = new ExecutionErrorManagerImpl(storage);
        ((SimpleRuntimeEnvironment)environment).getEnvironmentTemplate().set(EnvironmentName.EXEC_ERROR_MANAGER, executionErrorManager);
        logger.info("{} is created for {}", this.getClass().getSimpleName(), identifier);
    }

    private void internalSetDeploymentDescriptor() {
    	this.deploymentDescriptor = (DeploymentDescriptor) ((SimpleRuntimeEnvironment)environment).getEnvironmentTemplate().get("KieDeploymentDescriptor");
    	if (this.deploymentDescriptor == null) {
    		this.deploymentDescriptor = new DeploymentDescriptorManager().getDefaultDescriptor();
    	}
	}
    
    private void internalSetKieContainer() {
    	this.kieContainer = (KieContainer) ((SimpleRuntimeEnvironment)environment).getEnvironmentTemplate().get("KieContainer");
	}

	public void init() {
        if (!isUseLocking()) {
            runtimeManagerLockStrategy = lockStrategyFactory.createFreeLockStrategy();
        } else {
            runtimeManagerLockStrategy = lockStrategyFactory.createLockStrategy(identifier);
        }
	}
    
	protected void registerItems(RuntimeEngine runtime) {
        RegisterableItemsFactory factory = environment.getRegisterableItemsFactory();
        KieSession ksession = ((InternalRuntimeEngine) runtime).internalGetKieSession();
        // process handlers
        Map<String, WorkItemHandler> handlers = factory.getWorkItemHandlers(runtime);
        for (Entry<String, WorkItemHandler> entry : handlers.entrySet()) {
            ksession.getWorkItemManager().registerWorkItemHandler(entry.getKey(), entry.getValue());
        }
        
        // register globals
        Map<String, Object> globals = factory.getGlobals(runtime);
        for (Entry<String, Object> entry : globals.entrySet()) {
            ksession.setGlobal(entry.getKey(), entry.getValue());
        }
        
        // process listeners
        List<ProcessEventListener> processListeners = factory.getProcessEventListeners(runtime);
        for (ProcessEventListener listener : processListeners) {
            ksession.addEventListener(listener);
        }
        
        // agenda listeners
        List<AgendaEventListener> agendaListeners = factory.getAgendaEventListeners(runtime);
        for (AgendaEventListener listener : agendaListeners) {
            ksession.addEventListener(listener);
        }
        
        // working memory listeners
        List<RuleRuntimeEventListener> wmListeners = factory.getRuleRuntimeEventListeners(runtime);
        for (RuleRuntimeEventListener listener : wmListeners) {
            ksession.addEventListener(listener);
        }       
    }
    
    protected void registerDisposeCallback(RuntimeEngine runtime, TransactionSynchronization sync, Environment environment) {
        // register it if there is an active transaction as we assume then to be running in a managed environment e.g CMT       
        TransactionManager tm = getTransactionManager(environment);
        if (tm.getStatus() != TransactionManager.STATUS_NO_TRANSACTION
                && tm.getStatus() != TransactionManager.STATUS_ROLLEDBACK
                && tm.getStatus() != TransactionManager.STATUS_COMMITTED) {
            TransactionManagerHelper.registerTransactionSyncInContainer(tm, (OrderedTransactionSynchronization) sync);
        }
    }
    
    protected boolean canDispose(RuntimeEngine runtime) {
        // avoid duplicated dispose
        if (((RuntimeEngineImpl)runtime).isDisposed()) {
            return false;
        }
        // if this method was called as part of afterCompletion allow to dispose
        if (((RuntimeEngineImpl)runtime).isAfterCompletion()) {
            return true;
        }
        try {
            // check tx status to disallow dispose when within active transaction       
            TransactionManager tm = getTransactionManager(((InternalRuntimeEngine) runtime).internalGetKieSession().getEnvironment());
            if (tm.getStatus() != TransactionManager.STATUS_NO_TRANSACTION
                    && tm.getStatus() != TransactionManager.STATUS_ROLLEDBACK
                    && tm.getStatus() != TransactionManager.STATUS_COMMITTED) {
                return false;
            }
        } catch (SessionNotFoundException e) {
            // ignore it as it might be thrown for per process instance
        }
        
        return true;
    }
    
    protected void attachManager(RuntimeEngine runtime) {
        ((InternalRuntimeEngine) runtime).internalGetKieSession().getEnvironment().set(EnvironmentName.RUNTIME_MANAGER, this);
        ((InternalRuntimeEngine) runtime).internalGetKieSession().getEnvironment().set(EnvironmentName.DEPLOYMENT_ID, this.getIdentifier());
    }
    
    @Override
    public boolean isClosed() {
    	return this.closed;
    }

    @Override
    public void close() {
        close(false);
    }
    
    public void close(boolean removeJobs) {
    	cacheManager.dispose();
        environment.close();
        registry.remove(identifier);


        if(watcher != null) {
            watcher.unreference();
            watcher = null;
        }

        TimerService timerService = TimerServiceRegistry.getInstance().get(getIdentifier() + TimerServiceRegistry.TIMER_SERVICE_SUFFIX);
        if (timerService != null) {
            try {
                if (removeJobs && timerService instanceof GlobalTimerService) {
                    ((GlobalTimerService) timerService).destroy();
                }
                timerService.shutdown();
                GlobalSchedulerService schedulerService = ((SchedulerProvider) environment).getSchedulerService();  
                if (schedulerService != null) {
                    schedulerService.shutdown();
                }
            } finally {
                TimerServiceRegistry.getInstance().remove(getIdentifier() + TimerServiceRegistry.TIMER_SERVICE_SUFFIX);
            }
        }
        this.closed = true;
    }

    public org.kie.internal.runtime.manager.RuntimeEnvironment getEnvironment() {
        return (org.kie.internal.runtime.manager.RuntimeEnvironment)environment;
    }

    public void setEnvironment(RuntimeEnvironment environment) {
        this.environment = environment;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    
    protected InternalTaskService newTaskService(TaskServiceFactory factory) {
        InternalTaskService internalTaskService = (InternalTaskService) factory.newTaskService();
        if (internalTaskService instanceof CommandBasedTaskService) {
            ((CommandBasedTaskService) internalTaskService).getEnvironment().set(EnvironmentName.DEPLOYMENT_ID, this.getIdentifier());
            ((CommandBasedTaskService) internalTaskService).getEnvironment().set(EnvironmentName.OBJECT_MARSHALLING_STRATEGIES, 
                                                                                 ((SimpleRuntimeEnvironment)environment).getEnvironmentTemplate().get(EnvironmentName.OBJECT_MARSHALLING_STRATEGIES));
        }
        
        return internalTaskService;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected void configureRuntimeOnTaskService(InternalTaskService internalTaskService, RuntimeEngine engine) {
    	
        if (internalTaskService != null) {            
            
            ExternalTaskEventListener listener = new ExternalTaskEventListener();
            if (internalTaskService instanceof EventService) {
                ((EventService)internalTaskService).registerTaskEventListener(listener);
            }
            
          	// register task listeners if any  
            RegisterableItemsFactory factory = environment.getRegisterableItemsFactory();
        	for (TaskLifeCycleEventListener taskListener : factory.getTaskListeners()) {
        		((EventService<TaskLifeCycleEventListener>)internalTaskService).registerTaskEventListener(taskListener);
        	}

        }
    }
    
    protected void removeRuntimeFromTaskService() {
    	TaskContentRegistry.get().removeMarshallerContext(getIdentifier());
    }
    /**
     * Soft dispose means it will be invoked as sort of preparation step before actual dispose.
     * Mainly used with transaction synchronization to be invoked as part of beforeCompletion
     * to clean up any thread state - like thread local settings as afterCompletion can be invoked from another thread
     */
    public void softDispose(RuntimeEngine runtimeEngine) {
        
    }

    protected boolean canDestroy(RuntimeEngine runtime) {
    	if (((RuntimeEngineImpl) runtime).isAfterCompletion()) {
    		return false;
    	}
        TransactionManager tm = getTransactionManager(runtime.getKieSession().getEnvironment());
        if (tm.getStatus() == TransactionManager.STATUS_NO_TRANSACTION ||
                tm.getStatus() == TransactionManager.STATUS_ACTIVE) {
            return true;
        }
        return false;
    }

    protected boolean hasEnvironmentEntry(String name, Object value) {
    	Object envEntry = environment.getEnvironment().get(name);
    	if (value == null) {
    		return envEntry == null;
    	}
    	return value.equals(envEntry);
    }
    
    protected TransactionManager getTransactionManager(Environment env) {
    	if (env == null) {
    		env = environment.getEnvironment();
    	}
    	Object txm = env.get(EnvironmentName.TRANSACTION_MANAGER);
    	if (txm != null && txm instanceof TransactionManager) {
    		return (TransactionManager) txm;
    	}
    	
    	return TransactionManagerFactory.get().newTransactionManager(env);
    }
    
    protected TransactionManager getTransactionManagerInternal(Environment env) {
        
        try {
            return getTransactionManager(env);
        } catch (Exception e) {
            // return no op transaction manager as none were found so let the ksession manage the tx instead
            return new NoOpTransactionManager();
        }    
    }
    
    @Override
    public DeploymentDescriptor getDeploymentDescriptor() {
		return deploymentDescriptor;
	}

    @Override
	public void setDeploymentDescriptor(DeploymentDescriptor deploymentDescriptor) {
		this.deploymentDescriptor = deploymentDescriptor;
	}
    
    @Override
    public void setSecurityManager(SecurityManager securityManager) {
    	if (this.securityManager != null) {
    		throw new IllegalStateException("Security Manager for " + this.identifier + " manager is already set");
    	}
    	this.securityManager = securityManager;    
    }
    
    @Override
    public boolean hasSecurityManager() {
        return this.securityManager != null;
    }
    
    protected void checkPermission() {
    	if (this.securityManager != null) {
    		this.securityManager.checkPermission();
    	}
    }

	@Override
	public void setCacheManager(CacheManager cacheManager) {
		if (cacheManager != null) {
			this.cacheManager = cacheManager;
		}
	}

	@Override
	public CacheManager getCacheManager() {
		return cacheManager;
	}
	
	@Override
    public KieContainer getKieContainer() {
		return kieContainer;
	}

	@Override
	public void setKieContainer(KieContainer kieContainer) {
		this.kieContainer = kieContainer;
	}    


    /*
     * locking support for same context - runtime engine that deals with exact same process instance context
     */
    protected boolean isUseLocking() {
        return false;
    }

    protected void createLockOnNewProcessInstance(Long id, RuntimeEngine runtime) {
        createLockOnGetEngine(id, runtime);
    }

    protected void createLockOnGetEngine(Context<?> context, RuntimeEngine runtime) {
        if (context instanceof ProcessInstanceIdContext) {
            Long piId = ((ProcessInstanceIdContext) context).getContextId();
            createLockOnGetEngine(piId, runtime);
        } else {
            logger.debug("Trying to release lock with a non proper context on runtime engine {}", runtime);
        }
    }
    
    protected void createLockOnGetEngine(Long id, RuntimeEngine runtime) {
        logger.debug("Attempt to lock {} on runtime {}", id, runtime);
        if (id == null) {
            return;
        }
        if (!isUseLocking()) {
            logger.debug("Locking on runtime manager engine {} disabled for id {}", runtime, id);
            return;
        }
        try {
            RuntimeManagerLock lock = runtimeManagerLockStrategy.lock(id, runtime);
            if(watcher != null) {
                watcher.watch(id, lock);
            }
        } catch(InterruptedException e) {
            throw new RuntimeException("Runtime manager was not able to lock for {" + id +"}", e);
        }
    }

    protected void releaseAndCleanLock(RuntimeEngine runtime) {
        if (((RuntimeEngineImpl)runtime).getContext() instanceof ProcessInstanceIdContext) {
            Long piId = ((ProcessInstanceIdContext) ((RuntimeEngineImpl)runtime).getContext()).getContextId();
            if (piId != null) {
                releaseAndCleanLock(piId, runtime);
            }
        } else {
            logger.debug("Trying to release lock with a non proper context onn runtime manager engine {}", runtime);
        }
    }

    protected void releaseAndCleanLock(Long id, RuntimeEngine runtime) {
        logger.debug("Attempt to unlock {} on runtime {}", id, runtime);
        if (id == null) {
            return;
        }
        if (!isUseLocking()) {
            logger.debug("Locking on runtime manager engine {} disabled for id {}", runtime, id);
            return;
        }
        if(watcher != null) {
            watcher.unwatch(id);
        }
        runtimeManagerLockStrategy.unlock(id, runtime);
    }

    protected boolean isActive() {
        if (hasEnvironmentEntry("Active", false)) {
            return false;
        }
        
        return true;
    }

    @Override
    public void activate() {
        ((SimpleRuntimeEnvironment) environment).addToEnvironment("Active", true);
        
    }

    @Override
    public void deactivate() {
        ((SimpleRuntimeEnvironment) environment).addToEnvironment("Active", false);
        
    }
    
    public ExecutionErrorManager getExecutionErrorManager() {
        return executionErrorManager;
    }

    public abstract SessionFactory getFactory();
}
