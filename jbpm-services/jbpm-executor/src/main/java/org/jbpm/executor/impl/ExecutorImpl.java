/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.executor.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.drools.core.process.instance.WorkItem;
import org.drools.core.time.TimeUtils;
import org.drools.persistence.api.TransactionManager;
import org.jbpm.executor.ExecutorNotStartedException;
import org.jbpm.executor.impl.event.ExecutorEventSupport;
import org.jbpm.executor.impl.event.ExecutorEventSupportImpl;
import org.kie.api.executor.CommandContext;
import org.kie.api.executor.ExecutorStoreService;
import org.kie.api.executor.RequestInfo;
import org.kie.api.executor.STATUS;
import org.kie.internal.executor.api.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of the <code>Executor</code> that is baced by
 * <code>ScheduledExecutorService</code> for background task execution.
 * It can be configured for following:
 * <ul>
 *  <li>thread pool size - default 1 - use system property org.kie.executor.pool.size</li>
 *  <li>retry count - default 3 retries - use system property org.kie.executor.retry.count</li>
 *  <li>execution interval - default 3 seconds - use system property org.kie.executor.interval</li>
 * </ul>
 * Additionally executor can be disable to not start at all when system property org.kie.executor.disabled is 
 * set to true
 * Executor can be used with JMS as the medium to notify about jobs to be executed instead of relying strictly 
 * on poll mechanism that is available by default. JMS support is configurable and is enabled by default
 * although it requires JMS resources (connection factory and destination) to properly operate. If any of
 * these will not be found it will deactivate JMS support.
 * Configuration parameters for JMS support:
 * <ul>
 *  <li>org.kie.executor.jms - allows to enable JMS support globally - default set to true</li>
 *  <li>org.kie.executor.jms.cf - JNDI name of connection factory to be used for sending messages</li>
 *  <li>org.kie.executor.jms.queue - JNDI name for destination (usually a queue) to be used to send messages to</li>
 * </ul>
 */
public class ExecutorImpl implements Executor {

    private static final Logger logger = LoggerFactory.getLogger(ExecutorImpl.class);

    private static final int DEFAULT_PRIORITY = 5;
    private static final int MAX_PRIORITY = 9;
    private static final int MIN_PRIORITY = 0;

    private int retries = Integer.parseInt(System.getProperty("org.kie.executor.retry.count", "3"));
    private int interval = Integer.parseInt(System.getProperty("org.kie.executor.interval", "0"));
    private TimeUnit timeunit = TimeUnit.valueOf(System.getProperty("org.kie.executor.timeunit", "SECONDS"));

    private ExecutorStoreService executorStoreService;
    private List<TaskExecutorStrategy> taskExecutorStrategy;

    private ExecutorEventSupport eventSupport = new ExecutorEventSupportImpl();
    private AvailableJobsExecutor jobProcessor;
    private TransactionManager transactionManager;

    private int numberOfThreads;

    public ExecutorImpl() {
        taskExecutorStrategy = new ArrayList<>();
        taskExecutorStrategy.add(new JMSTaskExecutorStrategy());
        taskExecutorStrategy.add(new ThreadTaskExecutorStrategy());
    }

    public void setEventSupport(ExecutorEventSupport eventSupport) {
        this.eventSupport = eventSupport;
    }

    public void setExecutorStoreService(ExecutorStoreService executorStoreService) {
        this.executorStoreService = executorStoreService;
    }

    public ExecutorStoreService getExecutorStoreService() {
        return executorStoreService;
    }

    public AvailableJobsExecutor getJobProcessor() {
        return jobProcessor;
    }

    public void setJobProcessor(AvailableJobsExecutor jobProcessor) {
        this.jobProcessor = jobProcessor;
    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public int getInterval() {
        return interval;
    }

    @Override
    public void setInterval(int interval) {
        this.interval = interval;
    }

    @Override
    public int getRetries() {
        return retries;
    }

    @Override
    public void setRetries(int retries) {
        this.retries = retries;
    }

    @Override
    public int getThreadPoolSize() {
        return numberOfThreads;
    }

    @Override
    public void setThreadPoolSize(int threadPoolSize) {
        this.numberOfThreads = threadPoolSize;
    }

    @Override
    public TimeUnit getTimeunit() {
        return timeunit;
    }

    @Override
    public void setTimeunit(TimeUnit timeunit) {
        this.timeunit = timeunit;
    }

    @Override
    public void init() {
        if ("true".equalsIgnoreCase(System.getProperty("org.kie.executor.disabled"))) {
            return;
        }
        logger.info("Starting jBPM Executor component");
        taskExecutorStrategy.forEach(strategy -> {
            strategy.setInterval(interval, timeunit);
            strategy.setTransactionManager(transactionManager);
            strategy.setNumberOfThreads(numberOfThreads);
            strategy.setJobsExecutor(jobProcessor);
            strategy.setExecutor(ExecutorImpl.this);
            strategy.setExecutorStoreService(executorStoreService);
            strategy.init();
        });

        if(taskExecutorStrategy.stream().noneMatch(TaskExecutorStrategy::active)) {
            throw new ExecutorNotStartedException();
        }
    }

    @Override
    public void destroy() {
        if ("true".equalsIgnoreCase(System.getProperty("org.kie.executor.disabled"))) {
            return;
        }
        logger.info("Stopping jBPM Executor component");
        taskExecutorStrategy.forEach(TaskExecutorStrategy::destroy);
    }

    public void scheduleExecution(final RequestInfo requestInfo, final Date date) {
        Optional<TaskExecutorStrategy> executionStrategy = taskExecutorStrategy.stream().filter(strategy -> strategy.accept(requestInfo, date)).findFirst();
        if(executionStrategy.isPresent()) {
            requestInfo.setStatus(STATUS.SCHEDULED);
            executorStoreService.updateRequest(requestInfo, (job) -> {
                logger.info("Sending Job {} with execution strategy {} {}", requestInfo.getId(), executionStrategy.get().getClass().getCanonicalName(), date == null ? "inmediately" : "at " + date.toString());
                executionStrategy.get().schedule(requestInfo, date);
            });
        }
        logger.debug("Scheduled request for Command: {} - requestId: {} with {} retries", requestInfo.getCommandName(), requestInfo.getId(), requestInfo.getRetries());

    }

    public void clearExecution(Long requestId) {
        taskExecutorStrategy.forEach(strategy -> strategy.clear(requestId));
    }

    @Override
    public Long scheduleRequest(String commandId, CommandContext ctx) {
        return scheduleRequest(commandId, null, ctx);
    }

    @Override
    public Long scheduleRequest(String commandId, Date date, CommandContext ctx) {
        if (ctx == null) {
            throw new IllegalArgumentException("A Context Must Be Provided! ");
        }

        boolean owner = transactionManager.begin();
        RequestInfo requestInfo = buildRequestInfo(commandId, date, ctx);
        try {
            eventSupport.fireBeforeJobScheduled(requestInfo, null);
            executorStoreService.persistRequest(requestInfo, null);
            scheduleExecution(requestInfo, date);
            eventSupport.fireAfterJobScheduled(requestInfo, null);

        } catch (Throwable e) {
            eventSupport.fireAfterJobScheduled(requestInfo, e);
        } finally {
            transactionManager.commit(owner);
        }
        return requestInfo.getId();
    }

    @Override
    public void cancelRequest(Long requestId) {
        boolean owner = transactionManager.begin();
        RequestInfo job = (RequestInfo) executorStoreService.findRequest(requestId);
        try {
            logger.debug("Before - Cancelling Request with Id: {}", requestId);
            eventSupport.fireBeforeJobCancelled(job, null);
            executorStoreService.removeRequest(requestId, (T) -> taskExecutorStrategy.forEach(strategy -> strategy.cancel(job)));
            eventSupport.fireAfterJobCancelled(job, null);
            
        } catch (Throwable e) {
            logger.warn("Could not cancel Request with Id: {}", requestId);
            eventSupport.fireAfterJobCancelled(job, e);
        } finally {
            transactionManager.commit(owner);
        }

        logger.debug("After - Cancelling Request with Id: {}", requestId);
    }



    private RequestInfo buildRequestInfo(String commandId, Date date, CommandContext ctx) {
        String businessKey = (String) ctx.getData("businessKey");
        org.jbpm.executor.entities.RequestInfo requestInfo = new org.jbpm.executor.entities.RequestInfo();
        requestInfo.setCommandName(commandId);
        requestInfo.setKey(businessKey);
        requestInfo.setStatus(STATUS.QUEUED);
        requestInfo.setTime(date == null ? new Date() : date);
        requestInfo.setMessage("Ready to execute");
        requestInfo.setDeploymentId(getDeploymentId(ctx));
        if (ctx.getData("processInstanceId") != null) {
            requestInfo.setProcessInstanceId(((Number) ctx.getData("processInstanceId")).longValue());
        }
        requestInfo.setOwner((String) ctx.getData("owner"));
        if (ctx.getData("retries") != null) {
            requestInfo.setRetries(Integer.valueOf(String.valueOf(ctx.getData("retries"))));
        } else {
            requestInfo.setRetries(retries);
        }
        int priority = DEFAULT_PRIORITY;
        if (ctx.getData("priority") != null) {
            priority = (Integer) ctx.getData("priority");
            if (priority < MIN_PRIORITY) {
                logger.warn("Priority {} is not valid (cannot be less than {}) setting it to {}", priority, MIN_PRIORITY, MIN_PRIORITY);
                priority = MIN_PRIORITY;

            } else if (priority > MAX_PRIORITY) {
                logger.warn("Priority {} is not valid (cannot be more than {}) setting it to {}", priority, MAX_PRIORITY, MAX_PRIORITY);
                priority = MAX_PRIORITY;
            }

        }
        requestInfo.setPriority(priority);

        if (ctx.getData("retryDelay") != null) {
            List<Long> retryDelay = new ArrayList<Long>();
            String[] timeExpressions = ((String) ctx.getData("retryDelay")).split(",");

            for (String timeExpr : timeExpressions) {
                retryDelay.add(TimeUtils.parseTimeString(timeExpr));
            }
            ctx.setData("retryDelay", retryDelay);
        }

        requestInfo.setRequestData(ExecutorUtil.toByteArray(ctx));
        return requestInfo;
    }


    @Override
    public void updateRequestData(Long requestId, Map<String, Object> data) {
        logger.debug("About to update request {} data with following {}", requestId, data);

        org.jbpm.executor.entities.RequestInfo request = (org.jbpm.executor.entities.RequestInfo) executorStoreService.findRequest(requestId);
        EnumSet<STATUS> invalidStates = EnumSet.of(STATUS.CANCELLED, STATUS.DONE, STATUS.RUNNING);
        if (invalidStates.contains(request.getStatus())) {
            throw new IllegalStateException("Request data can't be updated when request is in status " + request.getStatus());
        }

        logger.debug("Processing Request Id: {}, status {} command {}", request.getId(), request.getStatus(), request.getCommandName());
        ClassLoader cl = ExecutorUtil.getClassLoader(request.getDeploymentId());
        CommandContext ctx = ExecutorUtil.toCommandContext(request.getRequestData(), cl);

        // update data
        WorkItem workItem = (WorkItem) ctx.getData("workItem");
        if (workItem != null) {
            logger.debug("Updating work item {} parameters with data {}", workItem, data);
            for (Entry<String, Object> entry : data.entrySet()) {
                workItem.setParameter(entry.getKey(), entry.getValue());
            }
        } else {
            logger.debug("Updating request context with data {}", data);
            for (Entry<String, Object> entry : data.entrySet()) {
                ctx.setData(entry.getKey(), entry.getValue());
            }
        }

        request.setRequestData(ExecutorUtil.toByteArray(ctx));
        executorStoreService.updateRequest(request, null);
        logger.debug("Request {} data updated successfully", requestId);
    }



    protected String getDeploymentId(CommandContext ctx) {
        String deploymentId = (String) ctx.getData("DeploymentId");
        if (deploymentId == null) {
            deploymentId = (String) ctx.getData("deploymentId");
        }
        
        return deploymentId;
    }

}
